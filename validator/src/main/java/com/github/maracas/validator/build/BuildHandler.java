package com.github.maracas.validator.build;

import java.util.List;

/**
 * Interface in charge of handling compilation tasks.
 */
public interface BuildHandler {

    /**
     * Builds a Java project. Throws an exception if the process fails.
     */
    void build();

    /**
     * Gathers all compiler messages after trying to compile a project.
     * @return list of {@link CompilerMessage}
     */
    List<CompilerMessage> gatherCompilerMessages();
}
