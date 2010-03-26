package org.apache.hise.utils;

import java.util.Map;

/**
 * Merges presentation parameters into template strings.
 */
public interface TemplateEngine {

    /**
     * Replaces occurrences of "$key$" in a template string with values provided in presentationParameters.
     * Removes blocks starting with ?IF-key? and ending with ?ENDIF-key? if key is not present in presentationParameters.
     * @param template The template String.
     * @param presentationParameterValues Presentation parameters.
     * @return The template string with filled in values.
     */
    String merge(String template, Map<String, Object> presentationParameterValues);

}