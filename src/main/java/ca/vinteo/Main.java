package ca.vinteo;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly 1 parameter. Got " + args.length);
        }

        Config config = new Config(Paths.get(args[0]));

        Set<String> directories = config.getDirectories();
        List<String> fileNames = new ArrayList<>();
        for (String directory : directories) {
            findFileAndDirectoryNames(Paths.get(directory), fileNames, config.getExtensions());
            for (String fileName : fileNames) {
                System.out.println(fileName);
            }
        }

    }

    private static void findFileAndDirectoryNames(Path rootDirectory, List<String> fileNames, Set<String> extensions) throws IOException {
        DirectoryStream<Path> directory = Files.newDirectoryStream(rootDirectory);
        for (Path path : directory) {
            if (Files.isDirectory(path)) {
                findFileAndDirectoryNames(path, fileNames, extensions);
                fileNames.add(path.getFileName().toString());
            } else if (extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                fileNames.add(path.getFileName().toString());
            }
        }
    }

    private static void findAllFileExtensions(Path rootDirectory, Map<String, List<String>> extensions) throws IOException {
        DirectoryStream<Path> directory = Files.newDirectoryStream(rootDirectory);
        for (Path path : directory) {
            if (Files.isDirectory(path)) {
                findAllFileExtensions(path, extensions);
            } else {
                String extension = com.google.common.io.Files.getFileExtension(path.getFileName().toString());
                if (extensions.containsKey(extension)) {
                    extensions.get(extension).add(path.getFileName().toString());
                } else {
                    List<String> files = new ArrayList<>();
                    files.add(path.getFileName().toString());
                    extensions.put(extension, files);
                }
            }
        }
    }

}
