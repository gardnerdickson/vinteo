package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class FileScanner {

    private static final Logger logger = LoggerFactory.getLogger(FileScanner.class);

    private Set<Path> directories;
    private final Set<String> allowedExtensions;

    public FileScanner(Set<Path> directories, Set<String> allowedExtensions, EventMediator eventMediator) {
        this.directories = directories;
        this.allowedExtensions = allowedExtensions;
        eventMediator.setFileScanner(this);
    }

    public Set<FileInfo> findAllFilePaths(Function<Path, Void> callback) throws IOException {
        logger.info("Finding all files in directories: '{}'. Filtering on extensions: {}", Joiner.on(", '").join(directories), Joiner.on(", ").join(allowedExtensions));
        return traversePaths(directories, allowedExtensions, callback);
    }

    private Set<FileInfo> traversePaths(Iterable<Path> rootDirectories, Set<String> extensions, Function<Path, Void> callback) throws IOException {
        Set<FileInfo> fileItems = new HashSet<>();
        for (Path directory : rootDirectories) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    fileItems.addAll(traversePaths(Collections.singletonList(path), extensions, callback));
                }
                if (Files.isRegularFile(path) && extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                    fileItems.add(new FileInfo(path.getFileName().toString(), path.toString()));
                    callback.apply(path);
                }
            }
        }
        return fileItems;
    }


    public void setDirectories(Set<Path> directories) {
        this.directories = directories;
    }

}
