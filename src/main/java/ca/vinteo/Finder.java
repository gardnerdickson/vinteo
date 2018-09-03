package ca.vinteo;

import ca.vinteo.ui.EventMediator;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Finder {

    private static final Logger logger = LoggerFactory.getLogger(Finder.class);

    private final ImmutableMap<String, String> fileMap;
    private final Set<Path> directories;
    private final Set<String> allowedExtensions;

    public Finder(Set<Path> directories, Set<String> allowedExtensions, EventMediator eventMediator) throws IOException {
        this.directories = directories;
        this.allowedExtensions = allowedExtensions;
        logger.info("Finding all files in directories: '{}'", Joiner.on(", '").join(directories));
        logger.info("Filtering on extensions: {}", Joiner.on(", ").join(allowedExtensions));
        fileMap = ImmutableMap.copyOf(findAllFilePaths());
        logger.info("Found {} video items.", fileMap.size());
        eventMediator.setFinder(this);
    }

    public List<String> findLike(String query) {
        List<String> results = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();
        for (String file : fileMap.keySet()) {
            if (file.toLowerCase().contains(lowerCaseQuery)) {
                results.add(file);
            }
        }
        return results;
    }

    public Map<String, String> results() {
        return new HashMap<>(fileMap);
    }

    public Map<String, String> findAllFilePaths() throws IOException {
        return traversePaths(directories, allowedExtensions);
    }

    private Map<String, String> traversePaths(Iterable<Path> rootDirectories, Set<String> extensions) throws IOException {
        Map<String, String> fileItems = new HashMap<>();
        for (Path directory : rootDirectories) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    fileItems.putAll(traversePaths(Collections.singletonList(path), extensions));
                }
                if (Files.isRegularFile(path) && extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                    fileItems.put(path.getFileName().toString(), path.toString());
                }
            }
        }
        return fileItems;
    }

}
