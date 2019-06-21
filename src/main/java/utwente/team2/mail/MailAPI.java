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


    public static void generateAndSendEmail(String body, String subject, String recipient) throws AddressException, MessagingException, NoSuchMethodError {

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

        //messageBodyPart.setText(html, "UTF-8", "html");
        generateMailMessage.setContent(body, "text/html");
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
        messageBodyPart.setText("You can find your infographic in the attachment below.");

        // Create a multipart message
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

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
        messageBodyPart.setFileName("infographic.png");
        multipart.addBodyPart(messageBodyPart);
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
