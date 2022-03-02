package com.github.maracas.validator.accuracy;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import japicmp.model.JApiCompatibilityChange;

/**
 * Match options to exclude unsupported breaking changes.
 */
public record MatcherOptions(
    Map<JApiCompatibilityChange, String> excludedBreakingChanges) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MatcherOptions.class);

    /**
     * Creates a MatcherOptions instance.
     */
    public MatcherOptions() {
        this(new HashMap<JApiCompatibilityChange, String>());
    }

    /**
     * Exclude a {@link JApiCompatibilityChange} instance from the matching
     * process.
     *
     * @param change {@link JApiCompatibilityChange} instance to exclude
     * @param regex  RegEx pattern to match the breaking change against a
     *               compiler message
     */
    public void excludeBreakingChange(JApiCompatibilityChange change, String regex) {
        excludedBreakingChanges.put(change, regex);
    }
}
