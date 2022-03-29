package com.github.maracas.forges;

public class ForgeException extends RuntimeException {
    public ForgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForgeException(Throwable cause) {
        super(cause);
    }
}
