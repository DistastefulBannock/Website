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
        @Index(columnList = "id", unique = true)
})
public class CommentEntity {

    @Id
    @GeneratedValue(generator = "blog_comment_id_seq")
    @Column(name = "comment_id", nullable = false)
    @SequenceGenerator(name = "blog_comment_id_seq", sequenceName = "blog_comment_id_seq", initialValue = 0, allocationSize = 1)
    private Long commentId;

    @Column(name = "author_id")
    private long authorId;

    @Column(name = "millis_posted")
    private long millisPosted;

    @Column(name = "content")
    private String content;

    @Column(name = "deleted")
    private boolean deleted;

    public long getCommentId() {
        return commentId;
    }

    private void setCommentId(long commentId) {
        this.commentId = commentId;
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

}
