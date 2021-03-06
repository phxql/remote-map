package de.mkammerer.remotemap.storage;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileStorage implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    private final ConcurrentMap<ByteArray, byte[]> map;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public FileStorage(ConcurrentMap<ByteArray, byte[]> map) {
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void put(byte[] key, byte[] value) {
        Objects.requireNonNull(value, "value");

        map.put(ByteArray.of(key), value);
        dirty.set(true);
    }

    @Override
    public @Nullable byte[] get(byte[] key) {
        return map.get(ByteArray.of(key));
    }

    @Override
    public void delete(byte[] key) {
        map.remove(ByteArray.of(key));
        dirty.set(true);
    }

    @Override
    public void save(Path file) throws IOException {
        Objects.requireNonNull(file, "file");

        if (!dirty.compareAndSet(true, false)) {
            // No changes, no need to save file
            return;
        }

        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        LOGGER.debug("Saving storage to {} ...", file.toAbsolutePath());

        long start = System.nanoTime();
        int size;
        // Write into temp file
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
            Set<Map.Entry<ByteArray, byte[]>> entries = map.entrySet();
            size = entries.size();
            for (Map.Entry<ByteArray, byte[]> entry : entries) {
                byte[] key = entry.getKey().getValue();
                byte[] value = entry.getValue();

                stream.writeInt(key.length);
                stream.write(key);

                stream.writeInt(value.length);
                stream.write(value);
            }
        }
        // Delete old destination file
        Files.deleteIfExists(file);
        // Rename temp file to destination file
        Files.move(tempFile, file);
        long end = System.nanoTime();

        LOGGER.debug("Stored {} entries in {}", size, Duration.ofNanos(end - start));
    }

    @Override
    public void load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        LOGGER.debug("Loading storage from {} ...", file.toAbsolutePath());

        dirty.set(false);

        int size = 0;
        long start = System.nanoTime();
        try (ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(file))) {
            map.clear();

            while (stream.available() > 0) {
                int keyLength = stream.readInt();
                byte[] key = readExactly(stream, keyLength);

                int valueLength = stream.readInt();
                byte[] value = readExactly(stream, valueLength);

                map.put(ByteArray.of(key), value);
                size += 1;
            }
        }
        long end = System.nanoTime();

        LOGGER.debug("Loaded {} entries in {}", size, Duration.ofNanos(end - start));
    }

    @Override
    public int getEntryCount() {
        return map.size();
    }

    private byte[] readExactly(InputStream stream, int length) throws IOException {
        assert stream != null;
        assert length >= 0;

        byte[] buffer = new byte[length];
        int offset = 0;

        while (offset < length) {
            int read = stream.read(buffer, offset, length - offset);
            if (read == -1) {
                throw new IOException(String.format("Unexpected end of stream at offset %d of length %d", offset, length));
            }
            offset += read;
        }

        return buffer;
    }
}
