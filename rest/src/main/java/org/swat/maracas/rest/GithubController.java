package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
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

			// Clone both branches
			Stopwatch stopwatch = Stopwatch.createStarted();
			clone(baseUrl, baseRef, basePath);
			clone(headUrl, headRef, headPath);
			long cloneTime = stopwatch.elapsed().toMillis();
			stopwatch.reset();

			// Build both branches and retrieve the JARs
			stopwatch.start();
			Path j1 = build(basePath);
			Path j2 = build(headPath);
			long buildTime = stopwatch.elapsed().toMillis();
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
				new ExecutionStatistics(cloneTime, buildTime, deltaTime),
				bcs);
		} catch (GHFileNotFoundException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The repository or PR does not exist", e);
		} catch (GitAPIException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "git clone failed: " + e.getMessage(), e);
		} catch (MavenInvocationException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Maven build failed: " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error: " + e.getMessage(), e);
		}
	}

	private void clone(String url, String ref, Path dest) throws InvalidRemoteException, TransportException, GitAPIException {
		if (!dest.toFile().exists()) {
			logger.info("Cloning {} [{}]", url, ref);
			String fullRef = "refs/heads/" + ref; // FIXME
			Git.cloneRepository()
				.setURI(url)
				.setBranchesToClone(Collections.singletonList(fullRef))
				.setBranch(fullRef)
				.setDirectory(dest.toFile())
				.call();
		} else logger.info("{} exists. Skipping.", dest);
	}

	private Path build(Path local) throws IOException, MavenInvocationException {
		Path target = local.resolve("target");
		Path pom = local.resolve("pom.xml");
		if (!target.toFile().exists()) {
			if (!pom.toFile().exists())
				throw new MavenInvocationException("No root pom.xml file in " + local);

			logger.info("Building {}", pom);
			Properties properties = new Properties();
			properties.setProperty("skipTests", "true");
			
		    InvocationRequest request = new DefaultInvocationRequest();
		    request.setPomFile(pom.toFile());
		    request.setGoals(Collections.singletonList("package"));
		    request.setProperties(properties);
		    request.setBatchMode(true);
		     
		    Invoker invoker = new DefaultInvoker();
		    invoker.setMavenHome(new File("/usr"));
		    invoker.execute(request);
		} else logger.info("{} has already been built. Skipping.", local);

		// FIXME: Just returning whatever .jar we found in /target/
	    try (Stream<Path> walk = Files.walk(target, 1)) {
	        Optional<Path> res = walk.filter(f -> f.toString().endsWith(".jar")).findFirst();
	        if (res.isPresent())
	        	return res.get();
	        else
	        	throw new MavenInvocationException("'package' goal on " + pom + " did not produce a JAR");
	    }
	}
}
