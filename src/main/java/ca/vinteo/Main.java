package ca.vinteo;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parameters commandLineArguments = getParameters();
        if (commandLineArguments.getRaw().size() != 1) {
            throw new IllegalArgumentException("Expected exactly 1 parameter. Got " + commandLineArguments.getRaw().size());
        }
        Config config = new Config(Paths.get(commandLineArguments.getRaw().get(0)));

        primaryStage.setTitle("Vinteo");
        TextField textField = new TextField();
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Something changed in the text field");
        });

        StackPane root = new StackPane();
        root.getChildren().add(textField);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
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

    private static void findAndPrintAllFilesAndDirectories(Set<String> directories, Set<String> extensionInclusions) throws IOException {
        List<String> fileNames = new ArrayList<>();
        for (String directory : directories) {
            findFileAndDirectoryNames(Paths.get(directory), fileNames, extensionInclusions);
            for (String fileName : fileNames) {
                System.out.println(fileName);
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


    private static void playWithVlc() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String fileName = "/media/gardner/EXTERNAL 2TB/Videos/TV/Homeland/Season 7/[TorrentCouch.com].Homeland.S07.Complete.720p.BRRip.x264.ESubs.[4.6GB].[Season.7.Full]/[TorrentCouch.com].Homeland.S07E01.720p.BRRip.x264.ESubs.mkv";
        String[] command = new String[]{"vlc", "--fullscreen", "--sub-track", "999", fileName};
        runtime.exec(command);
    }


    private static void openFolder() throws IOException {
        Desktop desktop = Desktop.getDesktop();
        desktop.open(Paths.get("/media/gardner/EXTERNAL 2TB/Videos/TV/Homeland/Season 7/[TorrentCouch.com].Homeland.S07.Complete.720p.BRRip.x264.ESubs.[4.6GB].[Season.7.Full]").toFile());
    }

}
