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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public Post getPost(long postId) throws BlogServiceException {
        return null;
    }

    @Override
    public InputStream getAsset(long postId, String assetPath) throws BlogServiceException {
        return null;
    }

    @Override
    @Transactional
    public Post makePost(String titleHtml, long authorId, Asset index, Asset... assets) throws BlogServiceException {
        Objects.requireNonNull(titleHtml);
        Objects.requireNonNull(index);
        if (assets == null)
            assets = new Asset[0];
        if (titleHtml.length() > 256)
            throw new BlogServiceException("Title html must not be more than 256 characters long");

        if (!persistOriginalFileNames){
            replaceAssetFileNames(assets);
            Asset[] indexArray = new Asset[]{index};
            replaceAssetFileNames(indexArray);
            index = indexArray[0];
        }

        PostEntity post = new PostEntity(
                authorId, System.currentTimeMillis(),
                titleHtml, index.name(),
                Arrays.stream(assets).map(Asset::name).toList()
        );
        post = postRepository.save(post);

        try {
            String postStorageCategory = getStorageCategoryForPost(post.getPostId());
            storageService.save(index.data(), postStorageCategory, index.name());

            String assetsStorageCategory = getStorageCategoryForPostAssets(post.getPostId());
            for (Asset asset : assets)
                storageService.save(asset.data(), assetsStorageCategory, asset.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Comment[] getComments(long postId, int page) throws BlogServiceException {
        return new Comment[0];
    }

    @Override
    public Comment makeComment(long postId, long commentAuthorId, String content) throws BlogServiceException {
        return null;
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

}
