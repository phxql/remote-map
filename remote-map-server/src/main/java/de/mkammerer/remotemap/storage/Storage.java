package de.mkammerer.remotemap.storage;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public interface Storage {
    void put(byte[] key, byte[] value);

    @Nullable byte[] get(byte[] key);

    void delete(byte[] key);

    void save(Path file) throws IOException;

    void load(Path file) throws IOException;

    int getEntryCount();
}
