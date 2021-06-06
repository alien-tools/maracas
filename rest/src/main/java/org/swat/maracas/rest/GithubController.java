package org.swat.maracas.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.swat.maracas.rest.data.BreakingChangeInstance;
import org.swat.maracas.rest.data.ExecutionStatistics;
import org.swat.maracas.rest.data.PullRequestResponse;
import org.swat.maracas.rest.tasks.CloneAndBuild;

import com.google.common.base.Stopwatch;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

@RestController
@RequestMapping("/github")
public class GithubController {
	private MaracasHelper maracas = MaracasHelper.getInstance();
	private GitHub github;
	private static final String CLONE_PATH = "./clones";
	private static final Logger logger = LogManager.getLogger(GithubController.class);

	@Autowired
    ResourceLoader resourceLoader;

	@PostConstruct
	public void initialize() {
		Resource githubRes = resourceLoader.getResource("classpath:.github");
		try (InputStream in = githubRes.getInputStream()) {
			Properties props = new Properties();
			props.load(in);
			this.github = GitHubBuilder.fromProperties(props).build();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	// Considering the computation time, this should probably be a POST job/GET result duo
	@GetMapping("/pr/{user}/{repository}/{pr}")
	PullRequestResponse analyzePullRequest(@PathVariable String user, @PathVariable String repository, @PathVariable Integer pr) {
		try {
			// Retrieve PR metadata from GH
			GHRepository repo = github.getRepository(String.format("%s/%s", user, repository));
			GHPullRequest pullRequest = repo.getPullRequest(pr);
			GHCommitPointer head = pullRequest.getHead();
			GHCommitPointer base = pullRequest.getBase();
			String headSha = head.getSha();
			String baseSha = base.getSha();
			String headUrl = head.getRepository().getHttpTransportUrl();
			String baseUrl = base.getRepository().getHttpTransportUrl();
			String headRef = head.getRef();
			String baseRef = base.getRef();
			Path basePath = Paths.get(CLONE_PATH).resolve(baseSha);
			Path headPath = Paths.get(CLONE_PATH).resolve(headSha);

			// Clone and build both repos
			Stopwatch stopwatch = Stopwatch.createStarted();
			CompletableFuture<Path> baseFuture = CompletableFuture.supplyAsync(new CloneAndBuild(baseUrl, baseRef, basePath));
			CompletableFuture<Path> headFuture = CompletableFuture.supplyAsync(new CloneAndBuild(headUrl, headRef, headPath));
			CompletableFuture.allOf(baseFuture, headFuture).join();
			long cloneAndBuildTime = stopwatch.elapsed().toMillis();
			Path j1 = baseFuture.get();
			Path j2 = headFuture.get();
			stopwatch.reset();

			// Build delta model
			stopwatch.start();
			IList delta = maracas.computeDelta(j1, j2, basePath);
			long deltaTime = stopwatch.elapsed().toMillis();
			stopwatch.reset();

			List<BreakingChangeInstance> bcs =
				delta.stream()
					.map(e -> BreakingChangeInstance.fromRascal((IConstructor) e))
					.collect(Collectors.toList());

			return new PullRequestResponse(
				headRef, baseRef, 0,
				new ExecutionStatistics(cloneAndBuildTime, deltaTime),
				bcs);
		} catch (GHFileNotFoundException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The repository or PR does not exist", e);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Execution error: " + e.getMessage(), e);
		} catch (CompletionException e) {
			logger.error(e);
			if (e.getCause() instanceof GitAPIException)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "git operation failed: " + e.getCause().getMessage(), e);
			else if (e.getCause() instanceof MavenInvocationException)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Maven build failed: " + e.getCause().getMessage(), e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error: " + e.getMessage(), e);
		}
	}
}
