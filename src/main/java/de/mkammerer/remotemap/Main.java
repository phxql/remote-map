package de.mkammerer.remotemap;

import de.mkammerer.remotemap.storage.FileStorage;
import de.mkammerer.remotemap.storage.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path file = Paths.get("storage.bin");

        Storage storage = new FileStorage();
        if (Files.isReadable(file)) {
            storage.load(file);
        } else {
            for (int i = 0; i < 100; i++) {
                byte[] key = new byte[]{(byte) i};
                byte[] value = new byte[]{(byte) i};

                storage.put(key, value);
            }

            storage.save(file);
        }
    }
}
