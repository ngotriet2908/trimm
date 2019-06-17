package utwente.team2.mail;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * @author Crunchify.com
 *
 */

public class MailAPI {
    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;

    public static void main(String args[]) throws AddressException, MessagingException {
        generateAndSendEmail("hahahaha", "greeting from recovery email", "khavronayevhen@gmail.com");
    }

    public static void generateAndSendEmail(String context, String subject, String recipient) throws AddressException, MessagingException, NoSuchMethodError {

        // Step1
        System.out.println("1st ===> setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
        System.out.println("2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        generateMailMessage.setSubject(subject + " " + LocalDateTime.now().toString());

        String emailBody = context;
        //messageBodyPart.setText(html, "UTF-8", "html");
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");

        // Step3
        System.out.println("3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", "datainfomationteam2@gmail.com", "minhtriet2908");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        System.out.println("===> Your Java Program has just sent an Email successfully. Check your email..");
    }

    public static void generateAndSendEmailWithAttachtMent(String context, String subject, String recipient, BufferedImage image)
            throws AddressException, MessagingException, NoSuchMethodError {

        // Step1
        System.out.println("1st ===> setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
        System.out.println("2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        generateMailMessage.setSubject(subject + " " + LocalDateTime.now().toString());

        BodyPart messageBodyPart = new MimeBodyPart();

        // Now set the actual message
        messageBodyPart.setText("This is message body");

        // Create a multipar message
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment

//        File file = new File("temp.png");
//        try {
//            ImageIO.write(image, "png", file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        messageBodyPart = new MimeBodyPart();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayDataSource data = new ByteArrayDataSource(os.toByteArray(),"image/png");
        try {
            System.out.println(data.getInputStream().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageBodyPart.setDataHandler(new DataHandler(data));
        messageBodyPart.setFileName("kindle.png");
        multipart.addBodyPart(messageBodyPart);
//        boolean flag = file.delete();
//        System.out.println("delete image " + flag);
        // Send the complete message parts
        generateMailMessage.setContent(multipart);

        // Step3
        System.out.println("3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", "datainfomationteam2@gmail.com", "minhtriet2908");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        System.out.println("===> Your Java Program has just sent an Email successfully. Check your email..");
    }
}
