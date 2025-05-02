package org.ndungutse.text_processor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ndungutse.text_processor.service.FileHandler;
import org.ndungutse.text_processor.service.RegexService;
import org.ndungutse.text_processor.util.AppContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class UIController {
    @FXML
    private TextArea textArea;
    @FXML private TextField patternField;
    @FXML private TextField replaceField;
    @FXML private Label regexStatusLabel;
    @FXML
    private Button previousMatchButton;
    @FXML
    private Button nextMatchButton;


    private final FileHandler fileHandler = new FileHandler();
    private final RegexService regexService = AppContext.getRegexService();
    private Path selectedFile;
    // Fields to store match indices and current match index
    private List<int[]> matchIndices = new ArrayList<>();
    private int currentMatch = 0;

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

        try {
        String pattern = patternField.getText();
        String text = textArea.getText();

        this.matchIndices = regexService.getMatchIndices(pattern, text);
        int count = this.matchIndices.size();

        if (count == 0) {
            regexStatusLabel.setText("No matches found.");
        } else {
            regexStatusLabel.setText("✅ Matches found: " + count);
            previousMatchButton.setDisable(false);
            nextMatchButton.setDisable(false);
            highlightMatch();
        }
        }catch (PatternSyntaxException e){
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleReplaceAll() {
        String pattern = patternField.getText();
        String replacement = replaceField.getText();
        String text = textArea.getText();

        String newText = fileHandler.replaceAll(pattern, replacement, text);
        textArea.setText(newText);
        regexStatusLabel.setText("✅ All matches replaced.");
    }

    public void handlePreviousMatch(ActionEvent event) {
        if (currentMatch > 0) {
            currentMatch--;
            highlightMatch();
        }
    }

    public void handleNextMatch(ActionEvent event) {
        if (currentMatch < matchIndices.size() - 1) {
            currentMatch++;
            highlightMatch();
        }
    }

    private void highlightMatch() {
        textArea.selectRange(matchIndices.get(currentMatch)[0], matchIndices.get(currentMatch)[1]);
    }
}
