package org.swat.maracas.rest.data;

public class ExecutionStatistics {
	private final long cloneTime;
	private final long buildTime;
	private final long deltaTime;

	public ExecutionStatistics(long cloneTime, long buildTime, long deltaTime) {
		this.cloneTime = cloneTime;
		this.buildTime = buildTime;
		this.deltaTime = deltaTime;
	}

	public long getCloneTime() {
		return cloneTime;
	}

	public long getBuildTime() {
		return buildTime;
	}

	public long getDeltaTime() {
		return deltaTime;
	}
}
