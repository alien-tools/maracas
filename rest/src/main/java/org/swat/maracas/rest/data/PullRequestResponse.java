package org.swat.maracas.rest.data;

import java.util.List;

public class PullRequestResponse {
	private final PullRequestMeta meta;
	private final ExecutionStatistics statistics;
	private final List<BreakingChangeInstance> breakingChanges;

	public PullRequestResponse(PullRequestMeta meta, ExecutionStatistics statistics, List<BreakingChangeInstance> breakingChanges) {
		this.meta = meta;
		this.statistics = statistics;
		this.breakingChanges = breakingChanges;
	}

	public PullRequestResponse(String head, String base, int mavenClients, ExecutionStatistics statistics, List<BreakingChangeInstance> breakingChanges) {
		this(new PullRequestMeta(head, base, mavenClients), statistics, breakingChanges);
	}

	public PullRequestMeta getMeta() {
		return meta;
	}

	public ExecutionStatistics getStatistics() {
		return statistics;
	}

	public List<BreakingChangeInstance> getBreakingChanges() {
		return breakingChanges;
	}

	static class PullRequestMeta {
		private final String head;
		private final String base;
		private final int mavenClients;

		public PullRequestMeta(String head, String base, int mavenClients) {
			this.head = head;
			this.base = base;
			this.mavenClients = mavenClients;
		}

		public String getHead() {
			return head;
		}

		public String getBase() {
			return base;
		}

		public int getMavenClients() {
			return mavenClients;
		}
	}
}
