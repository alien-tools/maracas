package com.github.maracas.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.build.CompilerMessage;
import com.github.maracas.matchers.AccuracyCase.AccuracyType;

/**
 * Type of matcher based on {@link BrokenUse} and {@link CompilerMessage}
 * object locations.
 */
public class LocationMatcher implements Matcher {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(LocationMatcher.class);

    @Override
    public Collection<AccuracyCase> match(Collection<BrokenUse> brokenUses, List<CompilerMessage> messages) {
        Map<String, List<CompilerMessage>> messagesMap = Matcher.messagesToMap(messages);
        Collection<AccuracyCase> matches = new ArrayList<AccuracyCase>();

        for (BrokenUse brokenUse : brokenUses) {
            String path = brokenUse.element().getPosition().getFile().getAbsolutePath();
            int column = brokenUse.element().getPosition().getColumn();
            int line = brokenUse.element().getPosition().getLine();

            List<CompilerMessage> currentMessages = messagesMap.get(path);
            List<CompilerMessage> matchedMessages = new ArrayList<CompilerMessage>();

            if (currentMessages != null)
                for (CompilerMessage message : currentMessages) {
                    if (line == message.line()) {
                        matchedMessages.add(message);
                    }
                }

            if (!matchedMessages.isEmpty()) {
                matches.add(new AccuracyCase(brokenUse, matchedMessages, AccuracyType.TRUE_POSITIVE));
                System.out.println(AccuracyType.TRUE_POSITIVE);
            } else {
                matches.add(new AccuracyCase(brokenUse, null, AccuracyType.FALSE_POSITIVE));
                System.out.println(AccuracyType.FALSE_POSITIVE);
            }

        }
        return matches;
    }
}
