package me.bannock.website.services.storage;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Saves a stream of data to a category with an identifier
     * @param dataStream The data stream to save the data for; will not be closed after being read
     * @param category The category to store the data in. Do not use user-created values
     * @param identifier The unique identifier to match the file with. Do not use user-created values
     * @throws IOException If something goes wrong while saving the data
     */
    void save(InputStream dataStream, String category, String identifier) throws IOException;

    /**
     * Creates a stream to get a specific bit of data
     * @param category The category to load the data from. Do not use user-created values
     * @param identifier The unique identifier for the data we're getting. Do not use user-created values
     * @return A stream to load the data from. Must be closed after use.
     * @throws IOException If something goes wrong while saving the data
     */
    InputStream load(String category, String identifier) throws IOException;

}
