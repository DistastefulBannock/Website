package me.bannock.website.model.header;

public class HeaderLink {

    public HeaderLink(String displayText, String redirect, int width, int height) {
        this.displayText = displayText;
        this.redirect = redirect;
        this.width = width;
        this.height = height;
    }

    private final String displayText, redirect;
    private final int width, height;

    public String getDisplayText() {
        return displayText;
    }

    public String getRedirect() {
        return redirect;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "HeaderLink{" +
                "displayText='" + displayText + '\'' +
                ", redirect='" + redirect + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

}
