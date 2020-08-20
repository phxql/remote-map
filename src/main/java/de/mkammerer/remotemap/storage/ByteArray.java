package de.mkammerer.remotemap.storage;

import java.util.Arrays;
import java.util.Objects;

class ByteArray {
    private final byte[] value;

    private ByteArray(byte[] value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public byte[] getValue() {
        return value;
    }

    public static ByteArray of(byte[] value) {
        return new ByteArray(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteArray byteArray = (ByteArray) o;

        return Arrays.equals(value, byteArray.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
