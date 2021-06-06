package org.swat.maracas.rest.data;

public class ExecutionStatistics {
	private final long cloneAndBuildTime;
	private final long deltaTime;

	public ExecutionStatistics(long cloneAndBuildTime, long deltaTime) {
		this.cloneAndBuildTime = cloneAndBuildTime;
		this.deltaTime = deltaTime;
	}

	public long getCloneAndBuildTime() {
		return cloneAndBuildTime;
	}

	public long getDeltaTime() {
		return deltaTime;
	}
}
