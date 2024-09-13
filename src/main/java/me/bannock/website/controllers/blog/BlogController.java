package me.bannock.website.controllers.blog;

import jakarta.servlet.http.HttpServletResponse;
import me.bannock.website.services.blog.BlogService;
import me.bannock.website.services.blog.BlogServiceException;
import me.bannock.website.services.blog.Post;
import me.bannock.website.services.user.User;
import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overviewproject.mime_types.GetBytesException;
import org.overviewproject.mime_types.MimeTypeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Date;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    public BlogController(BlogService blogService, UserService userService){
        this.blogService = blogService;
        this.userService = userService;
    }

    private final Logger logger = LogManager.getLogger();
    private final BlogService blogService;
    private final UserService userService;

    @Value("${bannock.blogController.indexCharsetName}")
    private String indexCharsetName;

    @GetMapping("/")
    public String index(){
        // TODO: Blog home page
        return "blog/home";
    }

    @GetMapping("/{postId}")
    public void redirectToPostDir(@PathVariable(value = "postId") long postId, HttpServletResponse response) throws IOException {
        response.sendRedirect("%s/".formatted(postId));
    }

    @GetMapping("/{postId}/")
    public String getPost(@PathVariable(value = "postId") long postId, Model model){
        String indexData;
        Post post;
        try(InputStream postIndexStream = blogService.getIndex(postId)){
            post = blogService.getPost(postId);
            indexData = new String(getBytesFromInputStream(postIndexStream), Charset.forName(indexCharsetName));
        }catch (BlogServiceException e) {
            logger.warn("Something went wrong while fetching post index, requestedPostId={}", postId, e);
            throw new RuntimeException(e);
        }catch (IOException e) {
            logger.error("Something went wrong while reading index input stream, postId={}", postId, e);
            throw new RuntimeException(e);
        }catch (IllegalCharsetNameException e){
            logger.error("The configured charset name is illegal and cannot be used. " +
                    "This may cause the page to display incorrect characters", e);
            throw new RuntimeException(e);
        }

        User author;
        try {
            author = userService.getUserWithId(post.authorId());
        } catch (UserServiceException e) {
            logger.warn("Something went wrong while finding post author, authorId={}, post={}", post.authorId(), post);
            throw new RuntimeException(e);
        }

        logger.info("User requested index for post, postId={}", postId);
        model.addAttribute("post", post);
        model.addAttribute("datePosted", new Date(post.millisPosted()).toString());
        model.addAttribute("author", author);
        model.addAttribute("postIndex", indexData);
        return "blog/post";
    }

    @GetMapping(value = "/{postId}/{assetName}")
    @ResponseBody
    public ResponseEntity<?> getPostAsset(@PathVariable(value = "postId") long postId,
                                                 @PathVariable(value = "assetName") String assetName,
                                                 HttpServletResponse response) throws GetBytesException {
        try{
            InputStream postAssetStream = blogService.getAsset(postId, assetName);

            String contentType = new MimeTypeDetector().detectMimeType(assetName, postAssetStream);
            response.setContentType(contentType);
            logger.info("Using mime type {} for request, postId={}, assetName={}", contentType, postId, assetName);
            logger.info("User requested asset for post, postId={}, assetName={}", postId, assetName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(postAssetStream));
        }catch (BlogServiceException e) {
            logger.warn("Something went wrong while fetching post index, requestedPostId={}", postId, e);
            throw new RuntimeException(e);
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        try(ByteArrayOutputStream streamBytes = new ByteArrayOutputStream()){
            byte[] buffer = new byte[4096];
            int readBytes;
            while (((readBytes = inputStream.read(buffer, 0, buffer.length)) != -1)){
                streamBytes.write(buffer, 0, readBytes);
            }
            return streamBytes.toByteArray();
        }
    }

}