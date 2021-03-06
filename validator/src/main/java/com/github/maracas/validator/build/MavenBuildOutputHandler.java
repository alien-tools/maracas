package com.github.maracas.validator.build;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

/**
 * Implementation of the {@link InvocationOutputHandler}. It gathers error
 * messages from the Maven build trace.
 */
public class MavenBuildOutputHandler implements InvocationOutputHandler {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MavenBuildOutputHandler.class);

    /**
     * RegEx pattern of a Maven error or warning in the build trace
     */
    private static final String MESSAGE_PATTERN = "\\[(ERROR|WARNING)\\]\\s+(.+):\\[(\\d+),(\\d+)\\]\\s+(.+)";

    /**
     * Set of compiler messages
     */
    private Set<CompilerMessage> messages;

    /**
     * Creates a MavenBuildOutputHandler instance.
     */
    public MavenBuildOutputHandler() {
        this.messages = new HashSet<CompilerMessage>();
    }

    /**
     * Returns the set of compiler messages extracted from the build trace.
     *
     * @return list of {@link CompilerMessage}
     */
    public Set<CompilerMessage> getMessages() {
        return this.messages;
    }

    @Override
    public void consumeLine(String currentLine) throws IOException {
        Pattern errorPattern = Pattern.compile(MESSAGE_PATTERN);

        if (currentLine.startsWith("[ERROR]") || currentLine.startsWith("[WARNING]")) {
            Matcher errorMatcher = errorPattern.matcher(currentLine);

            if (errorMatcher.matches()) {
                String path = errorMatcher.group(2);
                int line = Integer.parseInt(errorMatcher.group(3));
                int column = Integer.parseInt(errorMatcher.group(4));
                String message = errorMatcher.group(5);

                CompilerMessage compilerMessage =
                    new CompilerMessage(path, line, column, message);
                messages.add(compilerMessage);
            }
        } else {
            logger.warn("The following line couldn't be parsed: {}", currentLine);
        }
    }
}
