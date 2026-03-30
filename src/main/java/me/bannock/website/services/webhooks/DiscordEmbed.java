package me.bannock.website.services.webhooks;

public class DiscordEmbed {

    public DiscordEmbed(String title, String description, DiscordField... fields){
        this.title = title;
        this.description = description;
        this.fields = fields;
    }

    private String title, type = "rich", description, url;
    private DiscordField[] fields;
    private int color = 0xEB9300;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DiscordField[] getFields() {
        return fields;
    }

    public void setFields(DiscordField[] fields) {
        this.fields = fields;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
