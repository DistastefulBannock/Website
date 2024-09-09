package me.bannock.website.services.storage;

import me.bannock.website.security.Roles;
import org.springframework.security.access.annotation.Secured;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Saves a stream of data to a category with an identifier
     * @param dataStream The data stream to save the data for; will not be closed after being read
     * @param category The category to store the data in. Do not use user-created values
     * @param identifier The unique identifier to match the file with.
     * @throws IOException If something goes wrong while saving the data
     */
    @Secured(Roles.StorageServiceRoles.SAVE_DATA)
    void save(InputStream dataStream, String category, String identifier) throws IOException;

    /**
     * Creates a stream to get a specific bit of data
     * @param category The category to load the data from. Do not use user-created values
     * @param identifier The unique identifier for the data we're getting.
     * @return A stream to load the data from. Must be closed after use.
     * @throws IOException If something goes wrong while saving the data
     */
    @Secured(Roles.StorageServiceRoles.LOAD_DATA)
    InputStream load(String category, String identifier) throws IOException;

}
