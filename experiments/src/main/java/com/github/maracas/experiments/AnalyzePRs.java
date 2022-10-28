package com.github.maracas.experiments;

import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeAnalyzer;
import com.github.maracas.forges.github.GitHubForge;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AnalyzePRs {
	private final Forge forge;
	private List<Case> cases = new ArrayList<>();
	private List<Case> casesDone = new ArrayList<>();
	private static final Path PR_CSV = Path.of("./experiments/data/prs.csv");
	private static final Path RESULTS_CSV = Path.of("./experiments/data/results.csv");
	private static final Path WORKING_DIRECTORY = Path.of("./experiments/work");
	private static final Path GH_CACHE = Path.of("./experiments/cache");
	private static final Path REPORTS = Path.of("./experiments/reports");
	private static final Logger logger = LogManager.getLogger(AnalyzePRs.class);

	public AnalyzePRs() throws IOException {
		this.forge = new GitHubForge(buildGitHub());

		try (
			Reader prReader = new FileReader(PR_CSV.toFile());
			Reader resultsReader = new FileReader(RESULTS_CSV.toFile())
		) {
			this.casesDone = new CsvToBeanBuilder<Case>(resultsReader).withType(Case.class).build().parse();
			this.cases = new CsvToBeanBuilder<Case>(prReader).withType(Case.class).build().parse();
			this.cases.removeIf(c -> casesDone.stream().anyMatch(done ->
				c.owner.equals(done.owner) && c.name.equals(done.name) && c.number == done.number));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		var analyzer = new ForgeAnalyzer(forge, WORKING_DIRECTORY);
		analyzer.setBuildTimeoutSeconds(10 * 60);
		analyzer.setCloneTimeoutSeconds(10 * 60);
		analyzer.setExecutorService(Executors.newFixedThreadPool(4));

		try (var writer = new FileWriter(RESULTS_CSV.toFile(), true)) {
			var beanToCsv = new StatefulBeanToCsvBuilder<Case>(writer).build();
			var i = 0;

			for (var c : cases) {
				logger.info("[{}/{}] PR#{} of {}/{}",
					++i, cases.size(), c.number, c.owner, c.name);

				try {
					var pr = forge.fetchPullRequest(c.owner, c.name, c.number);
					var result = analyzer.analyzePullRequest(pr, 100, 5, MaracasOptions.newDefault());
					var j = 0;

					c.base = pr.baseBranch();
					c.head = pr.headBranch();
					c.changedFiles = pr.changedFiles().size();
					c.impactedPackages = result.size();

					for (var r : result) {
						if (r.delta() != null) {
							c.breakingChanges += r.delta().getBreakingChanges().size();
							c.brokenUses += r.allBrokenUses().size();
							c.clients += r.deltaImpacts().size();
							c.brokenClients += r.brokenClients().size();
							r.writeJson(REPORTS.resolve(String.format("%s-%s-%d-%d.json", c.owner, c.name, c.number, ++j)).toFile());

							logger.info("[{}/{}] PR#{} of {}/{}: found {} BCs and {} broken uses in {}/{} clients",
								i, cases.size(), c.number, c.owner, c.name, c.breakingChanges, c.brokenUses, c.brokenClients, c.clients);
						} else {
							c.error += r.error();
						}
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
		builder.addInterceptor(chain -> {
			logger.info("Sending request {}", chain.request().url());
			return chain.proceed(chain.request());
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
		int number;
		String base;
		String head;
		int changedFiles;
		int impactedPackages;
		int breakingChanges;
		int brokenUses;
		int clients;
		int brokenClients;
		String error;
	}
}
