package ca.vinteo;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

        Map<String, String> fileItems = findAllFilePaths(config.getDirectories().stream().map(dir -> Paths.get(dir)).collect(Collectors.toList()), config.getExtensions());
        Finder finder = new Finder(new ArrayList<>(fileItems.keySet()));

        primaryStage.setTitle("Vinteo");

        ObservableList<String> results = FXCollections.observableArrayList();
        results.addAll(finder.all());

        TextField textField = new TextField();
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            results.clear();
            results.addAll(finder.findLike(newValue));
        });

        ListView<String> resultView = new ListView<>(results);
        resultView.setOrientation(Orientation.VERTICAL);
        resultView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selection changed to " + newValue);
        });

        Button playButton = new Button("Play with VLC");
        playButton.setOnAction(event -> {
            String filename = resultView.getSelectionModel().getSelectedItem();
            try {
                playWithVlc(Paths.get(fileItems.get(filename)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to play file: " + filename, e);
            }
        });

        Button openFolderButton = new Button("Open folder");
        openFolderButton.setOnAction(event -> {
            String filename = resultView.getSelectionModel().getSelectedItem();
            openFolder(Paths.get(fileItems.get(filename)));
        });


        GridPane rootPane = new GridPane();
        rootPane.setHgap(10);
        rootPane.setVgap(10);
        rootPane.addRow(0, textField);
        rootPane.addRow(1, resultView);
        rootPane.addRow(2, playButton, openFolderButton);
        primaryStage.setScene(new Scene(rootPane, 500, 700));

        primaryStage.show();
    }


    private List<String> findAllFilesAndDirectories(Iterable<Path> rootDirectories, Set<String> extensions) throws IOException {
        List<String> fileItems = new ArrayList<>();
        for (Path directory : rootDirectories) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    fileItems.add(path.getFileName().toString());
                    fileItems.addAll(findAllFilesAndDirectories(Collections.singletonList(path), extensions));
                } else if (extensions.contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()))) {
                    fileItems.add(path.getFileName().toString());
                }
            }
        }
        return fileItems;
    }

    private Map<String, String> findAllFilePaths(Iterable<Path> rootDirectories, Set<String> extensions) throws IOException {
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


    private static void playWithVlc(Path filename) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] command = new String[]{"vlc", "--fullscreen", "--sub-track", "999", filename.toString()};
        runtime.exec(command);
    }


    private static void openFolder(Path filename) {
        Path directory = Files.isRegularFile(filename) ? filename.getParent() : filename;
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(directory.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to open directory: " + directory, e);
            }
        }).start();
    }

}
