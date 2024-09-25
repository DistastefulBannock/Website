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
    @Value("${bannock.hibernateBlog.featuredPageSize}")
    private int featuredPageSize = 10;
    @Value("${bannock.hibernateBlog.commentPageSize}")
    private int commentPageSize = 50;

    @Override
    @Transactional(readOnly = true)
    public Post getPost(long postId) throws BlogServiceException {
        PostEntity post = getPostEntity(postId);
        if (post.isDeleted())
            throw new BlogServiceException("Can not get post because this post has been deleted",
                    "Post has been deleted");
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
            logger.error("Failed to load blog post asset, postId={}, assetPath={}", postId, assetPath);
            throw new RuntimeException(e);
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
    public List<Post> getFeaturedPosts(int page) {
        Page<PostEntity> postsOnPage = postRepository
                .findByOrderByMillisPostedDesc(Pageable.ofSize(featuredPageSize).withPage(page));
        return postsOnPage.stream().map(this::toDto).toList();
    }

    @Override
    public int getFeaturePostTotalPages() {
        Page<PostEntity> postsOnPage = postRepository
                .findByOrderByMillisPostedDesc(Pageable.ofSize(featuredPageSize));
        return postsOnPage.getTotalPages();
    }

    @Override
    public Comment[] getComments(long postId, int page) throws BlogServiceException {
//        commentRepository.findByPostId(postId, Pageable.ofSize(commentPageSize));
        return new Comment[0]; // TODO
    }

    @Override
    public Comment makeComment(long postId, long commentAuthorId, String content) throws BlogServiceException {
        return null; // TODO
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
                entity.getAssetPaths().toArray(new String[0])
        );
    }

}
