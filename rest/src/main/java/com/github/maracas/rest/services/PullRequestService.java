package com.github.maracas.rest.services;

import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.build.maven.MavenBuilder;
import com.github.maracas.forges.clone.Cloner;
import com.github.maracas.forges.clone.git.GitCloner;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class PullRequestService {
	@Autowired
	private MaracasService maracasService;
	@Autowired
	private BreakbotService breakbotService;
	@Autowired
	private GitHub github;
	@Value("${maracas.clone-path:./clones}")
	private String clonePath;
	@Value("${maracas.report-path:./reports}")
	private String reportPath;

	private Forge forge;

	private final Map<String, CompletableFuture<Void>> jobs = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(PullRequestService.class);

	@PostConstruct
	public void initialize() {
		Paths.get(clonePath).toFile().mkdirs();
		Paths.get(reportPath).toFile().mkdirs();

		forge = new GitHubForge(github);
	}

	public PullRequest fetchPullRequest(String owner, String repository, int number) {
		return forge.fetchPullRequest(owner, repository, number);
	}

	public String analyzePR(PullRequest pr, String callback, String installationId, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name()) :
				BreakbotConfig.fromYaml(breakbotYaml);
		String uid = prUid(pr);
		File reportFile = reportFile(pr);
		String reportLocation = "/github/pr/%s/%s/%s".formatted(pr.repository().owner(), pr.repository().name(), pr.number());

		// If we're already on it, no need to compute it twice
		if (isProcessing(pr))
			logger.info("{} is already being analyzed", uid);
		else {
			logger.info("Starting the analysis of {}", uid);

			CompletableFuture<Void> future =
				CompletableFuture
					.supplyAsync(() -> buildMaracasReport(pr, config))
					.handle((report, ex) -> {
						jobs.remove(uid);

						if (ex != null) {
							logger.error("Error analyzing " + uid, ex);
							return new PullRequestResponse(ex.getCause().getMessage());
						}

						logger.info("Done analyzing {}", uid);
						serializeReport(report, reportFile);
						return new PullRequestResponse("ok", report);
					})
					.thenAccept(response -> {
						if (callback != null)
							breakbotService.sendPullRequestResponse(response, callback, installationId);
					});

			jobs.put(uid, future);
		}

		return reportLocation;
	}

	public MaracasReport analyzePRSync(PullRequest pr, String breakbotYaml) {
		BreakbotConfig config =
			StringUtils.isEmpty(breakbotYaml) ?
				breakbotService.readBreakbotConfig(pr.repository().owner(), pr.repository().name()) :
				BreakbotConfig.fromYaml(breakbotYaml);
		return buildMaracasReport(pr, config);
	}

	private MaracasReport buildMaracasReport(PullRequest pr, BreakbotConfig config) {
		try {
			Path baseClone = clonePath(pr.base());

			// Step 1: clone and build both branches of the pull request, retrieve JARs
			CompletableFuture<Builder> baseFuture = CompletableFuture.supplyAsync(
				() -> cloneAndBuild(pr.base(), config.build()));
			CompletableFuture<Builder> headFuture = CompletableFuture.supplyAsync(
				() -> cloneAndBuild(pr.head(), config.build()));

			CompletableFuture.allOf(baseFuture, headFuture).join();
			Builder baseBuilder = baseFuture.get();
			Builder headBuilder = headFuture.get();
			Optional<Path> baseJar = baseBuilder.locateJar();
			Optional<Path> headJar = headBuilder.locateJar();

			if (baseJar.isEmpty())
				throw new BuildException("Couldn't build a JAR from " + pr.base());
			if (headJar.isEmpty())
				throw new BuildException("Couldn't build a JAR from " + pr.head());

			// Step 2: build the delta model
			Path sources =
				StringUtils.isEmpty(config.build().sources()) ?
					baseBuilder.locateSources() :
					baseClone.resolve(config.build().sources());

			Delta delta = maracasService.makeDelta(baseJar.get(), headJar.get(), sources, config);

			// Step 3: if we found some breaking changes, clone & analyze the clients
			List<ClientReport> brokenUses = new ArrayList<>();
			if (delta.getBreakingChanges().isEmpty())
				brokenUses.addAll(
					config.clients().stream()
						.map(c -> ClientReport.empty(c.repository()))
						.toList()
				);
			else
				config.clients().parallelStream().forEach(c -> {
					try {
						// Clone the client, taking branch/sha into account
						String[] fields = c.repository().split("/");
						String owner = fields[0];
						String repository = fields[1];
						Repository clientRepo =
							StringUtils.isEmpty(c.branch()) ?
								forge.fetchRepository(owner, repository) :
								forge.fetchRepository(owner, repository, c.branch());
						String clientBranchSha = github.getRepository(owner + "/" + repository).getBranch(clientRepo.branch()).getSHA1();
						String clientSha = StringUtils.isEmpty(c.sha()) ? clientBranchSha : c.sha();
						Commit clientCommit =  new Commit(clientRepo, clientSha);

						Path clientClone = clone(clientCommit);
						Path clientSources = locateSources(clientClone, c.sources());

						brokenUses.add(
							ClientReport.success(c.repository(),
								maracasService.makeBrokenUses(delta, clientSources).stream()
									.map(d -> com.github.maracas.rest.data.BrokenUse.fromMaracasBrokenUse(d, clientRepo,
										clientRepo.branch(), clientClone.toAbsolutePath()))
									.toList())
						);

						logger.info("Done computing broken uses on {}", c.repository());
					} catch (Exception e) {
						logger.error(e);
						brokenUses.add(ClientReport.error(c.repository(),
							new MaracasException("Couldn't analyze client " + c.repository(), e)));
					}
				});

			// Step 4: build the report
			return new MaracasReport(
				com.github.maracas.rest.data.Delta.fromMaracasDelta(
					delta,
					pr,
					baseClone
				),
				brokenUses
			);
		} catch (ExecutionException | InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public Builder cloneAndBuild(Commit c, BreakbotConfig.Build config) {
		MavenBuilder builder = new MavenBuilder(clone(c).resolve(config.pom()));
		Properties properties = new Properties();
		config.properties().forEach(p -> properties.put(p, "true"));
		builder.build(config.goals(), properties);
		return builder;
	}

	public Path clone(Commit c) {
		Cloner cloner = new GitCloner();
		return cloner.clone(c, clonePath(c));
	}

	public Path locateSources(Path base, String sources) {
		if (!StringUtils.isEmpty(sources) && base.resolve(sources).toFile().exists())
			return base.resolve(sources);
		else if (base.resolve("src/main/java").toFile().exists())
			return base.resolve("src/main/java");
		else if (base.resolve("src/").toFile().exists())
			return base.resolve("src");
		else
			return base;
	}

	public boolean isProcessing(PullRequest pr) {
		return jobs.containsKey(prUid(pr));
	}

	private void serializeReport(MaracasReport report, File reportFile) {
		try {
			logger.info("Serializing {}", reportFile);
			reportFile.getParentFile().mkdirs();
			report.writeJson(reportFile);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public MaracasReport getReport(PullRequest pr) {
		try {
			File reportFile = reportFile(pr);
			if (reportFile.exists() && reportFile.length() > 0) {
				return MaracasReport.fromJson(reportFile);
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return null;
	}

	private String prUid(PullRequest pr) {
		return "%s-%s-%s-%s".formatted(
			pr.repository().owner(),
			pr.repository().name(),
			pr.number(),
			pr.head().sha()
		);
	}

	private File reportFile(PullRequest pr) {
		return Paths.get(reportPath)
			.resolve(pr.repository().owner())
			.resolve(pr.repository().name())
			.resolve("%d-%s.json".formatted(pr.number(), pr.head().sha()))
			.toFile();
	}

	private Path clonePath(Commit c) {
		return Paths.get(clonePath)
			.resolve(c.repository().owner())
			.resolve(c.repository().name())
			.resolve(c.sha())
			.toAbsolutePath();
	}
}
