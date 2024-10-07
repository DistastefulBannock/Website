package me.bannock.website.controllers.blog;

import org.springframework.ui.Model;

public class CommentFormException extends Exception {

    public CommentFormException(String message,
                                Model model, CommentFormPojo commentFormPojo) {
        this(message, message, model, commentFormPojo);
    }

    public CommentFormException(String userFriendlyMessage, String message,
                                Model model, CommentFormPojo commentFormPojo) {
        super(message);
        this.userFriendlyMessage = userFriendlyMessage;
        this.model = model;
        this.commentFormPojo = commentFormPojo;
        this.postId = commentFormPojo.getPostId();
    }

    private String userFriendlyMessage;
    private Model model;
    private CommentFormPojo commentFormPojo;
    private long postId;

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public Model getModel() {
        return model;
    }

    public CommentFormPojo getCommentFormPojo() {
        return commentFormPojo;
    }

    public long getPostId() {
        return postId;
    }

    @Override
    public String toString() {
        return "CommentFormException{" +
                "userFriendlyMessage='" + userFriendlyMessage + '\'' +
                ", commentFormPojo=" + commentFormPojo +
                ", postId=" + postId +
                '}';
    }

}
