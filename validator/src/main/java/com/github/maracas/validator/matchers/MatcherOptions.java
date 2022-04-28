package com.github.maracas.validator.matchers;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import japicmp.model.JApiCompatibilityChange;

/**
 * Match options to exclude unsupported breaking changes.
 */
@SuppressWarnings("rawtypes")
public record MatcherOptions(
	Set<Enum> excludedBreakingChanges,
    Set<String> excludedCompilerMessage) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MatcherOptions.class);

    /**
     * Creates a MatcherOptions instance.
     */
    public MatcherOptions() {
        this(new HashSet<Enum>(), new HashSet<String>());
    }

    /**
     * Exclude a {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     * instance from the matching process.
     *
     * @param change {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     *               instance to exclude
     */
    public void excludeBreakingChange(Enum change) {
    	excludedBreakingChanges.add(change);
    }

    /**
     * Exclude a compiler message from the matching process based on a regex pattern.
     *
     * @param regex  RegEx pattern to match the breaking change against a
     *               compiler message
     */
    public void excludeCompilerMessage(String regex) {
    	excludedCompilerMessage.add(regex);
    }

    /**
     * Exclude compiler messages from the matching process based on a set of
     * regex patterns.
     *
     * @param regexes  Set of RegEx patterns to match breaking changes against
     *                 compiler messages
     */
    public void excludeCompilerMessage(Set<String> regexes) {
    	excludedCompilerMessage.addAll(regexes);
    }
}
