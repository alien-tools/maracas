package com.github.maracas.experiments;

import com.github.maracas.forges.CommitBuilder;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public class ClientsManager {
	private final Map<String, GHRepository> clients = new HashMap<>();

	public void addClient(String name, GHRepository repo) {
		clients.put(name, repo);
	}

	public Map<String, GHCommit> clientsAtDate(Date closedAt) {
		// TODO: check whether the client was already depending on the library at this point, in the right version
		return clients.values().stream()
			.filter(c -> existsAt(c, closedAt))
			.map(c -> {
				try {
					// This consumes sooooooo many API requests
					var commits = c.queryCommits().until(closedAt).list().toList();

					if (!commits.isEmpty())
						return commits.get(0);
					else
						return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(toMap(
				c -> c.getOwner().getFullName(),
				c -> c
			));
	}

	private boolean existsAt(GHRepository repo, Date date) {
		try  {
			return repo.getCreatedAt().before(date);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
