package ca.vinteo;


import com.sun.javafx.scene.control.skin.LabeledText;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public final class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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
        setupUi(primaryStage, finder, config);
        primaryStage.show();
    }


    private void setupUi(Stage primaryStage, Finder finder, Config config) {
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
            logger.info("Selection changed to '{}'", newValue);
        });

        resultView.setOnMouseClicked((event) -> {
            LabeledText selectedItem = (LabeledText) event.getTarget();
            Path filePath = Paths.get(finder.results().get(selectedItem.getText()));
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                logger.info("Detected [double click]");
                try {
                    playWithVlc(filePath, config.getVlcCommand());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to play '" + filePath.toString() + "'", e);
                }
            } else if (event.isControlDown() && event.getButton() == MouseButton.PRIMARY) {
                logger.info("Detected [ctrl + left click]");
                openFolder(filePath);
            }
        });


        Button playButton = new Button("Play with VLC");
        playButton.setOnAction(event -> {
            String filename = resultView.getSelectionModel().getSelectedItem();
            try {
                playWithVlc(Paths.get(finder.results().get(filename)), config.getVlcCommand());
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

        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        rootPane.setSpacing(10);

        GridPane buttonPane = new GridPane();
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setAlignment(Pos.BASELINE_CENTER);
        buttonPane.add(playButton, 0, 0);
        buttonPane.add(openFolderButton, 1, 0);

        rootPane.getChildren().addAll(textField, resultView, buttonPane);
        VBox.setVgrow(resultView, Priority.ALWAYS);

        primaryStage.setScene(new Scene(rootPane, 500, 700));
    }


    private static void playWithVlc(Path filename, String vlcExec) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] command = new String[]{vlcExec, "--fullscreen", "--sub-track", "999", filename.toString()};
        logger.info("Opening '{}' with VLC. Executing command: {}", filename.toString(), command);
        runtime.exec(command);
    }


    private static void openFolder(Path filename) {
        Path directory = Files.isRegularFile(filename) ? filename.getParent() : filename;
        new Thread(() -> {
            try {
                logger.info("Opening directory in file explorer: '{}'", directory);
                Desktop.getDesktop().open(directory.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to open directory: " + directory, e);
            }
        }).start();
    }

}
