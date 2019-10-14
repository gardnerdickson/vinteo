package ca.vinteo;

import ca.vinteo.ui.EventMediator;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class FileScanner {

    private static final Logger logger = LoggerFactory.getLogger(FileScanner.class);

    private final Set<Path> directories;
    private final Set<String> allowedExtensions;

    public FileScanner(Set<Path> directories, Set<String> allowedExtensions, EventMediator eventMediator) {
        this.directories = directories;
        this.allowedExtensions = allowedExtensions;
        eventMediator.setFileScanner(this);
    }

    public Map<String, String> findAllFilePaths(Function<Path, Void> callback) throws IOException {
        logger.info("Finding all files in directories: '{}'. Filtering on extensions: {}", Joiner.on(", '").join(directories), Joiner.on(", ").join(allowedExtensions));
        return traversePaths(directories, allowedExtensions, callback);
    }

    private Map<String, String> traversePaths(Iterable<Path> rootDirectories, Set<String> extensions, Function<Path, Void> callback) throws IOException {
        Map<String, String> fileItems = new HashMap<>();
        for (Path directory : rootDirectories) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    fileItems.putAll(traversePaths(Collections.singletonList(path), extensions, callback));
                }
                if (Files.isRegularFile(path) && extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                    fileItems.put(path.getFileName().toString(), path.toString());
                    System.out.println(path.getFileName());
                    callback.apply(path);
                }
            }
        }
        return fileItems;
    }

}
