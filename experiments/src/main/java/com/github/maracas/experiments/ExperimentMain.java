package com.github.maracas.experiments;

import java.io.IOException;

import com.github.maracas.experiments.csv.ClientsCSVManager;
import com.github.maracas.experiments.utils.Constants;

public class ExperimentMain {

	public void run() {
		try {
			GitHubRepositoriesFetcher fetcher = new GitHubRepositoriesFetcher(0);
			ClientsCSVManager clientsCsv = new ClientsCSVManager(Constants.CLIENTS_CSV_PATH);

			System.out.println("Fetching repositories...");
			String cursor = clientsCsv.getCursor();
			fetcher.fetchRepositories(cursor, GitHubRepositoriesFetcher.REPO_MIN_STARS);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ExperimentMain().run();
	}
}
