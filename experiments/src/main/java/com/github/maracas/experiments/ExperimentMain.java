package com.github.maracas.experiments;

import java.io.IOException;

import com.github.maracas.experiments.utils.Constants;

public class ExperimentMain {

	// TODO: remove after refactoring
	public void run() {
		try {
			GitHubRepositoriesFetcher fetcher = new GitHubRepositoriesFetcher();
			ClientsCSVManager clientsCsv = new ClientsCSVManager(Constants.CLIENTS_CSV_PATH);

			System.out.println("Fetching repositories...");
			String cursor = clientsCsv.getCursor();
			fetcher.fetchRepositories(cursor, GitHubRepositoriesFetcher.REPO_MIN_STARS);
		} catch(IOException e) {

		}
//		writeErrors();
//
//		System.out.println("Found " + repositories.size());
//		try (FileWriter csv = new FileWriter("output.csv")) {
//			csv.write("cursor,owner,name,stars,groupId,artifactId,currentVersion,relativePath,clients,"
//				+ "relevantClients,cowner,cname,cstars\n");
//			csv.flush();
//
//			for (Repository repo : repositories) {
//				Collection<RepositoryPackage> packages = repo.getRepoPackages().values();
//
//				for (RepositoryPackage pkg : packages) {
//					List<Repository> clients = pkg.getRelevantClients();
//
//					for (Repository client : clients) {
//						csv.write("%s,%s,%s,%d,%s,%s,%s,%s,%d,%d,%s,%s,%d\n"
//							.formatted(repo.getCursor(), repo.getOwner(), repo.getName(), repo.getStars(),
//								pkg.getGroup(), pkg.getArtifact(),pkg.getCurrentVersion(),
//								pkg.getRelativePath(), pkg.getClients(), clients.size(),
//								client.getOwner(), client.getName(), client.getStars()));
//					}
//				}
//				csv.flush();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public static void main(String[] args) {
		new ExperimentMain().run();
	}
}
