package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.report.ClientImpact;

import java.util.Collection;
import java.util.List;

public interface CommitAnalyzer {
	Delta computeDelta(CommitBuilder v1, CommitBuilder v2, MaracasOptions options);

	List<ClientImpact> computeImpact(Delta delta, Collection<CommitBuilder> clients, MaracasOptions options);
}
