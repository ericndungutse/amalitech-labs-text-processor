package org.ndungutse.text_processor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexService {
    public boolean isValidPattern(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public List<int[]> getMatchIndices(String pattern, String text) throws PatternSyntaxException {
        List<int[]> matches = new ArrayList<>();
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(text);

        while (matcher.find()) {
            matches.add(new int[] { matcher.start(), matcher.end() });
        }
        return matches;
    }

    public List<String> extractMatches(String pattern, String text) throws PatternSyntaxException {
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(text);

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }

        return results;
    }

}