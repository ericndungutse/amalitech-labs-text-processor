package org.ndungutse.text_processor.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.ndungutse.text_processor.util.AppContext;

public class FileHandler {

    // Method to read the file
    public String readFile(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        AppContext.log("Read file: " + path); // Log after file read
        return content.toString();
    }

    // Method to write the file
    public void writeFile(String path, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(content);
        }
        AppContext.log("Written changes to file: " + path); // Log after file write
    }

    // Method to replace all occurrences of a pattern in text
    public String replaceAll(String pattern, String replacement, String text) throws PatternSyntaxException {
        AppContext.log("Applying replacement: Pattern = " + pattern + ", Replacement = " + replacement); // Log pattern

        // Extract matches using the regex service
        List<String> matches = AppContext.getRegexService().extractMatches(pattern, text);

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

    // Replace pattern in batch files
    public void replacePatternInFiles(List<Path> filePaths, String pattern, String replacement)
            throws IOException, PatternSyntaxException {
        AppContext.log("Starting batch replacement in files..."); // Log before batch replacement
        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());
            String updatedContent = replaceAll(pattern, replacement, content);
            writeFile(filePath.toString(), updatedContent);
            AppContext.log("Processed file: " + filePath.getFileName() + " for pattern replacement.");
        }
        AppContext.log("Batch replacement completed for " + filePaths.size() + " files.");
    }

    // Remove duplicate lines from a batch of files
    public void removeDuplicateInBatchOfFiles(List<Path> filePaths) throws IOException {
        AppContext.log("Starting batch duplicate removal in files..."); // Log before removing duplicates
        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());
            String updatedContent = removeDuplicates(content);
            writeFile(filePath.toString(), updatedContent);
            AppContext.log("Processed file: " + filePath.getFileName() + " for duplicate removal.");
        }
        AppContext.log("Batch duplicate removal completed for " + filePaths.size() + " files.");
    }

    // Method to remove duplicate lines from a string
    public String removeDuplicates(String text) {
        Set<String> result = Arrays
                .stream(text.split("\n"))
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        AppContext.log("Removed duplicates from content."); // Log after duplicates are removed
        return String.join("\n", result);
    }

    // Generate word frequency analysis from files
    public String generateWordFrequency(List<Path> filePaths) throws IOException {
        AppContext.log("Starting word frequency analysis..."); // Log before starting word frequency analysis
        Map<String, Integer> wordCounts = new HashMap<>();

        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());
            String[] words = content.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");

            for (String word : words) {
                if (!word.isBlank()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        StringBuilder resultBuilder = new StringBuilder();

        wordCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> resultBuilder
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n"));

        AppContext.log("Word frequency analysis completed."); // Log after word frequency analysis
        return resultBuilder.toString();
    }

    // Extract info based on delimiter, condition, and fields to extract
    public String extractInfo(List<Path> filePaths, String delimiter, String condition, String fieldsToExtract)
            throws IOException {

        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter must be provided.");
        }

        StringBuilder finalResult = new StringBuilder();

        for (Path filePath : filePaths) {
            AppContext.log("Processing file: " + filePath.getFileName() + " for info extraction."); // Log file
                                                                                                    // processing
            String content = readFile(filePath.toString());

            List<String> contentLines = new ArrayList<>(Arrays.asList(content.split("\n")));

            if (condition != null && !condition.isBlank()) {
                String filtered = filterLinesByCondition(contentLines, condition, delimiter);

                if (filtered.length() == 0) {
                    if (finalResult.length() > 0) {
                        finalResult.append("\n");
                    }
                    finalResult.append("No match found for condition [")
                            .append(condition)
                            .append("] in file: ")
                            .append(filePath.getFileName());
                    continue;
                }

                contentLines = Arrays.asList(filtered.split("\n"));
            }

            String result;
            if (fieldsToExtract != null && !fieldsToExtract.isBlank()) {
                List<String> fieldIndices = new ArrayList<>(Arrays.asList(fieldsToExtract.split(",")));
                result = extractFieldsFromContent(contentLines, fieldIndices, delimiter);
            } else {
                result = String.join("\n", contentLines);
            }

            if (!result.isEmpty()) {
                if (finalResult.length() > 0) {
                    finalResult.append("\n");
                }
                finalResult.append(result);
            }
        }

        AppContext.log("Info extraction completed."); // Log after info extraction
        return finalResult.toString();
    }

    // Method to filter lines based on condition and delimiter
    public String filterLinesByCondition(List<String> lines, String condition, String delimiter) {
        AppContext.log("Filtering lines by condition: " + condition); // Log condition filtering
        String[] operators = { "<=", ">=", "!=", "=", "<", ">" };
        String operatorUsed = null;

        for (String op : operators) {
            if (condition.contains(op)) {
                operatorUsed = op;
                break;
            }
        }

        if (operatorUsed == null) {
            throw new IllegalArgumentException("Invalid condition: No operator found.");
        }

        String[] operands = condition.split(operatorUsed);

        int fieldIndex;
        try {
            fieldIndex = Integer.parseInt(operands[0].trim()) - 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid field index in condition: " + operands[0]);
        }

        String expectedValue = operands[1].trim();
        List<String> result = new ArrayList<>();

        for (String line : lines) {
            String[] fields = line.trim().split(delimiter);

            if (fieldIndex >= fields.length)
                continue;

            String actualField = fields[fieldIndex].trim();
            boolean actualIsNumeric = isNumeric(actualField);
            boolean expectedIsNumeric = isNumeric(expectedValue);

            boolean matches;

            if (actualIsNumeric && expectedIsNumeric) {
                double actualNum = Double.parseDouble(actualField);
                double expectedNum = Double.parseDouble(expectedValue);

                matches = switch (operatorUsed) {
                    case "=" -> actualNum == expectedNum;
                    case "!=" -> actualNum != expectedNum;
                    case ">" -> actualNum > expectedNum;
                    case "<" -> actualNum < expectedNum;
                    case ">=" -> actualNum >= expectedNum;
                    case "<=" -> actualNum <= expectedNum;
                    default -> false;
                };
            } else {
                if (!operatorUsed.equals("=")) {
                    throw new IllegalArgumentException("Invalid operator for string comparison: " + operatorUsed);
                }
                matches = actualField.equalsIgnoreCase(expectedValue);
            }

            if (matches) {
                result.add(line);
            }
        }

        return result.stream().collect(Collectors.joining("\n"));
    }

    // Extract certain fields based on index
    private String extractFieldsFromContent(List<String> contentList, List<String> fieldsIndices,
            String delimiter) {
        return contentList.stream()
                .map(line -> {
                    List<String> lineArr = Arrays.asList(line.split(Pattern.quote(delimiter)));
                    return fieldsIndices.stream()
                            .map(field -> lineArr.get(Integer.parseInt(field.trim()) - 1))
                            .collect(Collectors.joining(delimiter));
                }).collect(Collectors.joining("\n"));
    }

    private boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
