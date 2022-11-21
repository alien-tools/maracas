package com.github.maracas.experiments.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Repository;

/**
 * Class in charge of managing the pull requests CSV file.
 */
public class PullRequestsCSVManager extends CSVManager {
	/**
	 * Creates a {@link PullRequestsCSVManager} instance.
	 *
	 * @param path  Path to the pull requests CSV file
	 * @throws IOException
	 */
	public PullRequestsCSVManager(String path) throws IOException {
		super(path);
	}

	@Override
	protected String[] buildColumns() {
		return new String[] {"cursor", "owner", "name", "repoUrl", "clients",
			"prUrl", "title", "number", "state", "draft", "files", "javaFiles",
			"baseRepo", "baseRef", "baseRefPrefix", "headRepo", "headRef", "headRefPrefix",
			"createdAt", "publishedAt", "mergedAt", "closedAt"};
	}

	@Override
	protected Map<String, String> buildColumnsFormat() {
		Map<String, String> map = new HashMap<>();
		map.put("cursor", "%s");
		map.put("owner", "%s");
		map.put("name", "%s");
		map.put("repoUrl", "%s");
		map.put("prUrl", "%s");
		map.put("clients", "%d");
		map.put("baseRepo", "%s");
		map.put("baseRef", "%s");
		map.put("baseRefPrefix", "%s");
		map.put("headRepo", "%s");
		map.put("headRef", "%s");
		map.put("headRefPrefix", "%s");
		map.put("title", "%s");
		map.put("number", "%d");
		map.put("state", "%s");
		map.put("draft", "%b");
		map.put("createdAt", "%t");
		map.put("publishedAt", "%t");
		map.put("mergedAt", "%t");
		map.put("closedAt", "%t");
		map.put("files", "%d");
		map.put("javaFiles", "%d");
		return map;
	}

	@Override
	public void writeRecord(Object obj) {
		if (obj instanceof PullRequest pr) {
			File csv = new File(path);
			try (
				Writer writer = new FileWriter(csv, true);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat)
			) {
				Repository repo = pr.getRepository();
				String cursor = repo.getCursor();
				String owner = repo.getOwner();
				String name = repo.getName();
				URL repoUrl = repo.getUrl();
				String baseRepo = pr.getBaseRepository();
				String baseRef = pr.getBaseRef();
				String baseRefPrefix = pr.getBaseRefPrefix();
				String headRepo = pr.getHeadRepository();
				String headRef = pr.getHeadRef();
				String headRefPrefix = pr.getHeadRefPrefix();
				String title = pr.getTitle();
				int number = pr.getNumber();
				State state = pr.getState();
				boolean draft = pr.isDraft();
				LocalDate createdAt = pr.getCreatedAt();
				LocalDate publishedAt = pr.getPublishedAt();
				LocalDate mergedAt = pr.getMergedAt();
				LocalDate closedAt = pr.getClosedAt();
				int files = pr.getFiles().size();
				long javaFiles = pr.getFiles().stream().filter(f -> f.endsWith(".java")).count();
				String prUrl = "https://github.com/%s/%s/pull/%s".formatted(owner, name, number);
				int clients = repo.getClients();

				printer.printRecord(cursor, owner, name, repoUrl, clients,
					prUrl, title, number, state, draft, files, javaFiles,
					baseRepo, baseRef, baseRefPrefix, headRepo, headRef, headRefPrefix,
					createdAt, publishedAt, mergedAt, closedAt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
