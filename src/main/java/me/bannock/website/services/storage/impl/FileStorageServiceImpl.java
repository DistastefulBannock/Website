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
     * @param identifier The identifier for the file we're mapping
     * @return The
     */
    private File toFile(String category, String identifier){
        Objects.requireNonNull(category);
        Objects.requireNonNull(identifier);
        String filePath = "%s/%s".formatted(category, identifier);
        return new File(applicationDir, filePath);
    }

}
