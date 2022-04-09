package com.github.maracas.validator.matchers;

import java.util.Set;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;

/**
 * Class in charge of filtering out broken uses and compiler messages based on
 * a set of {@link MatcherOptions}.
 */
public abstract class MatcherFilter {
    /**
     * Set of matcher options to filter broken uses and compiler messages
     */
    protected MatcherOptions opts;

    /**
     * Creates a MatcherFilter instance.
     *
     * @param opts set of {@link MatcherOptions}
     */
    public MatcherFilter(MatcherOptions opts) {
        this.opts = opts;
    }

    /**
     * Filters out the set of {@link BrokenUse} instances based on the
     * definition of the {@link MatcherOptions} field.
     *
     * @param brokenUses set of {@link BrokenUse} instances
     * @return filtered set fo {@link BrokenUse} instances
     */
    public abstract Set<BrokenUse> filterBrokenUses(Set<BrokenUse> brokenUses);

    /**
     * Filters out the set of {@link CompilerMessage} instances based on the
     * definition of the {@link MatcherOptions} field.
     *
     * @param messages set of {@link CompilerMessage} instances
     * @return filtered set of {@link CompilerMessage} instances
     */
    public abstract Set<CompilerMessage> filterCompilerMessages(Set<CompilerMessage> messages);
}
