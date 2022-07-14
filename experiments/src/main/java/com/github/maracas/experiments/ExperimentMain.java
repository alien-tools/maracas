package com.github.maracas.experiments;

import java.io.IOException;
import java.time.LocalDateTime;

import com.github.maracas.experiments.csv.ClientsCSVManager;
import com.github.maracas.experiments.utils.Constants;

public class ExperimentMain {

	public void run() {
		try {
			GitHubRepositoriesFetcher fetcher = new GitHubRepositoriesFetcher();
			ClientsCSVManager clientsCsv = new ClientsCSVManager(Constants.CLIENTS_CSV_PATH);

			System.out.println("Fetching repositories...");
			String cursor = clientsCsv.getCursor();
			LocalDateTime datetime = LocalDateTime.now();
			fetcher.fetchRepositories(cursor, datetime);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ExperimentMain().run();
	}
}
