package org.ndungutse.text_processor.service;

import org.ndungutse.text_processor.util.AppContext;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FileHandler {
    private final RegexService regexService = AppContext.getRegexService();

    public String readFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public void writeFile(String path, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(content);
        }
    }

    public String replaceAll(String pattern, String replacement, String text) throws PatternSyntaxException {
        // Extract matches using the regex service
        List<String> matches = regexService.extractMatches(pattern, text);

        // If there are no matches, return the original text
        if (matches.isEmpty()) {
            return text;
        }

        // Create a new StringBuilder to build the updated text
        StringBuilder updatedText = new StringBuilder(text);

        // Iterate over each match and replace it in the text
        for (String match : matches) {
            // Replace all occurrences of the match with the replacement string
            int index = updatedText.indexOf(match);
            while (index != -1) {
                updatedText.replace(index, index + match.length(), replacement);
                index = updatedText.indexOf(match, index + replacement.length());
            }
        }

        return updatedText.toString();
    }

    public void replacePatternInFiles(List<Path> filePaths, String pattern, String replacement)
            throws IOException, PatternSyntaxException {
        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());
            String updatedContent = replaceAll(pattern, replacement, content);
            writeFile(filePath.toString(), updatedContent);
            System.out.println("Processed file: " + filePath.getFileName());
        }
    }
}
