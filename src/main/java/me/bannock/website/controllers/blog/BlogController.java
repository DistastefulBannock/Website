package me.bannock.website.controllers.blog;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import me.bannock.website.controllers.ControllerUtils;
import me.bannock.website.security.Roles;
import me.bannock.website.security.authentication.UserDetailsImpl;
import me.bannock.website.services.blog.Asset;
import me.bannock.website.services.blog.BlogService;
import me.bannock.website.services.blog.BlogServiceException;
import me.bannock.website.services.blog.Comment;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
import java.util.Objects;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    public BlogController(BlogService blogService, UserService userService, ObjectMapper objectMapper){
        this.blogService = blogService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    private final Logger logger = LogManager.getLogger();
    private final BlogService blogService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    private Tracer tracer;

    @Value("${bannock.blogController.indexCharsetName}")
    private String indexCharsetName;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", required = false, defaultValue = "0") int page, Model model){
        List<Post> featuredPosts = new ArrayList<>(blogService.getFeaturedPosts(page));

        Authentication auth = ControllerUtils.getAuthNoAnon();
        long loggedInUid = -1L;
        if (auth != null){
            try{
                loggedInUid = userService.getUserWithName(auth.getName()).getId();
            }catch (UserServiceException e){
                logger.warn(e.getUserFriendlyError(), e);
            }
        }
        Map<Long, User> uidToAuthorsMappings = new HashMap<>();
        List<Post> postsToRemove = new ArrayList<>();
        for (Post post : featuredPosts){
            if (uidToAuthorsMappings.containsKey(post.authorId()))
                continue;
            try {
                User author = userService.getUserWithId(post.authorId());
                if (author.getId() != loggedInUid && author.isShadowBanned()){
                    logger.warn("Removing post from page because the author is shadow banned, postId={}, author={}",
                            post.postId(), author);
                    postsToRemove.add(post);
                    continue;
                }
                uidToAuthorsMappings.put(post.authorId(), author);
            } catch (UserServiceException e) {
                throw new WrappedBlogServiceException(
                        new BlogServiceException(e.getUserFriendlyError(), e.getMessage()), model);
            }
        }
        featuredPosts.removeAll(postsToRemove);

        model.addAttribute("featuredPosts", featuredPosts);
        model.addAttribute("uidToAuthorsMappings", uidToAuthorsMappings);
        model.addAttribute("totalPages", blogService.getFeaturedPostsTotalPages());
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
    public String getPost(@PathVariable(name = "postId") long postId,
                          @RequestParam(name = "commentPage", required = false, defaultValue = "-1") int commentPage,
                          @ModelAttribute(name = "commentFormErrorMessage") String commentFormErrorMessage,
                          @ModelAttribute(name = "commentFormPojo") CommentFormPojo commentForm,
                          Model model){
        String indexData;
        Post post;
        try(InputStream postIndexStream = blogService.getIndex(postId)){
            post = blogService.getPost(postId);
            if (post.deleted()){
                logger.warn("Could not get post because it has been deleted, postId={}", postId);
                throw new WrappedBlogServiceException(new BlogServiceException(
                        "Can not get post because it has been deleted", "Post has been deleted"), model);
            }

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

        Authentication auth = ControllerUtils.getAuthNoAnon();
        long loggedInUid = -1L;
        if (auth != null){
            try{
                loggedInUid = userService.getUserWithName(auth.getName()).getId();
            }catch (UserServiceException e){
                logger.warn("Something went wrong while getting logged in user id", e);
            }
        }
        User author;
        try {
            author = userService.getUserWithId(post.authorId());
            if (loggedInUid != author.getId() && author.isShadowBanned()){
                logger.warn("Could not get post because the author is shadow banned, postId={}, author={}",
                        postId, author);
                throw new WrappedBlogServiceException(
                        new BlogServiceException(
                                "Something went wrong while getting the post",
                                "Could not access post because the author is shadow banned"),
                        model);
            }
        } catch (UserServiceException e) {
            logger.warn("Something went wrong while finding post author, authorId={}, post={}", post.authorId(), post);
            throw new RuntimeException(e);
        }

        logger.info("User requested index for post, postId={}", postId);
        model.addAttribute("post", post);
        model.addAttribute("blogHeaderTitle", post.titlePlaintext());
        model.addAttribute("author", author);
        model.addAttribute("postIndex", indexData);

        try {
            int lastCommentPage = blogService.getCommentsTotalPages(postId) - 1;
            if (commentPage < 0)
                commentPage = lastCommentPage;
            commentPage = Math.max(0, commentPage);
            List<Comment> comments = blogService.getComments(postId, commentPage);

            List<Long> commentAuthorIds = new ArrayList<>();
            comments.forEach(comment -> commentAuthorIds.add(comment.authorId()));
            List<User> commentAuthors = userService.getManyUsersWithIds(commentAuthorIds);
            Map<Long, User> uidToUserMappings = new HashMap<>();
            commentAuthors.forEach(user -> uidToUserMappings.put(user.getId(), user));

            List<Comment> filteredComments = new ArrayList<>();
            for (Comment comment : comments){
                if (!uidToUserMappings.containsKey(comment.authorId())){
                    logger.warn("Excluded comment from the post comments because their author could not be found, " +
                            "commentAuthorId={}, postId={}", comment.authorId(), postId);
                    continue;
                }
                User commentAuthor = uidToUserMappings.get(comment.authorId());
                if (loggedInUid != commentAuthor.getId() && commentAuthor.isShadowBanned()){
                    logger.warn("Excluded comment from the post comments because the the comment's " +
                            "author is shadow banned commentAuthorId={}, postId={}", comment.authorId(), postId);
                    continue;
                }
                filteredComments.add(comment);
            }

            if (commentFormErrorMessage.isBlank())
                model.addAttribute("commentFormErrorMessage", null);
            model.addAttribute("comments", filteredComments);
            model.addAttribute("commentAuthorIdsToUserMappings", uidToUserMappings);
            model.addAttribute("currentCommentPage", commentPage);
            model.addAttribute("lastCommentPage", lastCommentPage);
            if (!model.containsAttribute("commentFormPojo")){
                CommentFormPojo commentFormPojo = new CommentFormPojo();
                commentFormPojo.setPostId(postId);
                model.addAttribute("commentFormPojo", commentFormPojo);
            }
        } catch (BlogServiceException e) {
            logger.warn("Something went wrong while attempting to get blog post comments, post={}", post, e);
            throw new WrappedBlogServiceException(e, model);
        }

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
            Post post = blogService.getPost(postId);
            if (post.deleted())
                throw new WrappedBlogServiceException(
                        new BlogServiceException("Can not get asset because the post has been deleted"), model);

            InputStream postAssetStream = blogService.getAsset(postId, assetName);
            String contentType = new MimeTypeDetector().detectMimeType(assetName, postAssetStream);
            response.setContentType(contentType);
            logger.info("Using mime type {} for request, postId={}, assetName={}", contentType, postId, assetName);
            logger.info("User requested asset for post, postId={}, assetName={}", postId, assetName);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "; filename=\"%s\"".formatted(assetName))
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(postAssetStream));
        }catch (BlogServiceException e) {
            logger.warn("Something went wrong while fetching post asset, requestedPostId={}, assetName={}",
                    postId, assetName, e);
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
    public void postMakePost(HttpServletRequest request, HttpServletResponse response,
                             @ModelAttribute PostFormPojo postForm, Model model){
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

        Authentication auth = ControllerUtils.getAuthNoAnon();
        if (auth == null){
            logger.warn("User attempted to make post but was not authenticated, " +
                    "postForm={}, remoteIp={}", postForm, request.getRemoteAddr());
            throw new RuntimeException("You must be authorized to make this request");
        }
        long authorId;
        try {
            User author = userService.getUserWithName(auth.getName());
            if (author.isAccountDisabled()){
                logger.warn("User could not make blog post because their account is disabled, author={}", author);
                throw new WrappedBlogServiceException(new BlogServiceException(
                        "Could not make post because your account is disabled"), model);
            }
            authorId = author.getId();
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

    @PostMapping("/deletePost")
    public void postDeletePost(HttpServletResponse response, @RequestParam(value = "postId") long postId, Model model) throws IOException {
        try {
            blogService.deletePost(postId);
        } catch (BlogServiceException e) {
            logger.warn("Something went wrong while deleting a post, postId={}", postId, e);
            throw new WrappedBlogServiceException(e, model);
        }
        logger.info("Successfully deleted post, postId={}", postId);
        response.sendRedirect("");
    }

    @PostMapping("/makeComment")
    @ResponseBody
    public void postMakeComment(HttpServletResponse response, HttpServletRequest request,
                                @ModelAttribute CommentFormPojo commentForm, Model model)
            throws IOException, CommentFormException {
        long authorId = getAuthorForNewComment(request.getSession(true), request.getRemoteAddr(),
                model, commentForm);
        Comment comment;
        try {
            comment = blogService.makeComment(commentForm.getPostId(), authorId, commentForm.getContent(), request.getRemoteAddr());
        } catch (BlogServiceException e) {
            throw new CommentFormException(e.getUserFriendlyError(), e.getMessage(), model, commentForm);
        }

        logger.info("User made new comment, comment={}", comment);
        response.sendRedirect("%s/#commentBox".formatted(commentForm.getPostId()));
    }

    /**
     * Gets or creates the user account needed to post a comment and returns their id
     * @param commentForm The submitted comment form
     * @param authorIp The comment author's ip address
     * @param model The model
     * @return The author's user account id
     * @throws CommentFormException If something goes wrong while getting or creating the author's account
     */
    private long getAuthorForNewComment(HttpSession session, String authorIp, Model model, CommentFormPojo commentForm) throws CommentFormException{
        Authentication auth = ControllerUtils.getAuthNoAnon();
        if (auth != null && auth.isAuthenticated()){
            try{
                User user = userService.getUserWithName(auth.getName());
                if (user.isAccountDisabled()){
                    throw new CommentFormException("Could not post comment because your account is disabled",
                            "Could not post comment because the user's account is disabled",
                            model, commentForm);
                }
                return user.getId();
            }catch (UserServiceException e){
                logger.warn("Failed to get user account using session for comment, " +
                        "one may be created later in the flow", e);
            }
        }

        User user = null;
        Objects.requireNonNull(commentForm.getDummyAccountEmailAddress());
        if (!commentForm.getDummyAccountEmailAddress().isBlank()){
            try {
                user = userService.getUserWithEmail(commentForm.getDummyAccountEmailAddress());
                if (!user.isUnclaimedAccount())
                    throw new CommentFormException("Please login to your account using your password",
                            "User could not make comment because their account is claimed", model, commentForm);
                if (user.isAccountDisabled())
                    throw new CommentFormException("Unable to post comment because your account has been disabled",
                            "User could not make comment because their account is disabled", model, commentForm);
            } catch (UserServiceException e) {
                logger.warn("Failed to get user account for comment using provided email, " +
                        "one may be created later in the flow", e);
            }
        }
        if (user == null){
            if (commentForm.getDummyAccountUsername().isBlank()){
                throw new CommentFormException("Please provide a username in the form to post comments",
                        model, commentForm);
            }
            try {
                user = userService.registerDummyUser(commentForm.getDummyAccountUsername(),
                        commentForm.getDummyAccountEmailAddress(), authorIp);
            } catch (UserServiceException e) {
                throw new CommentFormException(e.getUserFriendlyError(), e.getMessage(), model, commentForm);
            }
        }

        if (user == null){
            throw new CommentFormException("Failed to create user to post comment with. Please try again later.", "Something went wrong",
                    model, commentForm);
        }
        Authentication newAuth = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(user), user.getPassword(),
                user.getRoles().stream().map(SimpleGrantedAuthority::new).toList());
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(newAuth);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return user.getId();
    }

    @ExceptionHandler(CommentFormException.class)
    public RedirectView handleCommentFormException(HttpServletResponse response,
                                                   RedirectAttributes redirectAttributes,
                                                   CommentFormException commentFormException) throws IOException {
        redirectAttributes.addFlashAttribute("commentFormPojo", commentFormException.getCommentFormPojo());
        redirectAttributes.addAttribute("commentFormErrorMessage", commentFormException.getUserFriendlyMessage());
        logger.warn("User attempted to submit form but something went wrong", commentFormException);
        return new RedirectView("%s/#commentBox".formatted(commentFormException.getPostId()), true);
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