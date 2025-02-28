package com.github.maracas.experiments;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.experiments.utils.Constants;

/**
 * Main class of the experiment.
 */
public class ExperimentMain {
	/**
	 * Class logger
	 */
	private static final Logger logger = LogManager.getLogger(ExperimentMain.class);

	/**
	 * Runs the experiment to gather the dataset metadata.
	 */
	public void run() {
		try {
			GitHubRepositoriesFetcher fetcher = new GitHubRepositoriesFetcher(Constants.STARTING_DATE.toLocalDate());
			String cursor = fetcher.getLastCursor();
			LocalDateTime date = fetcher.getLastDate();
			LocalDateTime datetime = (date == null) ? Constants.STARTING_DATE : date;

			logger.info("Fetching repositories...");
			fetcher.fetchRepositories(cursor, datetime);

		} catch(IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Main method of the project.
	 * @param args
	 */
	public static void main(String[] args) {
		new ExperimentMain().run();
	}
}
