package ca.vinteo.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;


public class SettingsWindow {

    private final Stage stage;
    private final ListView<String> directoryListView;
    private ObservableList<String> directories;

    public SettingsWindow(EventMediator eventMediator) {
        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        rootPane.setSpacing(10);

        Label directoryLabel = new Label("Directories");
        directories = FXCollections.observableArrayList();
        directoryListView = new ListView<>(directories);
        directoryListView.setOrientation(Orientation.VERTICAL);

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> eventMediator.onSettingsWindowAddDirectoryButtonClicked());

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> eventMediator.onSettingsWindowRemoveDirectories(directoryListView.getSelectionModel().getSelectedIndices()));
        removeButton.setDisable(true);

        directoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                removeButton.setDisable(true);
            } else {
                removeButton.setDisable(false);
            }
        });

        GridPane buttonPane = new GridPane();
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setAlignment(Pos.BASELINE_CENTER);
        buttonPane.add(addButton, 0, 0);
        buttonPane.add(removeButton, 1, 0);

        rootPane.getChildren().addAll(directoryLabel, directoryListView, buttonPane);

        stage = new Stage();
        stage.setScene(new Scene(rootPane));
        stage.initModality(Modality.WINDOW_MODAL);

        eventMediator.setSettingsWindow(this);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.close();
    }

    public void removeDirectories(List<Integer> indices) {
        indices.forEach(index -> directoryListView.getItems().remove(index.intValue()));
    }

    public void setDirectories(List<String> directories) {
        this.directories.clear();
        this.directories.addAll(directories);
    }
}
