package ca.vinteo.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AddDirectoryWindow {

    private final Stage stage;
    private final TextField directoryText;

    public AddDirectoryWindow(EventMediator eventMediator) {
        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10, 10, 10, 10));
        rootPane.setSpacing(10);

        directoryText = new TextField();
        Button okButton = new Button("Ok");
        Button cancelButton = new Button("Cancel");

        okButton.setOnAction(event -> eventMediator.onDirectoryDialogOkButtonClicked(directoryText.getText()));
        cancelButton.setOnAction(event -> eventMediator.onDirectoryDialogCancelButtonClicked());

        GridPane buttonPane = new GridPane();
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setAlignment(Pos.BASELINE_CENTER);
        buttonPane.add(okButton, 0, 0);
        buttonPane.add(cancelButton, 1, 0);

        rootPane.getChildren().addAll(directoryText, buttonPane);

        stage = new Stage();
        stage.setScene(new Scene(rootPane));
        stage.initModality(Modality.WINDOW_MODAL);

        eventMediator.setAddDirectoryWindow(this);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
        directoryText.clear();
    }

}
