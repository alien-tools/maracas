package com.github.maracas.experiments.utils;

import java.time.LocalDateTime;

public final class Constants {
	/**
	 * This class should not be instantiated.
	 */
	private Constants() {}

	public static final String CLIENTS_CSV_PATH = "clients.csv";
	public static final String ERRORS_CSV_PATH = "errors.csv";
	public static final String PRS_CSV_PATH = "prs.csv";

	public static final LocalDateTime STARTING_DATE = LocalDateTime.of(2022, 7, 1, 0, 0);
	public static final LocalDateTime REPO_LAST_PUSHED_DATE = LocalDateTime.of(2022, 4, 1, 0, 0);
	public static final LocalDateTime CLIENT_LAST_PUSHED_DATE = LocalDateTime.of(2022, 4, 1, 0, 0);
	public static final LocalDateTime PR_LAST_CREATED = LocalDateTime.of(2022, 4, 1, 0, 0);

	public static final int REPO_MIN_STARS = 100;
	public static final int CLIENT_MIN_STARS = 100;

	public static final int PR_LAST_MERGED_IN_DAYS = 180;
	public static final int LAST_PUSH_IN_DAYS = 180;
}
