package com.github.maracas.experiments;

import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.analysis.CommitAnalyzer;
import com.github.maracas.forges.analysis.PullRequestAnalyzer;
import com.github.maracas.forges.github.GitHubForge;
import com.google.common.base.Stopwatch;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import japicmp.model.JApiCompatibilityChange;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
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
import java.util.concurrent.TimeUnit;

public class AnalyzePRs {
	private final Forge forge;
	private List<Case> cases = new ArrayList<>();
	private static final Path PR_CSV = Path.of("./experiments/data/prs-new.csv");
	private static final Path PR_OLD_CSV = Path.of("./experiments/data/prs.csv");
	private static final Path RESULTS_CSV = Path.of("./experiments/data/results-new.csv");
	private static final Path WORKING_DIRECTORY = Path.of("./experiments/work");
	private static final Path GH_CACHE = Path.of("./experiments/cache");
	private static final Path REPORTS = Path.of("./experiments/reports");
	private static final Logger logger = LogManager.getLogger(AnalyzePRs.class);

	public AnalyzePRs() throws IOException {
		this.forge = new GitHubForge(buildGitHub());

		try (
			Reader prReader = new FileReader(PR_CSV.toFile());
			Reader oldPrReader = new FileReader(PR_OLD_CSV.toFile());
			Reader resultsReader = new FileReader(RESULTS_CSV.toFile());
		) {
			var casesDone = new CsvToBeanBuilder<Case>(resultsReader).withType(Case.class).build().parse();
			var oldCases = new CsvToBeanBuilder<Case>(oldPrReader).withType(Case.class).build().parse();
			this.cases = new CsvToBeanBuilder<Case>(prReader).withType(Case.class).build().parse();
			this.cases.removeIf(c ->
				casesDone.stream().anyMatch(done ->	c.owner.equals(done.owner) && c.name.equals(done.name) && c.number == done.number) ||
				oldCases.stream().anyMatch(done ->	c.owner.equals(done.owner) && c.name.equals(done.name) && c.number == done.number));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		var commitAnalyzer = new CommitAnalyzer(new Maracas(), Executors.newFixedThreadPool(4));
		var analyzer = new PullRequestAnalyzer(WORKING_DIRECTORY, forge, commitAnalyzer);

		try (var writer = new FileWriter(RESULTS_CSV.toFile(), true)) {
			var beanToCsv = new StatefulBeanToCsvBuilder<Case>(writer).build();
			var i = 0;

			for (var c : cases) {
				logger.info("[{}/{}] PR#{} of {}/{}",
					++i, cases.size(), c.number, c.owner, c.name);
				Stopwatch sw = Stopwatch.createStarted();

				try {
					var opts = MaracasOptions.newDefault();
					opts.setCloneTimeoutSeconds(5 * 60);
					opts.setBuildTimeoutSeconds(10 * 60);
					opts.setMaxClassLines(20_000);
					opts.setClientsPerPackage(100);
					opts.setMinStarsPerClient(5);
					var pr = forge.fetchPullRequest(c.owner, c.name, c.number);
					var result = analyzer.analyze(pr, opts);
					var j = 0;

					c.base = pr.baseBranch();
					c.head = pr.headBranch();
					c.changedFiles = pr.changedFiles().size();
					c.impactedPackages = result.packageResults().size();

					for (var r : result.packageResults().values()) {
						if (r.delta() != null) {
							c.deprecations = (int) r.delta().getBreakingChanges().stream().filter(bc -> bc.getChange().equals(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED)).count();
							c.breakingChanges += r.delta().getBreakingChanges().size() - c.deprecations;
							c.brokenUses += r.allBrokenUses().size();
							c.checkedClients += r.clientResults().size();
							c.brokenClients += r.brokenClients().size();
							//r.writeJson(REPORTS.resolve(String.format("%s-%s-%d-%d.json", c.owner, c.name, c.number, ++j)).toFile());

							logger.info("[{}/{}] PR#{} of {}/{}: found {} BCs and {} broken uses in {}/{} clients",
								i, cases.size(), c.number, c.owner, c.name, c.breakingChanges, c.brokenUses, c.brokenClients, c.checkedClients);
						} else if (r.error() != null) {
							c.errors += r.error();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					c.errors += e.getMessage();
				}

				c.seconds = sw.elapsed(TimeUnit.SECONDS);
				beanToCsv.write(c);
				writer.flush();
				FileUtils.cleanDirectory(WORKING_DIRECTORY.toFile());
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
		int deprecations;
		int brokenUses;
		int checkedClients;
		int brokenClients;
		String errors;
		long seconds;
	}
}
