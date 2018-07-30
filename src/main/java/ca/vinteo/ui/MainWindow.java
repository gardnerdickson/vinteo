package ca.vinteo.ui;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class MainWindow {

    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private final Stage mainStage;
    private final EventMediator eventMediator;

    private final ObservableList<String> resultItems;

    public MainWindow(Stage mainStage, EventMediator eventMediator, Collection<String> items) {
        this.mainStage = mainStage;
        this.eventMediator = eventMediator;
        this.resultItems = FXCollections.observableArrayList(items);

        eventMediator.setMainWindow(this);
    }

    public void setup() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem separatorItem = new SeparatorMenuItem();
        MenuItem settingsItem = new MenuItem("Settings...");

        exitItem.setOnAction(event -> eventMediator.onExitMenuItemClicked());
        settingsItem.setOnAction(event -> eventMediator.onSettingsMenuItemClicked());

        fileMenu.getItems().addAll(settingsItem, separatorItem, exitItem);
        menuBar.getMenus().add(fileMenu);

        TextField textField = new TextField();
        textField.textProperty().addListener((observable, oldValue, newValue) -> eventMediator.onSearchQueryChanged(newValue));

        ListView<String> resultView = new ListView<>(resultItems);
        resultView.setOrientation(Orientation.VERTICAL);
        resultView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            eventMediator.onResultItemSelectionChanged();
        });

        resultView.setOnMouseClicked((event) -> {
            LabeledText selectedItem = (LabeledText) event.getTarget();
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                eventMediator.onResultItemDoubleClick(selectedItem.getText());
            } else if (event.isControlDown() && event.getButton() == MouseButton.PRIMARY) {
                eventMediator.onResultItemControlClick(selectedItem.getText());
            }
        });

        Button playButton = new Button("Play with VLC");
        playButton.setOnAction(event ->  {
            eventMediator.onPlayButtonPressed(resultView.getSelectionModel().getSelectedItem());
        });

        Button openFolderButton = new Button("Open folder");
        openFolderButton.setOnAction(event -> {
            eventMediator.onOpenFolderButtonPressed(resultView.getSelectionModel().getSelectedItem());
        });

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

        rootPane.getChildren().addAll(menuBar, textField, resultView, buttonPane);
        VBox.setVgrow(resultView, Priority.ALWAYS);

        this.mainStage.setScene(new Scene(rootPane, 500, 700));
    }

    public void updateResultView(List<String> items) {
        resultItems.clear();
        resultItems.addAll(items);
    }

    public void close() {
        this.mainStage.close();
    }

}
