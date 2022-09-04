package com.github.maracas.experiments;

import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.CommitBuilder;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.clone.Cloner;
import com.github.maracas.forges.github.GitHubForge;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AnalyzePRs {
	private GitHub gh = buildGitHub();
	private Forge forge = new GitHubForge(gh);
	private List<Case> cases = new ArrayList<>();
	private static final Path PR_CSV = Paths.get("./experiments/data/combined.csv");
	private static final Path RESULTS_CSV = Paths.get("./experiments/data/results.csv");
	private static final Path LIBRARY_CLONES = Paths.get("./experiments/clones/libraries");
	private static final Path CLIENT_CLONES = Paths.get("./experiments/clones/clients");
	private static final Path GH_CACHE = Paths.get("./experiments/cache");
	private static final Logger logger = LogManager.getLogger(AnalyzePRs.class);

	// Store the ones we've computed already
	private Map<String, PullRequest> cachePRs = new HashMap<>();
	private Map<String, Delta> cacheDeltas = new HashMap<>();
	private Map<Path, LibraryJar> cacheLibraries = new HashMap<>();
	private Map<Path, SourcesDirectory> cacheClients = new HashMap<>();

	public AnalyzePRs() throws IOException {
		this.gh = GitHubBuilder.fromEnvironment().build();
		this.forge = new GitHubForge(gh);

		try (Reader reader = new FileReader(PR_CSV.toFile())) {
			this.cases = new CsvToBeanBuilder<Case>(reader).withType(Case.class).build().parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try (var writer = new FileWriter(RESULTS_CSV.toFile())) {
			var beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
			var i = 0;

			for (var c : cases) {
				logger.info("Cached: {} PRs; {} deltas; {} libs; {} clients",
					cachePRs.size(), cacheDeltas.size(), cacheLibraries.size(), cacheClients.size());
				logger.info("[{}/{}] PR#{} of {}/{} on {}/{}",
					++i, cases.size(), c.prNumber, c.owner, c.name, c.cowner, c.cname);

				try {
					var pr = cachePRs.get(c.owner + c.name + c.prNumber);

					if (pr == null) {
						pr = forge.fetchPullRequest(c.owner, c.name, c.prNumber);
						cachePRs.put(c.owner + c.name + c.prNumber, pr);
					}

					var prPath = LIBRARY_CLONES.resolve(c.owner).resolve(c.name).resolve(String.valueOf(c.prNumber));

					try {
						// Clone and build PR's mergeBase
						var mergeBasePath = prPath.resolve("mergeBase");
						var mergeBaseConfig = new BuildConfig(mergeBasePath, Paths.get(c.pkgPath));
						var mergeBaseBuilder = new CommitBuilder(pr.mergeBase(), mergeBasePath, mergeBaseConfig);
						var mergeBaseJar = mergeBaseBuilder.cloneAndBuildCommit();
						logger.info("[{}#{}] PR's mergeBase JAR is at {}", c.name, c.prNumber, mergeBaseJar.get());

						try {
							// Clone and build PR's HEAD
							var headPath = prPath.resolve("head");
							var headConfig = new BuildConfig(headPath, Paths.get(c.pkgPath));
							var headBuilder = new CommitBuilder(pr.head(), headPath, headConfig);
							var headJar = headBuilder.cloneAndBuildCommit();
							logger.info("[{}#{}] PR's head JAR is at {}", c.name, c.prNumber, headJar.get());

							try {
								var delta = cacheDeltas.get(c.owner + c.name + c.prNumber);

								if (delta == null) {
									// Compute delta between mergeBase and HEAD
									cacheLibraries.putIfAbsent(mergeBaseJar.get(), new LibraryJar(mergeBaseJar.get()));
									cacheLibraries.putIfAbsent(headJar.get(), new LibraryJar(headJar.get()));
									delta = Maracas.computeDelta(cacheLibraries.get(mergeBaseJar.get()), cacheLibraries.get(headJar.get()));
									Files.write(prPath.resolve("delta.json"), delta.toJson().getBytes());
									cacheDeltas.put(c.owner + c.name + c.prNumber, delta);
									logger.info("[{}#{}] Found {} BCs", c.name, c.prNumber, delta.getBreakingChanges().size());
								} else
									logger.info("[{}#{}] Already computed: {} BCs", c.name, c.prNumber, delta.getBreakingChanges().size());

								c.bcs = delta.getBreakingChanges().size();
								if (!delta.getBreakingChanges().isEmpty()) {
									try {
										// Clone client
										var client = forge.fetchRepository(c.cowner, c.cname);
										var clientPath = CLIENT_CLONES.resolve(c.cowner).resolve(c.cname);
										Cloner.of(client).clone(client, clientPath);

										// Compute delta impact
										cacheClients.putIfAbsent(clientPath, new SourcesDirectory(clientPath));
										var impact = Maracas.computeDeltaImpact(cacheClients.get(clientPath), delta);
										var brokenUses = impact.getBrokenUses();
										c.brokenUses = brokenUses.size();
										Files.write(prPath.resolve(String.format("%s-%s-impact.json", c.cowner, c.cname)), impact.toJson().getBytes());
										logger.info("[{}#{}] Found {} broken uses in {}/{}",
											c.name, c.prNumber, brokenUses.size(), c.cowner, c.cname);

										if (impact.getThrowable() != null)
											c.error = impact.getThrowable().getMessage();
									} catch (Exception e) {
										e.printStackTrace();
										c.error = e.getMessage();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								c.error = e.getMessage();
							}
						} catch (Exception e) {
							e.printStackTrace();
							c.error = e.getMessage();
						}
					} catch (Exception e) {
						e.printStackTrace();
						c.error = e.getMessage();
					}
				} catch (Exception e) {
					e.printStackTrace();
					c.error = e.getMessage();
				}

				beanToCsv.write(c);
				writer.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds a GitHub client with built-in caching to temper rate-limited API calls
	 */
	private GitHub buildGitHub() throws IOException {
		var builder = new OkHttpClient().newBuilder();
		builder.addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				logger.info("Sending request {}", chain.request().url());
				return chain.proceed(chain.request());
			}
		});

		var cacheDir = GH_CACHE.toFile();
		cacheDir.mkdirs();
		var cache = new Cache(cacheDir, 100 * 1024L * 1024L);
		builder.cache(cache);

		return
			GitHubBuilder.fromEnvironment()
				.withConnector(new OkHttpGitHubConnector(builder.build()))
				.build();
	}

	public static void main(String[] args) throws IOException {
		new AnalyzePRs().run();
	}

	public static class Case {
		String owner;
		String name;
		String pkgPath;
		String cowner;
		String cname;
		int prNumber;
		int bcs;
		int brokenUses;
		String error;
	}
}
