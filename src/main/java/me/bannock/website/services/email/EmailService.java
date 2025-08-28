package me.bannock.website.services.email;

public interface EmailService {

    default void sendEmail(String from, String[] to, String subject,
                           String body, MessageType messageType){
        sendEmail(from, to, new String[0], new String[0], subject, body, messageType);
    }

    void sendEmail(String from, String[] to, String[] ccs, String[] bccs,
                   String subject, String body, MessageType messageType);

}
