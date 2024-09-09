package me.bannock.website.services.blog;

import java.util.Objects;

public record Post(long postId, String titleHtml, long authorId, long millisPosted,
                   String mainBodyFilePath, String... assetFilePaths) {

    /**
     * @param postId The post's unique id
     * @param titleHtml The html to display in the title of the post. Must be less than or equal to 256 characters long
     * @param authorId The user id of the post's author
     * @param millisPosted The time and date the post was made in milliseconds
     * @param mainBodyFilePath The path to the post's index.html file
     * @param assetFilePaths The paths to any assets used by the index.html file
     */
    public Post {
        Objects.requireNonNull(titleHtml);
        Objects.requireNonNull(mainBodyFilePath);
        if (assetFilePaths == null)
            assetFilePaths = new String[0];
        if (titleHtml.length() > 256)
            throw new IllegalArgumentException("Title HTML must be less than or equal to 256 characters long");
    }

}
