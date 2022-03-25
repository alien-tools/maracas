package com.github.maracas.forges.build;

public class BuildException extends RuntimeException {
    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
