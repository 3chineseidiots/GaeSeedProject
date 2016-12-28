package ca.hansolutions.service.impl;

import ca.hansolutions.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


@Service
public class MailServiceGoogle implements MailService {

    @Value("${app.engine.id}")
    String appid;

    @Value("${app.email.sender.suffix}")
    String emailSenderAddressSuffix;

    @Value("${app.email.sender.username}")
    String emailSenderUsername;


    String emailSenderAddress;


    @Override
    public void sendSingleEmail(String title, String content, String receiverAddress, String receiverUsername) throws Exception {

        javax.mail.Message message = this.buildEmailMessage(title, content);
        message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(receiverAddress, receiverAddress));
        Transport.send(message);
    }

    @Override
    public void sendEmails(String title, String content, List<String> receiverAddresses, List<String> receiverUsernames) throws Exception {

        javax.mail.Message message = this.buildEmailMessage(title, content);
        List<InternetAddress> internetAddressList = new LinkedList<>();
        for(int i = 0; i < receiverAddresses.size(); i++){
            InternetAddress internetAddress = new InternetAddress(receiverAddresses.get(i), receiverUsernames.get(i));
            internetAddressList.add(internetAddress);
        }

        message.addRecipients(javax.mail.Message.RecipientType.BCC, internetAddressList.toArray(new InternetAddress[internetAddressList.size()]));
        Transport.send(message);
    }

    public javax.mail.Message buildEmailMessage(String title, String content) throws UnsupportedEncodingException, MessagingException {


        if(emailSenderAddress == null){

            emailSenderAddress = "info@" + appid + emailSenderAddressSuffix;
        }

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(emailSenderAddress, emailSenderUsername));
        msg.setSubject(title);
        Multipart mp = getContent(content);
        msg.setContent(mp);

        return msg;
    }

    private Multipart getContent(String content) throws MessagingException {

        Multipart mp = new MimeMultipart();
        String htmlBody = "<div>\n" + content + "</div>";
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html");
        mp.addBodyPart(htmlPart);
        return mp;
    }
}
