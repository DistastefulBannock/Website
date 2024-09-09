package me.bannock.website.services.blog;

public class BlogServiceException extends Exception {

    /**
     * @param userFriendlyError The error that should be shown to the user as well as in any logs
     */
    public BlogServiceException(String userFriendlyError){
        this(userFriendlyError, userFriendlyError);
    }

    /**
     * @param userFriendlyError The error that should be shown to the user
     * @param message The message to show in any logs
     */
    public BlogServiceException(String userFriendlyError, String message){
        super(message);
        this.userFriendlyError = userFriendlyError;
    }

    private final String userFriendlyError;

    public String getUserFriendlyError() {
        return userFriendlyError;
    }

}
