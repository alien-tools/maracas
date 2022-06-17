package com.github.maracas.experiments.model;

import com.github.maracas.experiments.model.ExperimentError.ExperimentErrorCode;

public record ExperimentError(
	String cursor,
	String owner,
	String name,
	ExperimentErrorCode code,
	String comments) {

	public enum ExperimentErrorCode {
		IRRELEVANT_REPO, INACTIVE_REPO, POM_DOWNLOAD_ISSUE, INCOMPLETE_POM,
		NO_DEPENDANTS_PAGE, EMPTY_PKG_PAGE, NO_PKG_DEPENDANTS, CLIENTS_NUM_FORMAT_ERROR,
		NO_PKG_IN_REPO
	}

	public void printLog() {
		System.out.println("[%s] %s:%s: %s".formatted(code, owner, name, comments));
	}
}
