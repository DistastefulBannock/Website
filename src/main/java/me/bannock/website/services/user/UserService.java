package me.bannock.website.services.user;

public interface UserService {

    /**
     * Gets a user using their username and password
     * @param name The user's username
     * @param password The user's password
     * @return The user
     * @throws UserServiceException If something goes wrong while getting the user
     */
    User getUserWithNameAndPassword(String name, String password) throws UserServiceException;

    /**
     * Gets a user using their email
     * @param email The user's email
     * @return The user
     * @throws UserServiceException If something goes wrong while getting the user
     */
    User getUserWithEmail(String email) throws UserServiceException;

    /**
     * Gets a user using their name
     * @param name The user's name
     * @return The user
     * @throws UserServiceException If something goes wrong while getting the user
     */
    User getUserWithName(String name) throws UserServiceException;

    /**
     * Creates a dummy user; an incomplete user has a limited set of features and
     * is unable to login without first claiming the account
     * @param name The dummy's username
     * @param email The dummy's email
     * @param ip The dummy's ip address
     * @return The new user
     * @throws UserServiceException If something goes wrong while registering the user
     */
    User registerDummyUser(String name, String email, String ip) throws UserServiceException;

    /**
     * Creates a new user
     * @param user The user to create
     * @return The created user, may not match the provided user reference
     * @throws UserServiceException If something goes wrong while registering the user
     */
    User registerUser(User user) throws UserServiceException;

}
