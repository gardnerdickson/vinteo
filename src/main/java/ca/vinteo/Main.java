package ca.vinteo;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly 1 parameter. Got " + args.length);
        }

        Config config = new Config(Paths.get(args[0]));

        Path path = Paths.get("/media/gardner/EXTERNAL 2TB/Videos");
        List<String> fileNames = new ArrayList<>();
        findFileAndDirectoryNames(path, fileNames, config.getExtensions());
        for (String fileName : fileNames) {
            System.out.println(fileName);
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

}
