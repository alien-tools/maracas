package com.github.maracas.rest.controllers;

import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubClientsFetcher;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.data.ClientsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/github")
public class ClientsController {
	@Autowired
	private GitHub github;
	private Forge forge;

	private static final Logger logger = LogManager.getLogger(ClientsController.class);

	@PostConstruct
	public void initialize() {
		forge = new GitHubForge(github);
	}

	@GetMapping("/clients/{owner}/{name}")
	public ResponseEntity<ClientsResponse> fetchClients(
		@PathVariable String owner,
		@PathVariable String name
	) {
		Repository repository = forge.fetchRepository(owner, name);
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Package> packages = fetcher.fetchPackages();
		List<GitHubClientsFetcher.Client> clients = fetcher.fetchClients();

		return ResponseEntity.ok(new ClientsResponse(owner, name, packages, clients));
	}

	@GetMapping("/packages/{owner}/{name}")
	public ResponseEntity<ClientsResponse> fetchPackages(
		@PathVariable String owner,
		@PathVariable String name
	) {
		Repository repository = forge.fetchRepository(owner, name);
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(repository);
		List<GitHubClientsFetcher.Package> packages = fetcher.fetchPackages();

		return ResponseEntity.ok(new ClientsResponse(owner, name, packages, Collections.emptyList()));
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
