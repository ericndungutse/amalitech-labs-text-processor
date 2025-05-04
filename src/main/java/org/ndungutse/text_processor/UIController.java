package org.ndungutse.text_processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.ndungutse.text_processor.service.FileHandler;
import org.ndungutse.text_processor.service.RegexService;
import org.ndungutse.text_processor.util.AppContext;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UIController {
    @FXML
    private TextArea textArea;
    @FXML
    private TextField patternField;
    @FXML
    private TextField replaceField;
    @FXML
    private Label regexStatusLabel;
    @FXML
    private TextField batchPatternField;
    @FXML
    private TextField batchReplaceField;
    @FXML
    private Label batchRegexStatusLabel;
    @FXML
    private ListView<String> fileListView;
    @FXML
    private Button previousMatchButton;
    @FXML
    private Button nextMatchButton;
    @FXML
    private TextField delimiterField;

    @FXML
    private TextArea conditionField;

    @FXML
    private TextArea extractExprField;

    @FXML
    private Label extractStatusLabel;

    private final FileHandler fileHandler = new FileHandler();
    private final RegexService regexService = AppContext.getRegexService();
    private Path selectedFile;
    // Fields to store match indices and current match index
    private List<int[]> matchIndices = new ArrayList<>();
    private int currentMatch = 0;

    public void handleLoadFile() {
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

    // Saving Files
    @FXML
    public void handleSaveFile() {
        if (selectedFile != null) {
            handleSaveExistingFile();
        } else {
            handleSaveNewFile();
        }
    }

    @FXML
    public void handleSaveExistingFile() {
        // Check if a file has been selected (existing file)
        if (selectedFile != null) {
            try {
                // Save the content of the text area (analysis results) to the selected file
                Files.writeString(selectedFile, textArea.getText());
            } catch (IOException e) {
                textArea.setText("Error saving file: " + e.getMessage());
            }
        } else {
            textArea.setText("No file selected to save.");
        }
    }

    // Save New FIle
    @FXML
    public void handleSaveNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("newfile.txt");

        File newSelectedFile = fileChooser.showSaveDialog(null);

        if (newSelectedFile != null) {
            try {
                Files.writeString(newSelectedFile.toPath(), textArea.getText());
            } catch (IOException e) {
                textArea.setText("Error saving file: " + e.getMessage());
            }
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
        } catch (PatternSyntaxException e) {
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

    public void handlePreviousMatch() {
        if (currentMatch > 0) {
            currentMatch--;
            highlightMatch();
        }
    }

    public void handleNextMatch() {
        if (currentMatch < matchIndices.size() - 1) {
            currentMatch++;
            highlightMatch();
        }
    }

    private void highlightMatch() {
        textArea.selectRange(matchIndices.get(currentMatch)[0], matchIndices.get(currentMatch)[1]);
    }

    @FXML
    public void handleFilesSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            ObservableList<String> filePaths = FXCollections.observableArrayList();
            for (File file : selectedFiles) {
                filePaths.add(file.getAbsolutePath()); // ✅ Use full path
            }
            fileListView.setItems(filePaths); // ✅ Set full paths in ListView
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    @FXML
    public void handleBatchReplace() {
        String patternText = batchPatternField.getText();
        String replacementText = batchReplaceField.getText();

        if (patternText == null || patternText.isEmpty()) {
            batchRegexStatusLabel.setText("Pattern cannot be empty.");
            return;
        }

        ObservableList<String> selectedFilePaths = fileListView.getItems();
        if (selectedFilePaths.isEmpty()) {
            batchRegexStatusLabel.setText("No files selected.");
            return;
        }

        List<Path> filePaths = selectedFilePaths.stream()
                .map(Path::of)
                .collect(Collectors.toList());

        try {
            fileHandler.replacePatternInFiles(filePaths, patternText, replacementText);
            batchRegexStatusLabel.setText("Replaced in " + filePaths.size() + " file(s).");
        } catch (IOException | PatternSyntaxException e) {
            e.printStackTrace();
            batchRegexStatusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveDuplicatesInBatch() {
        ObservableList<String> selectedFilePaths = fileListView.getItems();
        if (selectedFilePaths.isEmpty()) {
            batchRegexStatusLabel.setText("No files selected.");
            return;
        }
        try {
            List<Path> filePaths = selectedFilePaths.stream()
                    .map(Path::of)
                    .collect(Collectors.toList());

            fileHandler.removeDuplicateInBatchOfFiles(filePaths);
            batchRegexStatusLabel.setText("Remove duplicate lines in " + filePaths.size() + " file(s).");
        } catch (IOException e) {
            batchRegexStatusLabel.setText("Error processing files.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleWordFrequency() {
        ObservableList<String> selectedFilePaths = fileListView.getItems();
        if (selectedFilePaths.isEmpty()) {
            batchRegexStatusLabel.setText("No files selected.");
            return;
        }

        List<Path> filePaths = selectedFilePaths.stream()
                .map(path -> Path.of(path))
                .collect(Collectors.toList());

        try {

            // Analyze word frequencies
            String frequencies = fileHandler.generateWordFrequency(filePaths);

            // Display result in the TextArea
            textArea.setText(frequencies);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    @FXML
    private void handleExtract() {
        ObservableList<String> selectedFilePaths = fileListView.getItems();
        if (selectedFilePaths.isEmpty()) {
            extractStatusLabel.setText("No files selected.");
            return;
        }

        String delimiter = delimiterField.getText().trim();
        String condition = conditionField.getText().trim();
        String fieldsToExtract = extractExprField.getText().trim();

        // Validate delimiter
        if (delimiter.length() != 1 || Character.isLetterOrDigit(delimiter.charAt(0))) {
            extractStatusLabel.setText("Invalid delimiter: must be a single special character.");
            return;
        }

        // Only allows: alphanumeric (with _), valid operators, optional spacing
        String conditionPattern = "^\\s*([A-Za-z0-9_]+)\\s*(<=|>=|!=|=|<|>)\\s*([A-Za-z0-9_]+)\\s*$";

        if (!condition.isEmpty() && !condition.matches(conditionPattern)) {
            extractStatusLabel
                    .setText("Invalid condition. Use alphanumeric operands and valid operator (e.g., age >= 18).");
            return;
        }

        List<Path> filePaths = selectedFilePaths.stream()
                .map(Path::of)
                .collect(Collectors.toList());

        try {

            String result = fileHandler.extractInfo(filePaths, delimiter, condition,
                    fieldsToExtract);
            textArea.setText(result);
            extractStatusLabel.setText("Extraction successful.");

            textArea.setText(result);
        } catch (IndexOutOfBoundsException e) {
            extractStatusLabel.setText("Extraction failed: " + e.getMessage());
            System.err.println("Extraction error: " + e.getMessage());
        } catch (IOException e) {
            extractStatusLabel.setText("Extraction failed: " + e.getMessage());
            System.err.println("Extraction error: " + e.getMessage());

        }
    }

}
