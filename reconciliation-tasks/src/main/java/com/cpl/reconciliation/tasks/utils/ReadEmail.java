package com.cpl.reconciliation.tasks.utils;

import javax.mail.*;
import java.util.Properties;

public class ReadEmail {

    private static void readEmail() {
        // Set your email credentials and server details
        String username = "reconciliation.cprl@del.in.mcd.com";
        String password = "Hello1291rec@rec";
        String host = "mymail.mcd.com"; // IMAP server address
        int port = 993; // IMAP server port
        // Set up JavaMail properties
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imap");
        properties.setProperty("mail.imap.ssl.enable", "true");
        // Connect to the IMAP server
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        try {
            // Create a Store object and connect to the server
            Store store = session.getStore();
            store.connect(host, port, username, password);
            // Open the INBOX folder
            javax.mail.Folder inbox = store.getFolder("Inbox");
            inbox.open(javax.mail.Folder.READ_ONLY);
            // Fetch messages from the inbox
            Message[] messages = inbox.getMessages();
            System.out.println("Message size:"+ messages.length);
            // Print details of each message
            for (Message message : messages) {
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Sent Date: " + message.getSentDate());
                System.out.println("Content: " + message.getContent());
                System.out.println("--------------------------------------");
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
