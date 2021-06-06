package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import nl.cwi.swat.aethereal.AetherCollector;
import nl.cwi.swat.aethereal.AetherDownloader;

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
	
	public List<File> findAffectedVersions() throws IOException, XmlPullParserException {
		GHRepository repo = github.getRepository("tdegueul/commons-io");
		AetherCollector col = new AetherCollector(15, 15);
		InputStream content = repo.getFileContent("pom.xml", "master").read();
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = reader.read(content);
		String gid = model.getGroupId();
		String aid = model.getArtifactId();
		String vid = model.getVersion();
		
		// https://regex101.com/r/vkijKf/1/
		String SEMVER_PATTERN = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
		Pattern semVer = Pattern.compile(SEMVER_PATTERN);
		Matcher matcher = semVer.matcher(vid);
		
		if (matcher.matches()) {
			int major = Integer.parseInt(matcher.group(1));
			String upperRange = vid;
			String lowerRange = major + ".0";
			List<Artifact> versions = col.collectAvailableVersions(String.format("%s:%s", gid, aid), lowerRange, upperRange);
			
			AetherDownloader downloader = new AetherDownloader(15);
			
			// @since 2.5
			String brokenDecl = "org.apache.commons.io.IOUtils.buffer(Ljava/io/Reader;)Ljava/io/BufferedReader;";
			List<String> brokenDecls = Collections.singletonList(brokenDecl);

			return
				versions
					.parallelStream()
					.map(v -> downloader.downloadArtifact(v).getFile())
					.filter(f -> f.exists())
					.filter(f -> {
						// Does it contain one of the affected declaration?
						try (ScanResult scanResult =
								new ClassGraph()
									.enableAllInfo()
									.overrideClasspath(f.toPath().toAbsolutePath().toString())
									.scan()
							) {
							// We can stop as soon as we find one match
							return scanResult.getAllClasses().stream().anyMatch(c -> {
								return
										brokenDecls.contains(c.getName()) ||
									   c.getDeclaredMethodAndConstructorInfo().stream().anyMatch(m -> {
										   return brokenDecls.contains(c.getName() + "." + m.getName() + m.getTypeDescriptorStr());
									   }) ||
									   c.getDeclaredFieldInfo().stream().anyMatch(fld -> {
										   return brokenDecls.contains(c.getName() + "." + fld.getName());
									   });
							});
						}
					})
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}
}
