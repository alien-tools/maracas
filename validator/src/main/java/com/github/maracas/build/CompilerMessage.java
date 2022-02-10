package com.github.maracas.build;

import java.util.Map;

/**
 * Record representing a Java compiler message.
 */
public record CompilerMessage(
    String path,
    int line,
    int column,
    String message,
    Map<String, String> parameters) {

}
