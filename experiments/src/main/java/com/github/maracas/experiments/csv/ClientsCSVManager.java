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
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import com.github.maracas.experiments.model.Repository;
import com.github.maracas.experiments.model.RepositoryPackage;

/**
 * Class in charge of managing the clients CSV file.
 */
public class ClientsCSVManager extends CSVManager {
	/**
	 * Creates a {@link ClientsCSVManager} instance.
	 *
	 * @param path  Path to the pull requests CSV file
	 * @throws IOException
	 */
	public ClientsCSVManager(String path) throws IOException {
		super(path);
	}

	@Override
	protected String[] buildColumns() {
		return new String[] {"cursor", "timestamp", "owner", "name", "sshUrl", "url", "stars",
			"createdAt", "pushedAt", "packages", "groupId", "artifactId", "version",
			"path", "clients", "relevantClients", "cowner", "cname", "csshUrl",
			"curl", "cstars"};
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
		map.put("stars", "%d");
		map.put("createdAt", "%t");
		map.put("pushedAt", "%t");
		map.put("packages", "%d");
		map.put("groupId", "%s");
		map.put("artifactId", "%s");
		map.put("version", "%s");
		map.put("path", "%s");
		map.put("clients", "%d");
		map.put("relevantClients", "%d");
		map.put("cowner", "%s");
		map.put("cname", "%s");
		map.put("csshUrl", "%s");
		map.put("curl", "%s");
		map.put("cstars", "%d");
		return map;
	}

	@Override
	public void writeRecord(Object obj) {
		if (obj instanceof RepositoryPackage pkg) {
			File csv = new File(path);
			try (Writer writer = new FileWriter(csv, true);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat);) {
				Repository repo = pkg.getRepository();
				String cursor = repo.getCursor();
				LocalDateTime timestamp = LocalDateTime.now(ZoneId.of("Europe/Amsterdam"));
				String owner = repo.getOwner();
				String name = repo.getName();
				URI sshUrl = repo.getSshUrl();
				URL url = repo.getUrl();
				int stars = repo.getStars();
				LocalDate createdAt = repo.getCreatedAt();
				LocalDate pushedAt = repo.getPushedAt();
				int packages = repo.getGitHubPackages();
				String groupId = pkg.getGroup();
				String artifactId = pkg.getArtifact();
				String version = pkg.getVersion();
				String path = pkg.getRelativePath();
				int clients = pkg.getClients();
				int relevantClients = pkg.getRelevantClients().size();

				for (Repository client : pkg.getRelevantClients()) {
					String cowner = client.getOwner();
					String cname = client.getName();
					URI csshUrl = client.getSshUrl();
					URL curl = client.getUrl();
					int cstars = client.getStars();

					printer.printRecord(cursor, timestamp, owner, name, sshUrl.toString(),
						url.toString(), stars, createdAt.toString(), pushedAt.toString(),
						packages, groupId, artifactId, version, path, clients,
						relevantClients, cowner, cname, csshUrl.toString(),
						curl.toString(), cstars);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the last printed value on the CSV file of the "pushedAt" column.
	 *
	 * @return last printed value on the CSV file of the "pushedAt" column. If
	 * there is no value returns {@code null}.
	 * @throws IOException
	 */
	public LocalDateTime getCurrentDate() throws IOException {
		String pushedAt = getLastValue("pushedAt");
		return (pushedAt == null) ? null
			: LocalDateTime.parse(pushedAt + "T00:00:00");
	}
}
