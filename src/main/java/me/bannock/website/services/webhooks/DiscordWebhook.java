package me.bannock.website.services.webhooks;

public class DiscordWebhook {

    public DiscordWebhook(String content, DiscordEmbed... embeds){
        this.content = content;
        this.embeds = embeds;
    }

    private String content;
    private DiscordEmbed[] embeds;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DiscordEmbed[] getEmbeds() {
        return embeds;
    }

    public void setEmbeds(DiscordEmbed[] embeds) {
        this.embeds = embeds;
    }

}
