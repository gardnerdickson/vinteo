package ca.vinteo;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Finder {

    private final ImmutableMap<String, String> fileMap;

    public Finder(Set<Path> directories, Set<String> allowedExtensions) throws IOException {
        this.fileMap = ImmutableMap.copyOf(findAllFilePaths(directories, allowedExtensions));
    }

    public List<String> findLike(String text) {
        List<String> results = new ArrayList<>();
        for (String key : fileMap.keySet()) {
            if (key.contains(text)) {
                results.add(key);
            }
        }
        return results;
    }

    public Map<String, String> results() {
        return new HashMap<>(fileMap);
    }


    private static Map<String, String> findAllFilePaths(Iterable<Path> rootDirectories, Set<String> extensions) throws IOException {
        Map<String, String> fileItems = new HashMap<>();
        for (Path directory : rootDirectories) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    fileItems.putAll(findAllFilePaths(Collections.singletonList(path), extensions));
                }
                if (Files.isRegularFile(path) && extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                    fileItems.put(path.getFileName().toString(), path.toString());
                }
            }
        }
        return fileItems;
    }


}
