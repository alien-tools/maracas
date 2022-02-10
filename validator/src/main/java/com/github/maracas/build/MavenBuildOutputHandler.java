package com.github.maracas.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static final Logger logger = LogManager.getLogger(MavenBuildHandler.class);

    /**
     * RegEx pattern of a Maven error in the build trace
     */
    private static final String ERROR_PATTERN = "\\[ERROR\\]\\s+(.+):\\[(\\d+),(\\d+)\\]\\s+(.+)";

    /**
     * List of compiler messages
     */
    private List<CompilerMessage> messages;

    /**
     * Creates a MavenBuildOutputHandler instance.
     */
    public MavenBuildOutputHandler() {
        this.messages = new ArrayList<CompilerMessage>();
    }

    /**
     * Returns the list of compiler messages extracted from the build trace.
     *
     * @return list of {@link CompilerMessage}
     */
    public List<CompilerMessage> getMessages() {
        return this.messages;
    }

    @Override
    public void consumeLine(String currentLine) throws IOException {
        Pattern errorPattern = Pattern.compile(ERROR_PATTERN);

        if (currentLine.startsWith("[ERROR]")) {
            Matcher errorMatcher = errorPattern.matcher(currentLine);

            if (errorMatcher.matches()) {
                String path = errorMatcher.group(1);
                int line = Integer.parseInt(errorMatcher.group(2));
                int column = Integer.parseInt(errorMatcher.group(3));
                String message = errorMatcher.group(4);

                CompilerMessage compilerMessage =
                    new CompilerMessage(path, line, column, message);
                messages.add(compilerMessage);
            }
        } else {
            logger.warn(String.format("The following line couldn't be parsed: %s",
                currentLine));
        }
    }
}
