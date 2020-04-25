package ca.vinteo.util;

import java.util.Objects;

public class FileInfo {
    private final String name;
    private final String path;

    public FileInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(name, fileInfo.name) &&
                Objects.equals(path, fileInfo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}
