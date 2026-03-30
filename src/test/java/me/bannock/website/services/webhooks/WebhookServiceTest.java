package me.bannock.website.services.webhooks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class WebhookServiceTest {

    @Autowired
    private WebhookService webhookService;

    @Test
    public void test(){
        webhookService.sendNotification("", "", Map.of("Detail 1", "Detail 2", "Detail 2", "Detail 2", "Detail 3", "Detail 2",
                "asdasdasdasdasdasdasDetail1", "Detail 2", "Detail2", "Detail 2", "Detail3", "Detaasdadsadasdasdasil 2", "Detasdail1", "Detail 2", "Detailasd2",
                "Detail 2", "Detaiasdl3", "Detasdasdnasbdasjhdbkjashdkjashdkjsadail 2"));
    }

}