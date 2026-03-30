package me.bannock.website.services.webhooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Map;

@Service
public class DiscordWebhookServiceImpl implements WebhookService {

    public DiscordWebhookServiceImpl(@Value("${bannock.analytics.aboutPageWebhook}") String aboutPageWebhook){
        this.restClient = RestClient.builder()
                .baseUrl(aboutPageWebhook)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private final RestClient restClient;
    private final Logger logger = LogManager.getLogger();

    @Override
    public void sendNotification(String title, String message, Map<String, String> details) {
        ArrayList<DiscordField> detailsFields = new ArrayList<>();
        for (String key : details.keySet()){
            if (detailsFields.size() >= 25) {
                logger.error("Had to truncate fields in webhook notification due to hitting max supported, " +
                        "fields={}, maxFields=25", details.size());
                break;
            }
            detailsFields.add(new DiscordField(key, details.get(key)));
        }
        DiscordField[] discordFields = detailsFields.toArray(new DiscordField[0]);

        DiscordWebhook discordWebhook = new DiscordWebhook("", new DiscordEmbed(title, message, discordFields));
        String webhookJson = new JSONObject(discordWebhook).toString();
        ResponseEntity response = restClient.post().body(webhookJson).retrieve().toBodilessEntity();
        if (response.getStatusCode() != HttpStatus.NO_CONTENT){
            logger.error("Something went wrong while sending webhook request, responseBody=\"{}\"", response.getBody());
        }
    }

}
