package ca.vinteo.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Set;

public class MainWindow {

    private final MainWindowHandler handler;
    private final Stage mainStage;

    public MainWindow(Stage mainStage, MainWindowHandler handler) {
        this.handler = handler;
        this.mainStage = mainStage;
    }

    public void start(Set<String> listItems) {
        ObservableList<String> results = FXCollections.observableArrayList();
        results.addAll(listItems);

        TextField textField = new TextField();
        textField.textProperty().addListener(handler.handleTextFieldChange(results));

        ListView<String> resultView = new ListView<>(results);
        resultView.setOrientation(Orientation.VERTICAL);
        resultView.getSelectionModel().selectedItemProperty().addListener(handler.handleResultViewChange());

        resultView.setOnMouseClicked(handler.handleListItemMouseClick());

        Button playButton = new Button("Play with VLC");
        playButton.setOnAction(handler.handlePlayButtonPress(resultView));

        Button openFolderButton = new Button("Open folder");
        openFolderButton.setOnAction(handler.handleOpenFolderButtonPress(resultView));

        this.mainStage.setTitle("Vinteo");

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

        this.mainStage.setScene(new Scene(rootPane, 500, 700));

    }

}
