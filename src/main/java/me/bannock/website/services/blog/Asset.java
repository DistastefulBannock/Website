package me.bannock.website.services.blog;

import java.io.InputStream;
import java.util.Objects;

public record Asset(String name, InputStream data) {

    /**
     * @param name The name of the asset
     * @param data The data to store in the asset
     */
    public Asset {
        Objects.requireNonNull(name);
        Objects.requireNonNull(data);
    }
}
