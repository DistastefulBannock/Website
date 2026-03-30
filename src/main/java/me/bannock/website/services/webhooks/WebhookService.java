package me.bannock.website.services.webhooks;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface WebhookService {

    @Async
    void sendNotification(String title, String message, Map<String, String> details);

}
