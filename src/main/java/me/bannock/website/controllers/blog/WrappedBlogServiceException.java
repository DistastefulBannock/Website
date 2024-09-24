package me.bannock.website.controllers.blog;

import me.bannock.website.services.blog.BlogServiceException;
import org.springframework.ui.Model;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Used to gracefully handle exceptions so that they direct to a handled error page.
 * If this is being thrown, it is expected that this exception is already logged and so will not be logged again.
 */
public class WrappedBlogServiceException extends RuntimeException {

    public WrappedBlogServiceException(BlogServiceException blogServiceException, Model model){
        this.blogServiceException = blogServiceException;
        this.model = model;
    }

    private final BlogServiceException blogServiceException;
    private final Model model;

    public BlogServiceException getBlogServiceException() {
        return blogServiceException;
    }

    public Model getModel() {
        return model;
    }

    public String getUserFriendlyError() {
        return blogServiceException.getUserFriendlyError();
    }

    @Override
    public String getMessage() {
        return blogServiceException.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return blogServiceException.getLocalizedMessage();
    }

    @Override
    public Throwable getCause() {
        return blogServiceException.getCause();
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return blogServiceException.initCause(cause);
    }

    @Override
    public String toString() {
        return blogServiceException.toString();
    }

    @Override
    public void printStackTrace() {
        blogServiceException.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        blogServiceException.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        blogServiceException.printStackTrace(s);
    }

    @Override
    public Throwable fillInStackTrace() {
        if (blogServiceException == null)
            return super.fillInStackTrace();
        return blogServiceException.fillInStackTrace();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return blogServiceException.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        blogServiceException.setStackTrace(stackTrace);
    }

}
