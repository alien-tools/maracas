package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swat.maracas.rest.data.Config;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.PullRequestResponse;
import org.swat.maracas.rest.delta.PullRequestDiff;
import org.swat.maracas.rest.impact.GithubRepository;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneException;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import nl.cwi.swat.aethereal.AetherCollector;
import nl.cwi.swat.aethereal.AetherDownloader;
import nl.cwi.swat.aethereal.MavenCollector;

@RestController
@RequestMapping("/github")
public class GithubController {
	private GitHub github;
	private Map<String, CompletableFuture<Delta>> jobs = new ConcurrentHashMap<>();

	private static final Logger logger = LogManager.getLogger(GithubController.class);

	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.delta-path:./deltas}")
	private String deltaPath;
	@Value("${maracas.breakbot-file:.breakbot.yml}")
	private String breakbotFile;

	@Autowired
    ResourceLoader resourceLoader;

	@PostConstruct
	public void initialize() {
		Paths.get(clonePath).toFile().mkdirs();
		Paths.get(deltaPath).toFile().mkdirs();

		Resource githubRes = resourceLoader.getResource("classpath:.github");
		try (InputStream in = githubRes.getInputStream()) {
			Properties props = new Properties();
			props.load(in);
			github = GitHubBuilder.fromProperties(props).build();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@PostMapping("/pr/{user}/{repository}/{prId}")
	String analyzePullRequest(@PathVariable String user, @PathVariable String repository, @PathVariable Integer prId, HttpServletResponse response) {
		try {
			// Read PR meta
			GHRepository repo = github.getRepository(user + "/" + repository);
			GHPullRequest pr = repo.getPullRequest(prId);
			PullRequestDiff prDiff = new PullRequestDiff(pr, clonePath);
			File deltaFile = Paths.get(deltaPath).resolve(user).resolve(repository).resolve(prId + ".json").toFile();
			String uid = prUid(user, repository, prId);

			// Read BreakBot config
			GHContent configFile = repo.getFileContent(breakbotFile);
			InputStream configIn = configFile.read();
			Config config = Config.fromYaml(configIn);

			String getLocation = String.format("/github/pr/%s/%s/%s", user, repository, prId);
			response.setStatus(HttpStatus.SC_ACCEPTED);
			response.setHeader("Location", getLocation);

			// If we have not yet computed this delta, compute it and store the future
			if (!jobs.containsKey(uid) && !deltaFile.exists()) {
				CompletableFuture<Delta> future =
					prDiff.diffAsync()
					.thenApply(delta -> {
						for (String c : config.getGithubClients())
							try {
								GHRepository clientRepo = github.getRepository(c);
								GithubRepository client = new GithubRepository(clientRepo, clonePath);
								delta = client.computeImpact(delta);
							} catch (IOException e) {
								e.printStackTrace();
							}
						return delta;
					}).handle((delta, e) -> {
						if (delta != null) {
							logger.info("Serializing {}", deltaFile);
							deltaFile.getParentFile().mkdirs();
							delta.toJson(deltaFile);
							return delta;
						} else if (e != null) {
							logger.error(e);
						}

						jobs.remove(uid);
						return null;
					});

				jobs.put(uid, future);
				return "processing";
			} else
				return "already processed";
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return e.getMessage();
		}
	}

	@GetMapping("/pr/{user}/{repository}/{prId}")
	PullRequestResponse getPullRequest(@PathVariable String user, @PathVariable String repository, @PathVariable Integer prId, HttpServletResponse response) {
			// Either we have it already
			File deltaFile = Paths.get(deltaPath).resolve(user).resolve(repository).resolve(prId + ".json").toFile();
			if (deltaFile.exists() && deltaFile.length() > 0) {
				Delta delta = Delta.fromJson(deltaFile);
				return new PullRequestResponse("ok", delta);
			}

			// Or we're currently computing it
			if (jobs.containsKey(prUid(user, repository, prId))) {
				response.setStatus(HttpStatus.SC_ACCEPTED);
				return new PullRequestResponse("processing", null);
			}

			// Or it doesn't exist
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return new PullRequestResponse("This PR isn't being analyzed", null);
	}

	@GetMapping("/pr-sync/{user}/{repository}/{prId}")
	PullRequestResponse analyzePullRequestDebug(@PathVariable String user, @PathVariable String repository, @PathVariable Integer prId, HttpServletResponse response) {
		try {
			GHRepository repo = github.getRepository(user + "/" + repository);
			GHPullRequest pr = repo.getPullRequest(prId);
			PullRequestDiff prDiff = new PullRequestDiff(pr, clonePath);
			Delta delta = prDiff.diff();

			// Read BreakBot config
			GHContent configFile = repo.getFileContent(breakbotFile);
			InputStream configIn = configFile.read();
			Config config = Config.fromYaml(configIn);

			for (String c : config.getGithubClients())
				try {
					GHRepository clientRepo = github.getRepository(c);
					GithubRepository client = new GithubRepository(clientRepo, clonePath);
					delta = client.computeImpact(delta);
				} catch (IOException e) {
					e.printStackTrace();
				}

			return new PullRequestResponse("ok", delta);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return new PullRequestResponse(e.getMessage(), null);
		} catch (CloneException | BuildException | CompletionException e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			return new PullRequestResponse(e.getMessage(), null);
		}
	}

	public List<File> findAffectedVersions() throws IOException, XmlPullParserException {
		GHRepository repo = github.getRepository("tdegueul/commons-io");
		MavenCollector col = new AetherCollector(15, 15);
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
			String brokenDecl = "org.apache.commons.io.IOUtils.buffer(Ljava/io/)Reader;)Ljava/io/BufferedReader;";
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

	private String prUid(String repository, String user, int prId) {
		return repository + "-" + user + "-" + prId;
	}
}
