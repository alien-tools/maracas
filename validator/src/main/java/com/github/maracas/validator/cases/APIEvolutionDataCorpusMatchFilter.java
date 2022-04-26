package com.github.maracas.validator.cases;

import java.util.Set;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.validator.build.CompilerMessage;
import com.github.maracas.validator.matchers.MatcherFilter;
import com.github.maracas.validator.matchers.MatcherOptions;

public class APIEvolutionDataCorpusMatchFilter extends MatcherFilter {

	public APIEvolutionDataCorpusMatchFilter(MatcherOptions opts) {
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
