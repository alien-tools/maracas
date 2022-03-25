package com.github.maracas.forges.clone;

public class CloneException extends RuntimeException {
    public CloneException(String message) {
        super(message);
    }

    public CloneException(Throwable cause) {
        super(cause);
    }
}
