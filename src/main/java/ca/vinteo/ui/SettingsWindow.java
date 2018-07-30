package ca.vinteo.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class SettingsWindow {

    private final Stage stage;

    public SettingsWindow(EventMediator eventMediator) {
        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        rootPane.setSpacing(10);

        Button testButton = new Button("Test");
        testButton.setOnAction(event -> {
            System.out.println("Hit the test button");
        });

        rootPane.getChildren().add(testButton);

        stage = new Stage();
        stage.setScene(new Scene(rootPane));
        stage.initModality(Modality.WINDOW_MODAL);

        eventMediator.setSettingsWindow(this);
    }

    public void start() {
        stage.show();
    }
}
