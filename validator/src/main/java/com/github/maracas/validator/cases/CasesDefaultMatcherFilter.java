package com.github.maracas.validator.cases;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;

import japicmp.model.JApiCompatibilityChange;

/**
 * CompChanges matcher filter to filter out broken uses and compiler messages
 * based on package naming conventions
 */
public class CasesDefaultMatcherFilter extends MatcherFilter {
    /**
     * Creates a CompChangesMatcherFilter instance.
     *
     * @param opts set of {@link MatcherOptions}
     */
    public CasesDefaultMatcherFilter(MatcherOptions opts) {
        super(opts);
    }

    @Override
	public Set<BrokenUse> filterBrokenUses(Set<BrokenUse> brokenUses) {
		Set<BrokenUse> filteredBrokenUses = new HashSet<BrokenUse>();

        if (opts != null && !opts.excludedBreakingChanges().isEmpty()) {
            for (BrokenUse bu : brokenUses) {
                JApiCompatibilityChange bc = bu.change();
                String path = bu.element().getPosition().getFile().getAbsolutePath();
                if (!opts.excludedBreakingChanges().contains(bc)
                	&& !matchesPattern(path, opts.excludedCompilerMessage()))
                    filteredBrokenUses.add(bu);
            }
        }
        return filteredBrokenUses;
	}

    @Override
    public Set<CompilerMessage> filterCompilerMessages(Set<CompilerMessage> messages) {
        Set<CompilerMessage> filteredMessages = new HashSet<CompilerMessage>();

        if (opts != null && !opts.excludedCompilerMessage().isEmpty()) {
            for (CompilerMessage message : messages)
                if (!matchesPattern(message.path(), opts.excludedCompilerMessage()))
                    filteredMessages.add(message);
        }
        return filteredMessages;
    }

    private boolean matchesPattern(String path, Set<String> regexes) {
    	for (String regex : regexes) {
        	Pattern pattern = Pattern.compile(regex);
        	Matcher matcher = pattern.matcher(path);
            if (matcher.matches())
                return true;
        }
    	return false;
    }
}
