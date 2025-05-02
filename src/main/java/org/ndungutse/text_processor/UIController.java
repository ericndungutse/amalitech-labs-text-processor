package org.ndungutse.text_processor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ndungutse.text_processor.service.FileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UIController {
    @FXML
    private TextArea textArea;

    private final FileHandler fileHandler = new FileHandler();
    private Path selectedFile;

    public void handleLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        // Show open file dialog
        selectedFile = fileChooser.showOpenDialog(new Stage()).toPath();

        if (selectedFile != null) {
            try {
                // Read file content
                String content = Files.readString(selectedFile);
                textArea.setText(content);
            } catch (IOException e) {
                textArea.setText("Error loading file: " + e.getMessage());
            }
        }
    }

    public void handleSaveFile(ActionEvent event) {
        if (selectedFile != null) try {
            // Save content back to the file
            Files.writeString(selectedFile, textArea.getText());
        } catch (IOException e) {
            textArea.setText("Error saving file: " + e.getMessage());
        }
    }
}
