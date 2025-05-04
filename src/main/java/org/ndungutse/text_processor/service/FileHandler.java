package org.ndungutse.text_processor.service;

import org.ndungutse.text_processor.util.AppContext;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        List<List<String>> resultList = new ArrayList<>();

        for (Path filePath : filePaths) {
            // get the file content
            String content = readFile(filePath.toString());

            // Indices
            List<String> fieldsIndices = new ArrayList<>(Arrays.asList(fieldsToExtract.split(",")));

            // [ --- --- ---- ----, ----- ---- -----]
            List<String> contentList = new ArrayList<>(Arrays.asList(content.split("\n")));

            resultList = contentList.stream()
                    .map(line -> {
                        List<String> lineArr = Arrays.asList(line.split(","));
                        return fieldsIndices.stream()
                                .map(field -> lineArr.get(Integer.parseInt(field) - 1))
                                .toList();
                    })
                    .toList();
        }

        return resultList.stream().map(res -> String.join(",", res)).collect(Collectors.joining("\n"));

    }

}
