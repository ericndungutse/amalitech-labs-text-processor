package org.ndungutse.text_processor.util;

import org.ndungutse.text_processor.service.RegexService;

public class AppContext {
    private static RegexService regexService;

    private AppContext() {
    }
    public synchronized static RegexService getRegexService() {
        if (regexService == null) {
            regexService = new RegexService();
        }
        return regexService;
    }
}
