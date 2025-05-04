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
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.ndungutse.text_processor.util.AppContext;

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

    // Remove Duplicate lines in a patch of files
    public void removeDuplicateInBatchOfFiles(List<Path> filePaths)
            throws IOException {
        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());
            String updatedContent = removeDuplicates(content);
            writeFile(filePath.toString(), updatedContent);
            System.out.println("Processed file: " + filePath.getFileName());
        }
    }

    // Remove Duplicates
    public String removeDuplicates(String text) {
        Set<String> result = Arrays
                .stream(text.split("\n"))
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return String.join("\n", result);
    }

    // Words frequency analysis
    public String generateWordFrequency(List<Path> filePaths) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();

        // Process each file
        for (Path filePath : filePaths) {
            String content = readFile(filePath.toString());

            // Remove non-word characters and split words by spaces
            String[] words = content.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");

            // Count occurrences of each word
            for (String word : words) {
                if (!word.isBlank()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Use StringBuilder to format the result
        StringBuilder resultBuilder = new StringBuilder();

        // Sort by word frequency in descending order
        wordCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> resultBuilder
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n"));

        // Return the result as a string
        return resultBuilder.toString();
    }

    public String extractInfo(List<Path> filePaths, String delimiter, String condition, String fieldsToExtract)
            throws IOException {

        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter must be provided.");
        }

        StringBuilder finalResult = new StringBuilder();

        for (Path filePath : filePaths) {
            // Read file content
            String content = readFile(filePath.toString());

            // Split into lines
            List<String> contentLines = new ArrayList<>(Arrays.asList(content.split("\n")));

            // Filter lines if condition is present
            if (condition != null && !condition.isBlank()) {
                contentLines = Arrays.asList(filterLinesByCondition(contentLines, condition, delimiter).split("\n"));
            }

            // Select fields if fieldsToExtract is present
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

        return finalResult.toString();
    }

    // FIlter content/lines by condition based on field defined by delimiter
    public String filterLinesByCondition(List<String> lines, String condition, String delimiter) {
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
                // Handle string comparison (only "=" operator allowed)
                if (!operatorUsed.equals("=")) {
                    throw new IllegalArgumentException("Invalid operator for string comparison: " + operatorUsed);
                }

                // Perform case insensitive string comparison for equality
                matches = actualField.equalsIgnoreCase(expectedValue);
            }

            if (matches) {
                result.add(line);
            }
        }

        return result.stream().collect(Collectors.joining("\n"));
    }

    // Select certain fields
    private String extractFieldsFromContent(List<String> contentList, List<String> fieldsIndices,
            String delimiter) {
        return contentList.stream()
                .map(line -> {
                    List<String> lineArr = Arrays.asList(line.split(delimiter));
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
