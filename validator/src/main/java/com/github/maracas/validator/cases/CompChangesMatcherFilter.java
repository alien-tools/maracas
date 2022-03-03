package com.github.maracas.validator.cases;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;

import japicmp.model.JApiCompatibilityChange;

/**
 * CompChanges matcher filter to filter out broken uses and compiler messages
 * based on package naming conventions
 */
public class CompChangesMatcherFilter extends MatcherFilter {
    /**
     * Creates a CompChangesMatcherFilter instance.
     *
     * @param opts set of {@link MatcherOptions}
     */
    public CompChangesMatcherFilter(MatcherOptions opts) {
        super(opts);
    }

    /**
     * Transforms a JApiCmp breaking change enum into a package name given the
     * CompChanges project.
     *
     * @param change {@link JApiCompatibilityChange} instance
     * @return package name representation of the breaking change
     */
    private String bcToPkgName(JApiCompatibilityChange change) {
        String[] words = change.name().split("_");
        String name = "";
        for (String word : words)
            name += word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();

        return name;
    }

    @Override
    public Set<BrokenUse> filterBrokenUses(Set<BrokenUse> brokenUses) {
        Set<BrokenUse> filteredBrokenUses = new HashSet<BrokenUse>();

        if (opts != null && !opts.excludedBreakingChanges().isEmpty()) {
            for (BrokenUse bu : brokenUses) {
                String path = bu.element().getPosition().getFile().getAbsolutePath();
                String bc = bcToPkgName(bu.change());
                if (path.contains(bc))
                    filteredBrokenUses.add(bu);
            }
        }
        return filteredBrokenUses;
    }

    @Override
    public Set<CompilerMessage> filterCompilerMessages(Set<CompilerMessage> messages) {
        Set<CompilerMessage> filteredMessages = new HashSet<CompilerMessage>();

        if (opts != null && !opts.excludedBreakingChanges().isEmpty()) {
            for (CompilerMessage message : messages) {
                boolean include = true;
                for (String pattern : opts.excludedBreakingChanges().values()) {
                    if (message.path().contains(pattern)) {
                        include = false;
                        break;
                    }
                }

                if (include)
                    filteredMessages.add(message);
            }
        }
        return filteredMessages;
    }
}
