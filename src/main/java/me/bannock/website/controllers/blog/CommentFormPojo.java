package me.bannock.website.controllers.blog;

import org.springframework.lang.Nullable;

import java.io.Serializable;

public class CommentFormPojo implements Serializable {

    private static final long serialVersionUID = 3621L;

    private @Nullable String dummyAccountEmailAddress;
    private @Nullable String dummyAccountUsername;
    private String content;
    private long postId;

    @Nullable
    public String getDummyAccountEmailAddress() {
        return dummyAccountEmailAddress;
    }

    public void setDummyAccountEmailAddress(@Nullable String dummyAccountEmailAddress) {
        this.dummyAccountEmailAddress = dummyAccountEmailAddress;
    }

    @Nullable
    public String getDummyAccountUsername() {
        return dummyAccountUsername;
    }

    public void setDummyAccountUsername(@Nullable String dummyAccountUsername) {
        this.dummyAccountUsername = dummyAccountUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "CommentFormPojo{" +
                "dummyAccountEmailAddress='" + dummyAccountEmailAddress + '\'' +
                ", dummyAccountUsername='" + dummyAccountUsername + '\'' +
                ", content='" + content + '\'' +
                ", postId=" + postId +
                '}';
    }

}
