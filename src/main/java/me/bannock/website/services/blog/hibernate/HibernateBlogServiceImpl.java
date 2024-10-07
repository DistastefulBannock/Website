package me.bannock.website.services.blog.hibernate;

import me.bannock.website.services.blog.Asset;
import me.bannock.website.services.blog.BlogService;
import me.bannock.website.services.blog.BlogServiceException;
import me.bannock.website.services.blog.Comment;
import me.bannock.website.services.blog.Post;
import me.bannock.website.services.storage.StorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class HibernateBlogServiceImpl implements BlogService {

    @Autowired
    public HibernateBlogServiceImpl(StorageService storageService, PostRepository postRepository,
                                    CommentRepository commentRepository){
        this.storageService = storageService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    private final Logger logger = LogManager.getLogger();

    private final StorageService storageService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Value("${bannock.hibernateBlog.persistOriginalFileNames}")
    private boolean persistOriginalFileNames;
    @Value("${bannock.hibernateBlog.commentPostingEnabled}")
    private boolean commentPostingEnabled;
    @Value("${bannock.blog.featuredPageSize}")
    private int featuredPageSize = 10;
    @Value("${bannock.blog.commentPageSize}")
    private int commentPageSize = 25;
    @Value("${bannock.blog.maxCommentContentSize}")
    private int maxCommentContentSize = 2048;
    @Value("${bannock.blog.maxCommentNewlineCount}")
    private int maxCommentNewlineCount = 15;
    @Value("${bannock.hibernateBlog.commentContentLogCharacterLimit}")
    private int commentContentLogCharacterLimit = 256;

    @Override
    @Transactional(readOnly = true)
    public Post getPost(long postId) throws BlogServiceException {
        PostEntity post = getPostEntity(postId);
        logger.info("Found post with post id, postId={}", postId);
        return toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream getIndex(long postId) throws BlogServiceException {
        PostEntity post = getPostEntity(postId);
        InputStream indexInputStream;
        try {
            indexInputStream = storageService.load(getStorageCategoryForPost(postId), post.getIndexPath());
        } catch (IOException e) {
            logger.error("Failed to load blog post index, postId={}", postId);
            throw new RuntimeException(e);
        }
        return indexInputStream;
    }

    @Override
    public InputStream getAsset(long postId, String assetPath) throws BlogServiceException {
        if (!postRepository.existsById(postId)){
            logger.warn("Could not get post asset because post does not exist, postId={}", postId);
            throw new BlogServiceException("Post does not exist");
        }
        InputStream assetInputStream;
        try {
            assetInputStream = storageService.load(getStorageCategoryForPostAssets(postId), assetPath);
        } catch (IOException e) {
            logger.warn("Failed to load blog post asset, postId={}, assetPath={}", postId, assetPath);
            throw new BlogServiceException("Something went wrong while loading asset", e.getMessage());
        }
        return assetInputStream;
    }

    @Override
    @Transactional
    public Post makePost(String titleHtml, String titlePlaintext, long authorId, String[] tags, Asset index, Asset... assets) throws BlogServiceException {
        Objects.requireNonNull(titleHtml);
        Objects.requireNonNull(index);
        if (assets == null)
            assets = new Asset[0];
        if (titleHtml.length() > 256){
            logger.warn("Failed to create new blog post because title length is more than 256 " +
                    "characters long, authorId={} title=\"{}\"", authorId, titleHtml);
            throw new BlogServiceException("Title html must not be more than 256 characters long");
        }

        if (!persistOriginalFileNames){
            replaceAssetFileNames(assets);
            Asset[] indexArray = new Asset[]{index};
            replaceAssetFileNames(indexArray);
            index = indexArray[0];
            logger.info("Persist original file names are disabled, so all files names submitted " +
                    "have been remapped. Content of the files have remained unchanged, authorId={}", authorId);
        }
        if (index.name().isEmpty())
            index = new Asset("index.html", index.data());
        PostEntity post = new PostEntity(
                authorId, System.currentTimeMillis(),
                titleHtml, titlePlaintext, Arrays.asList(tags),
                index.name(), Arrays.stream(assets).map(Asset::name).toList()
        );
        post = postRepository.save(post);

        try {
            String postStorageCategory = getStorageCategoryForPost(post.getPostId());
            storageService.save(index.data(), postStorageCategory, index.name());
            logger.info("Saved blog post index, postId={}, indexPath=\"{}\"", post.getPostId(), index.name());

            String assetsStorageCategory = getStorageCategoryForPostAssets(post.getPostId());
            for (Asset asset : assets) {
                if (asset.name().isEmpty())
                    continue;
                storageService.save(asset.data(), assetsStorageCategory, asset.name());
                logger.info("Saved blog post asset, postId={}, assetPath=\"{}\"", post.getPostId(), asset.name());
            }
        } catch (IOException e) {
            logger.warn("Failed to create new blog post, authorId={}", authorId, e);
            throw new RuntimeException(e);
        }

        logger.info("User created new blog post, authorId={}, postId={}", authorId, post.getPostId());

        return toDto(post);
    }

    @Override
    @Transactional
    public void deletePost(long postId) throws BlogServiceException {
        PostEntity post = getPostEntity(postId);
        post.setDeleted(true);
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getFeaturedPosts(int page) {
        Page<PostEntity> postsOnPage = postRepository
                .findByDeletedFalseOrderByMillisPostedDesc(Pageable.ofSize(featuredPageSize).withPage(page));
        return postsOnPage.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getFeaturedPostsTotalPages() {
        Page<PostEntity> postsOnPage = postRepository
                .findByDeletedFalseOrderByMillisPostedDesc(Pageable.ofSize(featuredPageSize));
        return postsOnPage.getTotalPages();
    }

    @Override
    @Transactional
    public Comment makeComment(long postId, long commentAuthorId, String content, String commentAuthorIp) throws BlogServiceException {
        if (!commentPostingEnabled){
            logger.info("User could not post comment because comment posting is disabled, " +
                            "postId={}, commentAuthorId={}, content(log formatted)={}",
                    postId, commentAuthorId, getCommentLogContent(content));
        }
        if (content.length() > maxCommentContentSize){
            logger.info("User attempted to make comment larger than {}, " +
                            "authorId={}, postId={}, content(log formatted)={}",
                    maxCommentContentSize, commentAuthorId, postId, getCommentLogContent(content));
            throw new BlogServiceException("Your comment must be less than %s characters long".formatted(maxCommentContentSize));
        }
        LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(content));
        try {
            lineNumberReader.skip(Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int newlineCount = lineNumberReader.getLineNumber();
        if (newlineCount > maxCommentNewlineCount){
            logger.info("User attempted to make comment with more newlines than permitted, " +
                    "newlineCount={}, maxNewlineCount={}, content(log formatted)={}",
                    newlineCount, maxCommentNewlineCount, getCommentLogContent(content));
            throw new BlogServiceException("Your comment contains too many newlines");
        }

        PostEntity post = getPostEntity(postId);
        if (post.isDeleted()){
            logger.warn("User attempted to make comment on deleted post, " +
                            "authorId={}, postId={}, content(log formatted)={}",
                    commentAuthorId, postId, getCommentLogContent(content));
            throw new BlogServiceException("Unable to comment on post because it has already been deleted");
        }

        CommentEntity comment = new CommentEntity(postId, commentAuthorId, System.currentTimeMillis(), content, commentAuthorIp);
        commentRepository.save(comment);
        logger.info("User make comment on post, authorId={}, postId={}, content(log formatted)={}",
                commentAuthorId, postId, getCommentLogContent(content));
        return toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getComments(long postId, int page) throws BlogServiceException {
        return getCommentPage(postId, page).stream().map(this::toCommentDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getCommentsTotalPages(long postId) throws BlogServiceException {
        Page<CommentEntity> comments = getCommentPage(postId);
        return comments.getTotalPages();
    }

    /**
     * Use @Transactional!!
     * Gets the first page of comments for a post
     * @param postId The post to get the comments for
     * @return The first page of comments for a post
     * @throws BlogServiceException If something goes wrong while getting the comments
     */
    private Page<CommentEntity> getCommentPage(long postId) throws BlogServiceException{
        return getCommentPage(postId, 0);
    }

    /**
     * Use @Transactional!!
     * Gets a given page of comments for a post
     * @param postId The post to get the comments for
     * @param page The comments page number
     * @return The requested page of comments for a post
     * @throws BlogServiceException If something goes wrong while getting the comments
     */
    private Page<CommentEntity> getCommentPage(long postId, int page) throws BlogServiceException{
        PostEntity post = getPostEntity(postId);
        if (post.isDeleted()) {
            logger.warn("User attempted to access comments on deleted post, postId={}, commentsPage={}",
                    postId, page);
            throw new BlogServiceException("Cannot get comments because post has been deleted");
        }

        Pageable pageable = Pageable.ofSize(commentPageSize).withPage(page);
        return commentRepository.findByPostIdAndDeletedFalseOrderByMillisPostedDesc(postId, pageable);
    }

    /**
     * @param entity The comment's entity
     * @return The dto equivalent of the entity
     */
    private Comment toCommentDto(CommentEntity entity){
        return new Comment(entity.getPostId(), entity.getAuthorId(), entity.getMillisPosted(), entity.getContent());
    }

    /**
     * Formats the comment's content so it is prepped to be logged
     * @param content The comment's content
     * @return The log formatted content
     */
    private String getCommentLogContent(String content){
        if (content.length() > commentContentLogCharacterLimit){
            content = content.substring(0, commentContentLogCharacterLimit + 1);
        }
        return content;
    }

    /**
     * Gets a post entity with a given post id. Caller method must have transactional; This method only reads
     * @param postId The post id to get the post with
     * @return The post entity
     * @throws BlogServiceException If the post entity was not found; also logs a warning
     */
    private PostEntity getPostEntity(long postId) throws BlogServiceException {
        Optional<PostEntity> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            logger.warn("Could not find post using provided id, postId={}", postId);
            throw new BlogServiceException("Post not found for given id");
        }
        return post.get();
    }

    /**
     * @param postId The post id to get the storage category for
     * @return The storage category where files for the post are stored
     */
    private String getStorageCategoryForPost(long postId){
        return "blog/%s".formatted(postId);
    }

    /**
     * @param postId The post id to get the storage assets category for
     * @return The storage category where assets for the post are stored
     */
    private String getStorageCategoryForPostAssets(long postId){
        return "%s/assets".formatted(getStorageCategoryForPost(postId));
    }

    /**
     * Replaces the file names for the given assets to randomly created, but recreatable names
     * @param assets The assets
     */
    private void replaceAssetFileNames(Asset... assets){
        for (int assetIndex = 0; assetIndex < assets.length; assetIndex++){
            Asset oldAsset = assets[assetIndex];
            assets[assetIndex] = new Asset(getNameForIndex(assetIndex), oldAsset.data());
        }
    }

    /**
     * Creates a unique but recreatable name for a given index
     * @param index The index to get the name for
     * @return The unique name for the given index
     */
    private String getNameForIndex(int index){
        char[] chars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
                'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
                'W', 'X', 'Y', 'Z'};
        int radix = chars.length;
        StringBuilder nameBuilder = new StringBuilder();
        do{
            nameBuilder.append(chars[index % radix]);
            index /= radix;
        }while(index > 0);
        return nameBuilder.toString();
    }

    /**
     * Converts a post entity to its DTO equivalent
     * @param entity The entity to convert
     * @return The dto equivalent
     */
    private Post toDto(PostEntity entity){
        return new Post(
                entity.getPostId(),
                entity.getTitleHtml(),
                entity.getTitlePlaintext(),
                entity.getAuthorId(),
                entity.getMillisPosted(),
                entity.getTags().toArray(new String[0]),
                entity.getAssetPaths().toArray(new String[0]),
                entity.isDeleted()
        );
    }

}
