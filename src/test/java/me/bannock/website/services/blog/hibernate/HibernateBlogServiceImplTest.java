package me.bannock.website.services.blog.hibernate;

import me.bannock.website.security.Roles;
import me.bannock.website.services.blog.Asset;
import me.bannock.website.services.blog.BlogServiceException;
import me.bannock.website.services.blog.Comment;
import me.bannock.website.services.blog.Post;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HibernateBlogServiceImplTest {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private HibernateBlogServiceImpl hibernateBlogService;

    @Test
    @WithMockUser(username = "test", authorities = {
            Roles.BlogServiceRoles.MAKE_POSTS, Roles.BlogServiceRoles.READ_POSTS,
            Roles.StorageServiceRoles.SAVE_DATA, Roles.StorageServiceRoles.LOAD_DATA
    })
    public void makeAndGetPostTest() throws IOException, BlogServiceException {
        makeAndGetPost();
    }

    @WithMockUser(username = "test", authorities = {
            Roles.BlogServiceRoles.MAKE_POSTS, Roles.BlogServiceRoles.READ_POSTS,
            Roles.StorageServiceRoles.SAVE_DATA, Roles.StorageServiceRoles.LOAD_DATA
    })
    public Post makeAndGetPost() throws IOException, BlogServiceException {
        String myCoolAssetName = "2024-09-02_15.37.12.png";
        byte[] coolAssetBytes;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(ImageIO.read(HibernateBlogServiceImplTest.class.getClassLoader().getResourceAsStream("hibernateBlogServiceImplTest/%s".formatted(myCoolAssetName))), "png", baos);
            coolAssetBytes = baos.toByteArray();
        }

        byte[] coolScriptBytes = "console.log('Awesome script loaded D)');".getBytes(StandardCharsets.UTF_8);
        String coolScriptName = "coolScript.js";

        byte[] indexBytes = "This is my awesome post! Thank you for viewing it! <img src=\"%s\"/><script src=\"%s\" type=\"text/javascript\"></script>"
                .formatted(myCoolAssetName, coolScriptName).getBytes(StandardCharsets.UTF_8);
        Post post = hibernateBlogService.makePost(
                "<h1>My awesome test post!</h1>",
                "My awesome text post!",
                0,
                new String[]{"Awesome", "Testing", "Cool"},
                new Asset("index.html", new ByteArrayInputStream(indexBytes)),
                new Asset(myCoolAssetName, new ByteArrayInputStream(coolAssetBytes)),
                new Asset(coolScriptName, new ByteArrayInputStream(coolScriptBytes)));
        assertNotNull(post);
        assertEquals(post, hibernateBlogService.getPost(post.postId()));
        assertArrayEquals(indexBytes, hibernateBlogService.getIndex(post.postId()).readAllBytes());
        assertArrayEquals(coolAssetBytes, hibernateBlogService.getAsset(post.postId(), myCoolAssetName).readAllBytes());
        logger.info("Created test post, post={}", post);
        return post;
    }

    @Test
    @Disabled
    @WithMockUser(username = "test", authorities = {
            Roles.BlogServiceRoles.MAKE_POSTS, Roles.BlogServiceRoles.READ_POSTS,
            Roles.StorageServiceRoles.SAVE_DATA, Roles.StorageServiceRoles.LOAD_DATA
    })
    public void makeABunchOfPosts() throws IOException, BlogServiceException {
        for (int i = 0; i < 100; i++)
            makeAndGetPost();
    }

    @Test
    @WithMockUser(username = "test", authorities = {
            Roles.BlogServiceRoles.MAKE_POSTS, Roles.BlogServiceRoles.READ_POSTS,
            Roles.BlogServiceRoles.MAKE_COMMENTS, Roles.BlogServiceRoles.READ_COMMENTS,
            Roles.StorageServiceRoles.SAVE_DATA, Roles.StorageServiceRoles.LOAD_DATA
    })
    public void makePostAndTestComments() throws IOException, BlogServiceException {
        Post post = hibernateBlogService.getPost(0);
        Comment comment = hibernateBlogService.makeComment(post.postId(), 0,
                "This is an awesome test comment!!!", "localhost");
        int lastPage = hibernateBlogService.getCommentsTotalPages(post.postId()) - 1;
        List<Comment> lastPagePostComments = hibernateBlogService.getComments(post.postId(), lastPage);
        assertTrue(lastPagePostComments.stream().anyMatch(comment::equals));
    }

}