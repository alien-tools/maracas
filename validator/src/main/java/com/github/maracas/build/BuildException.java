package com.github.maracas.build;

/**
 * Exception raised during the build process of a project.
 * TODO: This class has been defined already in the rest project services pkg.
 */
public class BuildException extends RuntimeException {
    /**
     * UID of the servial version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a BuildException instance.
     *
     * @param message exception message
     */
    public BuildException(String message) {
        super(message);
    }

    /**
     * Creates a BuildException instance.
     *
     * @param message exception message
     * @param cause   cause of the exception {@link Throwable}
     */
    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a BuildException instance.
     *
     * @param cause cause of the exception {@link Throwable}
     */
    public BuildException(Throwable cause) {
        super(cause);
    }
}
