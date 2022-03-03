package com.github.maracas.validator.build;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Record representing a Java compiler message.
 */
public record CompilerMessage(
    String path,
    int line,
    int column,
    String message) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(CompilerMessage.class);

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (getClass() != that.getClass())
            return false;

        CompilerMessage other = (CompilerMessage) that;
        return Objects.equals(path, other.path)
            && Objects.equals(line, other.line)
            && Objects.equals(message.trim().toLowerCase(),
                other.message.trim().toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            path,
            line,
            message.trim().toLowerCase());
    }
}
