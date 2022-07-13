package com.github.maracas.experiments.csv;

import com.github.maracas.experiments.csv.ErrorRecord.ExperimentErrorCode;

public record ErrorRecord(
	String cursor,
	String owner,
	String name,
	ExperimentErrorCode code,
	String comments) {

	public enum ExperimentErrorCode {
		GITHUB_API_ERROR, IRRELEVANT_REPO, INACTIVE_REPO, POM_DOWNLOAD_ISSUE, INCOMPLETE_POM,
		NO_DEPENDANTS_PAGE, EMPTY_PKG_PAGE, NO_PKG_DEPENDANTS, CLIENTS_NUM_FORMAT_ERROR,
		NO_PKG_IN_REPO, EXCEEDED_RATE_LIMIT
	}

	public void printLog() {
		System.out.println("[%s] %s:%s: %s".formatted(code, owner, name, comments));
	}
}
