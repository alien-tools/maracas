package com.github.maracas.forges.analysis;

import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.report.ClientImpact;
import com.github.maracas.forges.report.CommitsReport;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ArbitraryCommitsAnalyzer {
	private final CommitAnalyzer commitAnalyzer;

	public ArbitraryCommitsAnalyzer(CommitAnalyzer commitAnalyzer) {
		this.commitAnalyzer = Objects.requireNonNull(commitAnalyzer);
	}

	public CommitsReport analyzeCommits(CommitBuilder v1, CommitBuilder v2, Collection<CommitBuilder> clients, MaracasOptions options) throws BuildException, CloneException {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		Objects.requireNonNull(clients);
		Objects.requireNonNull(options);

		try {
			Delta delta = commitAnalyzer.computeDelta(v1, v2, options);

			if (delta.getBreakingChanges().isEmpty())
				return CommitsReport.noImpact(v1.getCommit(), v2.getCommit(), delta, v1.getClonePath());
			List<ClientImpact> clientsImpact = commitAnalyzer.computeImpact(delta, clients, options);
			return CommitsReport.success(v1.getCommit(), v2.getCommit(), delta, clientsImpact, v1.getClonePath());
		} catch (Exception e) {
			return CommitsReport.error(v1.getCommit(), v2.getCommit(), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}
	}
}
