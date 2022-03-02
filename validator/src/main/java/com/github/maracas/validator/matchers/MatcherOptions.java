package com.github.maracas.validator.matchers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import japicmp.model.JApiCompatibilityChange;

/**
 * Match options to exclude unsupported breaking changes.
 */
public record MatcherOptions(
    Map<Enum, String> excludedBreakingChanges) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MatcherOptions.class);

    /**
     * Creates a MatcherOptions instance.
     */
    public MatcherOptions() {
        this(new HashMap<Enum, String>());
    }

    /**
     * Exclude a {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     * instance from the matching process.
     *
     * @param change {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     *               instance to exclude
     * @param regex  RegEx pattern to match the breaking change against a
     *               compiler message
     */
    public void excludeBreakingChange(Enum change, String regex) {
        excludedBreakingChanges.put(change, regex);
    }
}
