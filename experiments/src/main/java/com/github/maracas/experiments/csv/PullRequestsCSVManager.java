package com.github.maracas.experiments.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import com.github.maracas.experiments.model.PullRequest;
import com.github.maracas.experiments.model.PullRequest.State;
import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;

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
		return new String[] {"cursor", "timestamp", "owner", "name", "sshUrl", "url",
			"baseRepo", "baseRef", "baseRefPrefix", "headRepo", "headRef", "headRefPrefix",
			"title", "number", "state", "draft", "files", "createdAt", "publishedAt",
			"mergedAt", "closedAt", "groupId", "artifactId", "version", "filePath"};
	}

	@Override
	protected Map<String, String> buildColumnsFormat() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("cursor", "%s");
		map.put("timestamp", "%t");
		map.put("owner", "%s");
		map.put("name", "%s");
		map.put("sshUrl", "%s");
		map.put("url", "%s");
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
		map.put("groupId", "%s");
		map.put("artifactId", "%s");
		map.put("version", "%s");
		map.put("filePath", "%s");
		return map;
	}

	@Override
	public void writeRecord(Object obj) {
		if (obj instanceof PullRequest pr) {
			File csv = new File(path);
			try (Writer writer = new FileWriter(csv, true);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat);) {
				Repository repo = pr.getRepository();
				String cursor = repo.getCursor();
				LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("Europe/Amsterdam"));
				String owner = repo.getOwner();
				String name = repo.getName();
				URI sshUrl = repo.getSshUrl();
				URL url = repo.getUrl();
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

				for (RepositoryPackage pkg: pr.getModifiedPackages()) {
					String groupId = pkg.getGroup();
					String artifactId = pkg.getArtifact();
					String version = pkg.getVersion();
					List<String> pkgFiles = pr.getFilesPerPackage(pkg.getName());

					for (String filePath : pkgFiles) {
						printer.printRecord(cursor, timestamp, owner, name, sshUrl,
							url, baseRepo, baseRef, baseRefPrefix, headRepo, headRef,
							headRefPrefix, title, number, state, draft, files,
							createdAt, publishedAt, mergedAt, closedAt, groupId,
							artifactId, version, filePath);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
