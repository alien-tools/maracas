package com.github.maracas.rest.controllers;

import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import com.github.maracas.forges.github.GitHubClient;
import com.github.maracas.forges.github.GitHubClientsFetcher;
import com.github.maracas.forges.github.GitHubClientsScraper;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.data.ClientsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/github")
public class ClientsController {
	private final Forge forge;
	private final GitHubClientsFetcher fetcher;

	private static final Logger logger = LogManager.getLogger(ClientsController.class);

	public ClientsController(GitHub github) {
		this.fetcher = new GitHubClientsScraper(Duration.ofDays(7));
		this.forge = new GitHubForge(Objects.requireNonNull(github), this.fetcher);
	}

	@GetMapping("/clients/{owner}/{name}")
	public ResponseEntity<ClientsResponse> fetchClients(
		@PathVariable String owner,
		@PathVariable String name
	) {
		Repository repository = forge.fetchRepository(owner, name);
		List<RepositoryModule> modules = fetcher.fetchModules(repository);
		List<GitHubClient> clients = fetcher.fetchClients(repository, GitHubClientsFetcher.ClientFilter.ALL, Integer.MAX_VALUE);

		return ResponseEntity.ok(new ClientsResponse(owner, name, modules, clients));
	}

	@GetMapping("/modules/{owner}/{name}")
	public ResponseEntity<ClientsResponse> fetchModules(
		@PathVariable String owner,
		@PathVariable String name
	) {
		Repository repository = forge.fetchRepository(owner, name);
		List<RepositoryModule> modules = fetcher.fetchModules(repository);

		return ResponseEntity.ok(new ClientsResponse(owner, name, modules, Collections.emptyList()));
	}

	@ExceptionHandler({ForgeException.class})
	public ResponseEntity<ClientsResponse> handleForgeExceptions(Exception e) {
		logger.error(e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ClientsResponse(e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ClientsResponse> handleExceptions(Exception e) {
		logger.error("Uncaught exception ", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ClientsResponse(e.getMessage()));
	}
}
