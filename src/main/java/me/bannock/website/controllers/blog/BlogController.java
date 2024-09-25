package me.bannock.website.controllers.blog;

import brave.Tracer;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.website.security.Roles;
import me.bannock.website.services.blog.Asset;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private Tracer tracer;

    @Value("${bannock.blogController.indexCharsetName}")
    private String indexCharsetName;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", required = false, defaultValue = "0") int page, Model model){
        List<Post> featuredPosts = blogService.getFeaturedPosts(page);
        Map<Long, User> uidToAuthorsMappings = new HashMap<>();
        for (Post post : featuredPosts){
            if (uidToAuthorsMappings.containsKey(post.authorId()))
                continue;
            try {
                User author = userService.getUserWithId(post.authorId());
                uidToAuthorsMappings.put(post.authorId(), author);
            } catch (UserServiceException e) {
                throw new WrappedBlogServiceException(
                        new BlogServiceException(e.getUserFriendlyError(), e.getMessage()), model);
            }
        }

        model.addAttribute("featuredPosts", featuredPosts);
        model.addAttribute("uidToAuthorsMappings", uidToAuthorsMappings);
        model.addAttribute("nextPageAvailable", !blogService.getFeaturedPosts(page + 1).isEmpty());
        model.addAttribute("blogHeaderTitle", "Featured Posts");
        model.addAttribute("currentPage", page);
        model.addAttribute("isOnHome", true);
        logger.info("User requested featured posts, page={}, featuredPostSize={}", page, featuredPosts.size());
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
            throw new WrappedBlogServiceException(e, model);
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
        model.addAttribute("blogHeaderTitle", post.titlePlaintext());
        model.addAttribute("author", author);
        model.addAttribute("postIndex", indexData);

        StringBuilder seoKeywordsBuilder = new StringBuilder();
        Arrays.stream(post.tags()).forEachOrdered(tag -> seoKeywordsBuilder.append(", ").append(tag));
        model.addAttribute("seoKeywords", seoKeywordsBuilder.substring(2));
        return "blog/post";
    }

    @GetMapping(value = "/{postId}/{assetName}")
    @ResponseBody
    public ResponseEntity<?> getPostAsset(@PathVariable(value = "postId") long postId,
                                          @PathVariable(value = "assetName") String assetName,
                                          HttpServletResponse response, Model model) throws GetBytesException {
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
            throw new WrappedBlogServiceException(e, model);
        }
    }

    @GetMapping("/makePost")
    @Secured(Roles.BlogServiceRoles.MAKE_POSTS)
    public String getMakePost(Model model){
        model.addAttribute("blogHeaderTitle", "Create a new post");
        model.addAttribute("formPojo", new PostFormPojo());
        return "blog/makePost";
    }

    @PostMapping("/makePost")
    @Secured(Roles.BlogServiceRoles.MAKE_POSTS)
    public void postMakePost(HttpServletResponse response, @ModelAttribute PostFormPojo postForm, Model model){
        String[] splitTags = postForm.getTags().split(",");
        for (int i = 0; i < splitTags.length; i++)
            splitTags[i] = splitTags[i].trim();

        Asset indexAsset;
        List<Asset> postAssets = new ArrayList<>();
        try{
            indexAsset = new Asset(postForm.getIndex().getOriginalFilename(), postForm.getIndex().getInputStream());
            for (MultipartFile asset : postForm.getAssets()){
                postAssets.add(new Asset(asset.getOriginalFilename(), asset.getInputStream()));
            }
        } catch (IOException e) {
            logger.warn("User attempted to upload file but something went wrong", e);
            throw new WrappedBlogServiceException(
                    new BlogServiceException("Something went wrong while uploading files"), model);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long authorId;
        try {
            authorId = userService.getUserWithName(auth.getName()).getId();
        } catch (UserServiceException e) {
            throw new RuntimeException(e);
        }

        try {
            Post post = blogService.makePost(postForm.getTitleHtml(), postForm.getTitlePlaintext(), authorId, splitTags,
                    indexAsset, postAssets.toArray(new Asset[0]));
            logger.info("User created new blog post, authorId={}, postId={}", authorId, post.postId());
            response.sendRedirect("%s/".formatted(post.postId()));
        } catch (BlogServiceException e) {
            logger.warn("Something went wrong while attempting to make new blog post, authorId={}", authorId, e);
            throw new WrappedBlogServiceException(e, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(WrappedBlogServiceException.class)
    public ModelAndView handleBlogServiceException(WrappedBlogServiceException blogServiceException){
        ModelAndView model = new ModelAndView("blog/error", blogServiceException.getModel().asMap());
        model.addObject("tId", tracer.currentSpan().context().traceIdString());
        model.addObject("userFriendlyMessage", blogServiceException.getUserFriendlyError());
        return model;
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