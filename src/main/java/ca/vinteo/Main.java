package ca.vinteo;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
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

        Set<Path> directoryPaths = config.getDirectories().stream().map(dir -> Paths.get(dir)).collect(Collectors.toSet());
        Finder finder = new Finder(directoryPaths, config.getExtensions());

        ObservableList<String> results = FXCollections.observableArrayList();
        results.addAll(finder.results().keySet());

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
                playWithVlc(Paths.get(finder.results().get(filename)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to play file: " + filename, e);
            }
        });

        Button openFolderButton = new Button("Open folder");
        openFolderButton.setOnAction(event -> {
            String filename = resultView.getSelectionModel().getSelectedItem();
            openFolder(Paths.get(finder.results().get(filename)));
        });

        primaryStage.setTitle("Vinteo");

        GridPane rootPane = new GridPane();

        ColumnConstraints columnInfo = new ColumnConstraints();
        columnInfo.setPercentWidth(100);

        RowConstraints rowInfo = new RowConstraints();
        rowInfo.setPercentHeight(100);

//        rootPane.setGridLinesVisible(true);
        rootPane.setAlignment(Pos.BASELINE_CENTER);
        rootPane.getColumnConstraints().add(columnInfo);
//        rootPane.getRowConstraints().add(rowInfo);
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        rootPane.setHgap(10);
        rootPane.setVgap(10);
        rootPane.add(textField, 0, 0);
        rootPane.add(resultView, 0, 1);

        GridPane buttonPane = new GridPane();
//        buttonPane.setGridLinesVisible(true);
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setAlignment(Pos.BASELINE_CENTER);
        buttonPane.add(playButton, 0, 0);
        buttonPane.add(openFolderButton, 1, 0);

        rootPane.add(buttonPane, 0 ,2);

        primaryStage.setScene(new Scene(rootPane, 500, 700));
        primaryStage.show();
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
