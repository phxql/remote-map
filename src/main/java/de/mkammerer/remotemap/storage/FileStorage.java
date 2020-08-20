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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FileStorage implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    private final ConcurrentMap<ByteArray, byte[]> map = new ConcurrentHashMap<>();

    @Override
    public void put(byte[] key, byte[] value) {
        Objects.requireNonNull(value, "value");

        map.put(ByteArray.of(key), value);
    }

    @Override
    public @Nullable byte[] get(byte[] key) {
        return map.get(ByteArray.of(key));
    }

    @Override
    public void delete(byte[] key) {
        map.remove(ByteArray.of(key));
    }

    @Override
    public void save(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        LOGGER.debug("Saving storage to {} ...", file.toAbsolutePath());

        int size;
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(file))) {
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

        LOGGER.debug("Stored {} entries", size);
    }

    @Override
    public void load(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        LOGGER.debug("Loading storage from {} ...", file.toAbsolutePath());

        int size = 0;
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

        LOGGER.debug("Loaded {} entries", size);
    }

    @Override
    public int getEntryCount() {
        return map.size();
    }

    private byte[] readExactly(InputStream stream, int length) throws IOException {
        assert stream != null;
        assert length >= 0;

        byte[] buffer = new byte[length];

        int read = stream.read(buffer);
        if (read != length) {
            throw new IOException(String.format("Tried to read %d bytes, got only %d", length, read));
        }
        return buffer;
    }
}
