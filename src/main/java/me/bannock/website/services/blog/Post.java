package me.bannock.website.services.blog;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public record Post(long postId, String titleHtml, String titlePlaintext,
                   long authorId, long millisPosted, String[] tags,
                   String[] assetFilePaths, boolean deleted) {

    /**
     * @param postId The post's unique id
     * @param titleHtml The html to display in the title of the post. Must be less than or equal to 256 characters long
     * @param titlePlaintext The title to display on the post page. Must be less than or equal to 256 characters long
     * @param authorId The user id of the post's author
     * @param tags The post's tags. Used by users for searching posts and topics
     * @param millisPosted The time and date the post was made in milliseconds
     * @param assetFilePaths The paths to any assets used by the index.html file
     */
    public Post {
        Objects.requireNonNull(titleHtml);
        Objects.requireNonNull(titlePlaintext);
        if (tags == null)
            tags = new String[0];
        if (assetFilePaths == null)
            assetFilePaths = new String[0];
        if (titleHtml.length() > 256)
            throw new IllegalArgumentException("Title HTML must be less than or equal to 256 characters long");
        if (titlePlaintext.length() > 256)
            throw new IllegalArgumentException("Plaintext title must be less than or equal to 256 characters long");
    }

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
        Post post = (Post) o;
        return postId == post.postId && authorId == post.authorId && millisPosted == post.millisPosted && Objects.equals(titleHtml, post.titleHtml) && Objects.deepEquals(assetFilePaths, post.assetFilePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, titleHtml, authorId, millisPosted, Arrays.hashCode(assetFilePaths));
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId=" + postId +
                ", titleHtml='" + titleHtml + '\'' +
                ", authorId=" + authorId +
                ", millisPosted=" + millisPosted +
                ", assetFilePaths=" + Arrays.toString(assetFilePaths) +
                ", deleted=" + deleted +
                '}';
    }

}
