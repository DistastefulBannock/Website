package me.bannock.website.services.blog.hibernate;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "blog_posts", indexes = {
        @Index(columnList = "post_id", unique = true),
        @Index(columnList = "authorId")
})
public class PostEntity {

    public PostEntity(long authorId, long millisPosted, String titleHtml,
                      String titlePlaintext, List<String> tags, String indexPath,
                      List<String> assetPaths) {
        this(authorId, millisPosted, titleHtml, titlePlaintext, tags, indexPath, assetPaths, false);
    }

    public PostEntity(long authorId, long millisPosted, String titleHtml,
                      String titlePlaintext, List<String> tags, String indexPath,
                      List<String> assetPaths, boolean deleted) {
        Objects.requireNonNull(titleHtml);
        Objects.requireNonNull(titlePlaintext);
        Objects.requireNonNull(indexPath);
        if (tags == null)
            tags = new ArrayList<>();
        if (assetPaths == null)
            assetPaths = new ArrayList<>();
        this.authorId = authorId;
        this.millisPosted = millisPosted;
        this.titleHtml = titleHtml;
        this.titlePlaintext = titlePlaintext;
        this.tags = tags;
        this.indexPath = indexPath;
        this.assetPaths = assetPaths;
        this.deleted = deleted;
    }

    public PostEntity(){}

    @Id
    @GeneratedValue(generator = "blog_post_id_seq")
    @Column(name = "post_id")
    @SequenceGenerator(name = "blog_post_id_seq", sequenceName = "blog_post_id_seq", initialValue = 0, allocationSize = 1)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private long authorId;

    @Column(name = "millis_posted", nullable = false)
    private long millisPosted;

    @Column(name = "titleHtml", length = 256, nullable = false)
    private String titleHtml;

    @Column(name = "title_plaintext", length = 256, nullable = false)
    private String titlePlaintext;

    @Column(name = "tags")
    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "blog_post_tags", joinColumns = @JoinColumn(name = "post_id"), indexes = {
            @Index(columnList = "post_id"),
            @Index(columnList = "tags")
    })
    private List<String> tags;

    @Column(name = "index_path", nullable = false)
    private String indexPath;

    @Column(name = "asset_paths")
    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "blog_post_assets", joinColumns = @JoinColumn(name = "post_id"), indexes = {
            @Index(columnList = "post_id"),
            @Index(columnList = "asset_paths")
    })
    private List<String> assetPaths;

    @Column(name = "deleted")
    private boolean deleted;

    private void setPostId(long postId) {
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getMillisPosted() {
        return millisPosted;
    }

    public void setMillisPosted(long millisPosted) {
        this.millisPosted = millisPosted;
    }

    public String getTitleHtml() {
        return titleHtml;
    }

    public void setTitleHtml(String titleHtml) {
        this.titleHtml = titleHtml;
    }

    public String getTitlePlaintext() {
        return titlePlaintext;
    }

    public void setTitlePlaintext(String titlePlaintext) {
        this.titlePlaintext = titlePlaintext;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAssetPaths() {
        return assetPaths;
    }

    public void setAssetPaths(List<String> assetPaths) {
        this.assetPaths = assetPaths;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
