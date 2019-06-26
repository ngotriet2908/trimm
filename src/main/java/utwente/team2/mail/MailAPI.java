package utwente.team2.mail;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

// based on the example from crunchify.com
public class MailAPI {
    static Properties mailServerProperties;

    public synchronized static void generateAndSendEmail(String body, String subject, String recipient) throws MessagingException, NoSuchMethodError {
        setupMailProperties();

        Session mailSession = Session.getDefaultInstance(mailServerProperties, null);
        MimeMessage message = createMessage(recipient, subject, mailSession);

        getTransportLayer(message, mailSession);
        System.out.println("Successfully sent an email to " + recipient);
    }

    public synchronized static void generateAndSendEmailWithAttachment(String context, String subject, String recipient, BufferedImage image)
            throws MessagingException, NoSuchMethodError {
        setupMailProperties();

        Session mailSession = Session.getDefaultInstance(mailServerProperties, null);
        MimeMessage message = createMessage(recipient, subject, mailSession);

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("You can find your infographic in the attachment below.");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayDataSource data = new ByteArrayDataSource(os.toByteArray(), "image/png");

        messageBodyPart.setDataHandler(new DataHandler(data));
        messageBodyPart.setFileName("infographic.png");
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        getTransportLayer(message, mailSession);
        System.out.println("Successfully sent an email to " + recipient);
    }

    public synchronized static void setupMailProperties() {
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
    }

    public synchronized static MimeMessage createMessage(String recipient, String subject, Session mailSession) throws MessagingException {
        MimeMessage mailMessage = new MimeMessage(mailSession);
        mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        mailMessage.setSubject(subject + " " + LocalDateTime.now().toString());
        return mailMessage;
    }

    public synchronized static void getTransportLayer(MimeMessage message, Session session) throws MessagingException {
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com", "datainfomationteam2@gmail.com", "minhtriet2908");
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
