package me.bannock.website.services.blog.hibernate;

import me.bannock.website.security.Roles;
import me.bannock.website.services.blog.Asset;
import me.bannock.website.services.blog.BlogServiceException;
import me.bannock.website.services.blog.Post;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HibernateBlogServiceImplTest {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private HibernateBlogServiceImpl hibernateBlogService;

    @Test
    @WithMockUser(username = "test", authorities = {
            Roles.BlogServiceRoles.MAKE_POSTS, Roles.BlogServiceRoles.READ_POSTS,
            Roles.StorageServiceRoles.SAVE_DATA
    })
    public void makeAndGetPost() throws IOException, BlogServiceException {
        String myCoolAssetName = "2024-09-02_15.37.12.png";
        byte[] coolAssetBytes;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(ImageIO.read(HibernateBlogServiceImplTest.class.getClassLoader().getResourceAsStream("hibernateBlogServiceImplTest/%s".formatted(myCoolAssetName))), "png", baos);
            coolAssetBytes = baos.toByteArray();
        }
        byte[] indexBytes = "This is my awesome post! Thank you for viewing it! <img src=\"assets/%s\"/>"
                .formatted(myCoolAssetName).getBytes(StandardCharsets.UTF_8);
        Post post = hibernateBlogService.makePost(
                "<h1>My awesome test post!</h1>",
                0,
                new Asset("index.html", new ByteArrayInputStream(indexBytes)),
                new Asset("assets/%s".formatted(myCoolAssetName), new ByteArrayInputStream(coolAssetBytes)));
        assertNotNull(post);
        assertEquals(post, hibernateBlogService.getPost(post.postId()));
        logger.info("Created post, post={}", post);
    }

}