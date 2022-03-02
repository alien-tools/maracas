package com.github.maracas.validator.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.brokenUse.APIUse;
import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.validator.accuracy.AccuracyCase;
import com.github.maracas.validator.accuracy.AccuracyCase.AccuracyType;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.cases.CompChangesMatcherFilter;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.SourcePosition;

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
    public Collection<AccuracyCase> match(Set<BrokenUse> brokenUses, Set<CompilerMessage> messages, MatcherOptions opts) {
        MatcherFilter filter = new CompChangesMatcherFilter(opts);
        Set<BrokenUse> filteredBrokenUses = filter.filterBrokenUses(brokenUses);
        Set<CompilerMessage> filteredMessages = filter.filterCompilerMessages(messages);

        Map<String, List<CompilerMessage>> messagesMap = Matcher.messagesToMap(filteredMessages);
        Collection<AccuracyCase> cases       = new ArrayList<AccuracyCase>();
        Set<CompilerMessage> matchedMessages = new HashSet<CompilerMessage>();

        for (BrokenUse brokenUse : filteredBrokenUses) {
            // Handle special cases that are not detected by the compiler
            if (isDeprecatedImport(brokenUse)) {
                cases.add(new AccuracyCase(brokenUse, new ArrayList<CompilerMessage>(), AccuracyType.TRUE_POSITIVE));
                continue;
            }

            SourcePosition position = brokenUse.element().getPosition();
            String path = position.getFile().getAbsolutePath();
            int line = position.getLine();

            List<CompilerMessage> currentMessages = messagesMap.get(path);
            List<CompilerMessage> currentMatchedMessages = new ArrayList<CompilerMessage>();

            if (currentMessages != null)
                for (CompilerMessage message : currentMessages) {
                    if (line == message.line())
                        currentMatchedMessages.add(message);
                }

            if (!currentMatchedMessages.isEmpty())
                cases.add(new AccuracyCase(brokenUse, currentMatchedMessages, AccuracyType.TRUE_POSITIVE));
            else
                cases.add(new AccuracyCase(brokenUse, null, AccuracyType.FALSE_POSITIVE));

            matchedMessages.addAll(currentMatchedMessages);
        }

        Set<CompilerMessage> unmatchedMessages = new HashSet<CompilerMessage>(filteredMessages);
        unmatchedMessages.removeAll(matchedMessages);
        for (CompilerMessage message : unmatchedMessages)
            cases.add(new AccuracyCase(null, List.of(message), AccuracyType.FALSE_NEGATIVE));

        return cases;
    }

    /**
     * Verifies if the broken use points to an ANNOTATION_DEPRECATED_ADDED
     * breaking change and an IMPORT API use. See issue #24.
     *
     * @param brokenUse {@link BrokenUse} instance
     * @return true if the conditions are met, false otherwise
     */
    private boolean isDeprecatedImport(BrokenUse brokenUse) {
        return brokenUse.change().equals(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED)
            && brokenUse.use().equals(APIUse.IMPORT);
    }
}
