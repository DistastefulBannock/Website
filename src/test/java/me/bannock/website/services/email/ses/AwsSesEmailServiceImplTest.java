package me.bannock.website.services.email.ses;

import me.bannock.website.services.email.EmailService;
import me.bannock.website.services.email.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AwsSesEmailServiceImplTest {

    @Autowired
    public EmailService emailService;

    @Test
    public void sendEmailTest(){
        Assertions.assertDoesNotThrow(() -> {
            emailService.sendEmail("admin@bannock.me", new String[]{"DistastefulBannock@gmail.com"},
                    "Test email", "Test email 2", MessageType.PLAIN_TEXT);
        });
    }

}
