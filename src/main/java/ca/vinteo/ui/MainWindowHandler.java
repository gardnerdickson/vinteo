package ca.vinteo.ui;

import ca.vinteo.Finder;
import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainWindowHandler {

    private static final Logger logger = LoggerFactory.getLogger(MainWindowHandler.class);

    private final Finder finder;
    private final String vlcCommand;

    public MainWindowHandler(Finder finder, String vlcCommand) {
        this.finder = finder;
        this.vlcCommand = vlcCommand;
    }

    public ChangeListener<String> handleTextFieldChange(ObservableList<String> listItems) {
        return (observable, oldValue, newValue) -> {
            listItems.clear();
            listItems.addAll(finder.findLike(newValue));
        };
    }

    public ChangeListener<String> handleResultViewChange() {
        return (observable, oldValue, newValue) -> logger.debug("Selection changed to '{}'", newValue);
    }

    public EventHandler<MouseEvent> handleListItemMouseClick() {
        return (MouseEvent event) -> {
            LabeledText selectedItem = (LabeledText) event.getTarget();
            Path filePath = Paths.get(finder.results().get(selectedItem.getText()));
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                logger.info("Detected [double click]");
                try {
                    playWithVlc(filePath, vlcCommand);
                }
                catch (IOException e) {
                    throw new RuntimeException("Failed to play '" + filePath.toString() + "'", e);
                }
            }
            else if (event.isControlDown() && event.getButton() == MouseButton.PRIMARY) {
                logger.info("Detected [ctrl + left click]");
                openFolder(filePath);
            }
        };

    }

    public EventHandler<ActionEvent> handlePlayButtonPress(ListView<String> listView) {
        return (event) -> {
            String filename = listView.getSelectionModel().getSelectedItem();
            try {
                playWithVlc(Paths.get(finder.results().get(filename)), vlcCommand);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to play file: " + filename, e);
            }
        };
    }

    public EventHandler<ActionEvent> handleOpenFolderButtonPress(ListView<String> listView) {
        return (event) -> {
            String filename = listView.getSelectionModel().getSelectedItem();
            openFolder(Paths.get(finder.results().get(filename)));
        };
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
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to open directory: " + directory, e);
            }
        }).start();
    }


}
