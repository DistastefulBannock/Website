package me.bannock.website.services.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
public class AwsSesEmailServiceImpl implements EmailService {

    @Autowired
    public AwsSesEmailServiceImpl(SesClient sesClient){
        this.sesClient = sesClient;
    }

    private final SesClient sesClient;

    @Override
    public void sendEmail(String from, String[] to, String[] ccs,
                          String[] bccs, String subject, String body,
                          MessageType messageType) {
        Destination destination = Destination.builder().toAddresses(to).ccAddresses(ccs).bccAddresses(bccs).build();

        Content bodyContent = Content.builder().data(body).build();
        Content subjectContent = Content.builder().data(subject).build();

        Body awsBody;
        if (messageType == MessageType.PLAIN_TEXT){
            awsBody = Body.builder().text(bodyContent).build();
        }else{
            awsBody = Body.builder().html(bodyContent).build();
        }

        Message message = Message.builder().body(awsBody).subject(subjectContent).build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(from)
                .build();
        sesClient.sendEmail(sendEmailRequest);
    }

}
