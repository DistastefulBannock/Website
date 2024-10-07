package me.bannock.website.controllers.blog;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class PostFormPojo {

    private String titleHtml, titlePlaintext;

    /**
     * Tags for the post split with comma delimiters
     */
    private String tags;
    private MultipartFile index;
    private MultipartFile[] assets;

    public String getTitleHtml() {
        return titleHtml;
    }

    public void setTitleHtml(String titleHtml) {
        this.titleHtml = titleHtml;
    }

    public String getTitlePlaintext() {
        return titlePlaintext;
    }

    public void setTitlePlaintext(String titlePlaintext) {
        this.titlePlaintext = titlePlaintext;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tagsSplit) {
        this.tags = tagsSplit;
    }

    public MultipartFile getIndex() {
        return index;
    }

    public void setIndex(MultipartFile index) {
        this.index = index;
    }

    public MultipartFile[] getAssets() {
        return assets;
    }

    public void setAssets(MultipartFile[] assets) {
        this.assets = assets;
    }

    @Override
    public String toString() {
        return "PostFormPojo{" +
                "titleHtml='" + titleHtml + '\'' +
                ", titlePlaintext='" + titlePlaintext + '\'' +
                ", tagsSplit='" + tags + '\'' +
                ", index=" + index +
                ", assets=" + Arrays.toString(assets) +
                '}';
    }

}
