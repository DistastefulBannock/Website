package me.bannock.website.services.webhooks;

public class DiscordField {

    public DiscordField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name, value;
    private boolean inline = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

}
