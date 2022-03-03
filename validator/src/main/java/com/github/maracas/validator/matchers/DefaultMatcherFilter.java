package com.github.maracas.validator.matchers;

import java.util.Set;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;

/**
 * Default matcher filter. It returns the set of broken uses and compiler messages
 * without any modification.
 */
public class DefaultMatcherFilter extends MatcherFilter {
    /**
     * Creates a DefaultMatcherFilter instance.
     *
     * @param opts set of {@link MatcherOptions}
     */
    public DefaultMatcherFilter(MatcherOptions opts) {
        super(opts);
    }

    @Override
    public Set<BrokenUse> filterBrokenUses(Set<BrokenUse> brokenUses) {
        return brokenUses;
    }

    @Override
    public Set<CompilerMessage> filterCompilerMessages(Set<CompilerMessage> messages) {
        return messages;
    }

}
