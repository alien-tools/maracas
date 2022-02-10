package com.github.maracas.build;

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
}
