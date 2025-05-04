package org.ndungutse.text_processor.util;

import org.ndungutse.text_processor.service.RegexService;
import javafx.scene.control.TextArea;

public class AppContext {
    private static RegexService regexService;
    private static TextArea logTextArea; // Static TextArea to access it globally

    private AppContext() {
    }

    public synchronized static RegexService getRegexService() {
        if (regexService == null) {
            regexService = new RegexService();
        }
        return regexService;
    }

    public static void setLogTextArea(TextArea logTextArea) {
        if (AppContext.logTextArea == null) {
            AppContext.logTextArea = logTextArea;
        }
    }

    public static void log(String message) {
        if (logTextArea != null) {
            logTextArea.appendText(message + "\n");
        }
    }
}
