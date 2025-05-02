package org.ndungutse.text_processor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void handleLoadFile(ActionEvent event) {

    }

    public void handleSaveFile(ActionEvent event) {
    }
}