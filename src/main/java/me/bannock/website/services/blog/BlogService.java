package me.bannock.website.services.blog;

import me.bannock.website.security.Roles;
import org.springframework.security.access.annotation.Secured;

import java.io.InputStream;
import java.util.List;

public interface BlogService {

    /**
     * @param postId The post's id
     * @return The post
     * @throws BlogServiceException If something goes wrong while getting the post
     */
    @Secured(Roles.BlogServiceRoles.READ_POSTS)
    Post getPost(long postId) throws BlogServiceException;

    /**
     * Gets a steam of data for a given post's index file; the one shown by default when the post is displayed
     * @param postId The post id the index is for
     * @return An input stream containing the data for the asset
     * @throws BlogServiceException If something goes wrong while getting the asset
     */
    @Secured(Roles.BlogServiceRoles.READ_POSTS)
    InputStream getIndex(long postId) throws BlogServiceException;

    /**
     * Gets a steam of data for a given asset
     * @param postId The post id the asset is related to
     * @param assetPath The path to the asset
     * @return An input stream containing the data for the asset
     * @throws BlogServiceException If something goes wrong while getting the asset
     */
    @Secured(Roles.BlogServiceRoles.READ_POSTS)
    InputStream getAsset(long postId, String assetPath) throws BlogServiceException;

    /**
     * Attempt to make a new post
     * @param titleHtml The html to display in the title of the post. Must be less than or equal to 256 characters long
     * @param titlePlaintext The text to display as the title on the post page. Must be less than or equal to 256 chars long
     * @param authorId The author of the post
     * @param tags The tags the post should have. Used by users for searching post topics.
     * @param index The index file of the post; what is shown to the user when first opened
     * @param assets Any asset files used by the post
     * @return The post
     * @throws BlogServiceException If something went wrong while creating the post
     */
    @Secured(Roles.BlogServiceRoles.MAKE_POSTS)
    Post makePost(String titleHtml, String titlePlaintext, long authorId,
                  String[] tags, Asset index, Asset... assets) throws BlogServiceException;

    /**
     * @param page The page number
     * @return The featured posts to display on the blog home page
     */
    @Secured(Roles.BlogServiceRoles.READ_POSTS)
    List<Post> getFeaturedPosts(int page);

    /**
     * @return The total amount of pages of featured posts
     */
    @Secured(Roles.BlogServiceRoles.READ_POSTS)
    int getFeaturePostTotalPages();

    /**
     * Gets some of the comments for a given post
     * @param postId The post id to get the comments for
     * @param page The page of comments to get; starts at 0
     * @return An array containing a list of the comments for the specified page for the post
     * @throws BlogServiceException If something goes wrong while getting the comments
     */
    @Secured(Roles.BlogServiceRoles.READ_COMMENTS)
    Comment[] getComments(long postId, int page) throws BlogServiceException;

    /**
     * Creates a new comment under a given post
     * @param postId The post to make the comment under
     * @param commentAuthorId The user id of the user making the comment
     * @param content The content to store in the comment
     * @return The created comment
     * @throws BlogServiceException If something goes wrong while making the comment
     */
    @Secured(Roles.BlogServiceRoles.MAKE_COMMENTS)
    Comment makeComment(long postId, long commentAuthorId, String content) throws BlogServiceException;

}
