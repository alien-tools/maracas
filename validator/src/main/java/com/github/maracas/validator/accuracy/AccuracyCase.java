package com.github.maracas.validator.accuracy;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.validator.accuracy.AccuracyCase.AccuracyType;
import com.github.maracas.validator.build.CompilerMessage;

/**
 * Accuracy case reporting (non-)matches between {@link BrokenUse} and
 * {@link CompilerMessage} objects.
 */
public record AccuracyCase(
    BrokenUse brokenUse,
    List<CompilerMessage> messages,
    AccuracyType type) {
    /**
     * Type of accuracy case
     */
    public enum AccuracyType {
        FALSE_POSITIVE,
        TRUE_POSITIVE,
        FALSE_NEGATIVE
    }

    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(AccuracyCase.class);

    /**
     * Creates an AccuracyCase instance. Checks if the {@link BrokenUse} object
     * is defined or the list of {@link CompilerMessage} objects (at least one).
     *
     * @param brokenUse {@link BrokenUse} object
     * @param messages  list of {@link CompilerMessage} objects
     * @param type      {AccuracyType} of the case
     */
    public AccuracyCase(BrokenUse brokenUse, List<CompilerMessage> messages, AccuracyType type) {
        assert brokenUse != null || (messages != null && !messages.isEmpty());
        assert type != null;

        this.brokenUse = brokenUse;
        this.messages = messages;
        this.type = type;
    }
}
