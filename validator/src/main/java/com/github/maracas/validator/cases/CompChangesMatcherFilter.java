package com.github.maracas.validator.cases;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.validator.matchers.MatcherOptions;

import japicmp.model.JApiCompatibilityChange;

public class CompChangesMatcherFilter extends CasesDefaultMatcherFilter {

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
            	JApiCompatibilityChange bc = bu.change();
            	if (opts.excludedBreakingChanges().contains(bc))
                    continue;

                String path = bu.element().getPosition().getFile().getAbsolutePath();
                String pkg = bcToPkgName(bc);
                if (path.contains(pkg))
                    filteredBrokenUses.add(bu);
            }
        }
        return filteredBrokenUses;
    }
}
