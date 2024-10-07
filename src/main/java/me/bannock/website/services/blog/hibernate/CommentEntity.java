package me.bannock.website.services.blog.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_comments", indexes = {
        @Index(columnList = "comment_id", unique = true),
        @Index(columnList = "post_id, deleted"),
        @Index(columnList = "millis_posted, original_author_ip")
})
public class CommentEntity {

    public CommentEntity(long postId, long authorId, long millisPosted, String content, String authorIp) {
        this.postId = postId;
        this.authorId = authorId;
        this.millisPosted = millisPosted;
        this.content = content;
        this.deleted = false;
        this.originalAuthorIp = authorIp;
    }

    public CommentEntity(){}

    @Id
    @GeneratedValue(generator = "blog_comment_id_seq")
    @Column(name = "comment_id", nullable = false)
    @SequenceGenerator(name = "blog_comment_id_seq", sequenceName = "blog_comment_id_seq", initialValue = 0, allocationSize = 1)
    private Long commentId;

    @Column(name = "post_id")
    private long postId;

    @Column(name = "author_id")
    private long authorId;

    @Column(name = "millis_posted")
    private long millisPosted;

    @Column(name = "content")
    private String content;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "original_author_ip", nullable = false)
    private String originalAuthorIp;

    public long getCommentId() {
        return commentId;
    }

    private void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getOriginalAuthorIp() {
        return originalAuthorIp;
    }

    public void setOriginalAuthorIp(String originalAuthorIp) {
        this.originalAuthorIp = originalAuthorIp;
    }

}
