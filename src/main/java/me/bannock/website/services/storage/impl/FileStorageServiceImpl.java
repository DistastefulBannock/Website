package me.bannock.website.services.storage.impl;

import me.bannock.website.services.storage.StorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements StorageService {

    public FileStorageServiceImpl(){
        if (!applicationDir.exists() && !applicationDir.mkdirs()){
            logger.warn("Failed to create application directory. This may cause problems later, " +
                    "path={}, canWrite={}", applicationDir.getAbsoluteFile().toString(), applicationDir.canWrite());
        }
    }

    private final Logger logger = LogManager.getLogger();
    private final File applicationDir = new File(new File("application/").getAbsoluteFile().toString());

    /**
     * In bytes, how much data being written between file and io+ streams at a time
     * Common values: 1024 or 1024*16 or 1024*32
     */
    @Value("${bannock.ioTransferBuffer:1024}")
    private int ioTransferBuffer;

    @Override
    public void save(InputStream dataStream, String category, String identifier) throws IOException {
        File save = toFile(category, identifier);
        if (!save.getParentFile().exists() && !save.getParentFile().mkdirs())
            logger.warn("Could not create directory for save, " +
                    "This may cause issues later, category=\"{}\", id=\"{}\", path=\"{}\", canWrite={}",
                    category, identifier, save.getAbsoluteFile().toString(), save.canWrite());

        long writeStartMillis = System.currentTimeMillis();
        try(OutputStream saveOut = new FileOutputStream(save)){
            byte[] buffer = new byte[ioTransferBuffer];
            int readBytes = 0;
            while ((readBytes = dataStream.read(buffer, 0, buffer.length)) != -1){
                saveOut.write(buffer, 0, readBytes);
            }
        }catch (IOException e){
            logger.warn("Failed to save file, category=\"{}\", id=\"{}\", saveTime={}ms, canWrite={}",
                    category, identifier, System.currentTimeMillis() - writeStartMillis, save.canWrite(), e);
            throw e;
        }
        logger.info("Wrote \"{}\"/\"{}\" to {} in {}ms", category, identifier,
                save.getAbsoluteFile().toPath().toString(), System.currentTimeMillis() - writeStartMillis);
    }

    @Override
    public InputStream load(String category, String identifier) throws IOException {
        File save = toFile(category, identifier);
        return new FileInputStream(save);
    }

    /**
     * @param category The category for the file we're getting. Do not pass in user supplied  values
     * @param identifier The identifier for the file we're mapping. Could be a user supplied value
     * @return The file
     * @throws IllegalArgumentException if the identifier does not pass validations
     */
    private File toFile(String category, String identifier){
        Objects.requireNonNull(category);
        Objects.requireNonNull(identifier);

        if (new File(identifier).isAbsolute())
            throw new IllegalArgumentException("Identifier must not match to an absolute path");

        // We move the directories found in the identifier's path to the category's so our directory
        // traversal validation fails if the resulting file is outside the directory the intended file is located in
        StringBuilder identifierPathBuilder = new StringBuilder();
        String[] identifierPathParts = identifier.split("[\\\\/]");
        int pathIndex = 0;
        for (pathIndex = 0; pathIndex < identifierPathParts.length - 1; pathIndex++){
            identifierPathBuilder
                    .append(identifierPathBuilder.isEmpty() ? "" : "/")
                    .append(identifierPathParts[pathIndex]);
        }
        identifier = identifierPathParts[pathIndex];
        category = "%s/%s".formatted(category, identifierPathBuilder.toString());

        File categoryDir = new File(applicationDir, "%s/".formatted(category));
        File file = new File(categoryDir, identifier);
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(categoryDir.getAbsolutePath())){
                logger.warn("File was not supplied because a path traversal attempt was detected, " +
                        "innerScopeCategory={}, innerScopeId={}, pathTraversedTo={}", category, identifier,canonicalPath);
                throw new IllegalArgumentException("Invalid file identifier. Please try again later.");
            }
        } catch (IOException e) {
            logger.warn("Something went wrong while doing path traversal validations, " +
                    "category={}, id={}", category, identifier);
            throw new RuntimeException(e);
        }

        return file;
    }

}
