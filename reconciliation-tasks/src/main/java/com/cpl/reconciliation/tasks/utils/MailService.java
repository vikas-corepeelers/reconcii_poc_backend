package com.cpl.reconciliation.tasks.utils;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MailService {

//    public void fetchEmails(Properties properties,
//                            String host, String username,
//                            String password, String protocol, SearchTerm searchTerm,
//                            MessageProcessor messageProcessor) throws Exception {
//
//        Session session = Session.getDefaultInstance(properties);
//        Store store = session.getStore(protocol);
//        store.connect(host,993, username, password);
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_WRITE);
//        Message[] messages = inbox.search(searchTerm);
//        messageProcessor.processMessages(messages);
//        inbox.close(false);
//        store.close();
//    }

    public void sendMail(String recipientEmail, String subject, String senderEmail, String username, String password, Properties props, String htmlContent) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}