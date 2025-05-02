package org.ndungutse.text_processor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ndungutse.text_processor.service.FileHandler;
import org.ndungutse.text_processor.service.RegexService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UIController {
    @FXML
    private TextArea textArea;
    @FXML private TextField patternField;
    @FXML private TextField replaceField;
    @FXML private Label regexStatusLabel;


    private final FileHandler fileHandler = new FileHandler();
    private final RegexService regexService = new RegexService();
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

    @FXML
    private void handleMatchPattern() {
        String pattern = patternField.getText();
        String text = textArea.getText();

        if (!regexService.isValidPattern(pattern)) {
            regexStatusLabel.setText("❌ Invalid regex pattern.");
            return;
        }

        List<int[]> matches = regexService.getMatchIndices(pattern, text);
        int count = matches.size();

        if (count == 0) {
            regexStatusLabel.setText("No matches found.");
        } else {
            regexStatusLabel.setText("✅ Matches found: " + count);
        }
    }

    @FXML
    private void handleReplaceAll() {
        String pattern = patternField.getText();
        String replacement = replaceField.getText();
        String text = textArea.getText();

        if (!regexService.isValidPattern(pattern)) {
            regexStatusLabel.setText("❌ Invalid regex pattern.");
            return;
        }

        String newText = regexService.replaceAll(pattern, replacement, text);
        textArea.setText(newText);
        regexStatusLabel.setText("✅ All matches replaced.");
    }
}
