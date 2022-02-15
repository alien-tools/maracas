package com.github.maracas.validator.accuracy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;

/**
 * Matches broken uses with compiler messages.
 */
public interface Matcher {
    /**
     * Computes a collection of {@link AccuracyCase} objects. The collection
     * comes from matching {@link BrokenUse} and {@link CompilerMessage} objects.
     * TODO: feels weird to have collections and lists. Use the same?
     *
     * @param brokenUses collection of {@link BrokenUse} objects
     * @param messages   list of {@link CompilerMessage} objects
     * @return collection of {@link AccuracyCase} objects
     */
    Collection<AccuracyCase> match(Collection<BrokenUse> brokenUses, List<CompilerMessage> messages);

    /**
     * Transforms a list of {@link CompilerMessage} objects into a map. The keys
     * are paths of a compilation unit and the values are a list of
     * {@link CompilerMessage} objects reported in such a file.
     *
     * @param messages list of {@link CompilerMessage} objects
     * @return map of paths and {@link CompilerMessage} objects
     */
    static Map<String, List<CompilerMessage>> messagesToMap(List<CompilerMessage> messages) {
        Map<String, List<CompilerMessage>> messagesMap = new HashMap<String, List<CompilerMessage>>();

        for (CompilerMessage message : messages) {
            String key = message.path();
            if (!messagesMap.containsKey(key)) {
                messagesMap.put(key, List.of(message));
            } else {
                List<CompilerMessage> currentMessages = new ArrayList(messagesMap.get(key));
                currentMessages.add(message);
                messagesMap.replace(key, currentMessages);
            }
        }
        return messagesMap;
    }
}
