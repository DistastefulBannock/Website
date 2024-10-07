package me.bannock.website.services.blog;

import java.util.Date;
import java.util.Objects;

public record Comment(long postId, long authorId, long millisPosted, String content) {

    /**
     * @return The time and date the post was made formatted in a string
     */
    public String getFormattedPostDate(){
        return new Date(millisPosted()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return postId == comment.postId && authorId == comment.authorId && millisPosted == comment.millisPosted && Objects.equals(content, comment.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, authorId, millisPosted, content);
    }
}
