package me.bannock.website.services.storage.impl;

import me.bannock.website.security.Roles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileStorageServiceImplTest {

    @Autowired
    private FileStorageServiceImpl storageService;

    @Value("${bannock.ioTransferBuffer:1024}")
    private int ioTransferBuffer;

    private String getTestCategory(){
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String callingClassName;
        try {
            callingClassName = Class.forName(stackTrace[1].getClassName(),
                    false, getClass().getClassLoader()).getSimpleName();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "test/%s/%s/".formatted(callingClassName, stackTrace[1].getMethodName()
                .replaceAll("lambda\\$", "").replaceAll("\\$[0-9]", ""));
    }

    @Test
    @WithMockUser(username = "test", authorities = {
            Roles.StorageServiceRoles.SAVE_DATA, Roles.StorageServiceRoles.LOAD_DATA
    })
    void saveAndLoadGlunggiOfDifferentSize(){
        assertDoesNotThrow(() -> {
            for (int i = 0; i <= 30; i++){ // Writes files as large as 1gb
                // Create glunggus with random data and save.
                // Stores the old data, so we can verify the data is good later
                int glunggusFactorSize = (int)Math.pow(2, i);
                String fileName = "x%s %s.glunggus".formatted(glunggusFactorSize, i);
                byte[] dataBytes = new byte[glunggusFactorSize];
                ThreadLocalRandom.current().nextBytes(dataBytes);
                storageService.save(
                        new ByteArrayInputStream(dataBytes), getTestCategory(), fileName);

                ByteArrayOutputStream glunggusData = new ByteArrayOutputStream();
                InputStream glunggusStream = storageService.load(getTestCategory(), fileName);
                byte[] glunggusBuffer = new byte[ioTransferBuffer];
                int readGlunggiBytes = 0;
                while ((readGlunggiBytes = glunggusStream.read(glunggusBuffer, 0, glunggusBuffer.length)) != -1){
                    glunggusData.write(glunggusBuffer, 0, readGlunggiBytes);
                }
                // Load must be the same as save, or else
                assertArrayEquals(dataBytes, glunggusData.toByteArray());
            }
        });
    }

    @Test
    @WithMockUser(username = "test", authorities = {
            Roles.StorageServiceRoles.SAVE_DATA
    })
    void attemptSaveDataWithDirectoryTraversalPath(){
        String category = "test";
        String id /* id is the only var that can be user supplied */ = "../..\\test.png";
        assertThrows(IllegalArgumentException.class, () -> storageService.save(new ByteArrayInputStream(new byte[1]), category, id));
    }

}