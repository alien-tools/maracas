package com.github.maracas.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.github.maracas.*;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubForge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.Delta;
import com.github.maracas.rest.data.BrokenUse;
import com.github.maracas.rest.data.MaracasReport;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaracasReportTests {
	@Autowired
	private GitHub github;
	private MaracasReport report;

	@BeforeEach
	void setUp() {
		LibraryJar v1 = new LibraryJar(
			Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			new SourcesDirectory(Paths.get("../test-data/comp-changes/old/")));
		LibraryJar v2 = new LibraryJar(Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		SourcesDirectory c1 = new SourcesDirectory(Paths.get("../test-data/comp-changes/client/"));

		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(c1)
			.build();
		AnalysisResult result = Maracas.analyze(query);

		GitHubForge forge = new GitHubForge(github);
		PullRequest pr = forge.fetchPullRequest("alien-tools", "comp-changes", 6);
		Repository clientRepo = forge.fetchRepository("alien-tools", "comp-changes-client");

		report = new MaracasReport(
			Delta.fromMaracasDelta(result.delta(), pr, Paths.get("../test-data/comp-changes/old/")),
			List.of(ClientReport.success("alien-tools/comp-changes-client",
				result.allBrokenUses()
					.stream()
					.map(d -> BrokenUse.fromMaracasBrokenUse(d, clientRepo, "main", c1.getLocation()))
					.collect(Collectors.toList())
			))
		);
	}

	@Test
	void testSourceLocationsDelta() {
		// Hamcrest's hasProperty doesn't work with records yet
		//assertThat(
		//	report.delta().breakingChanges(),
		//	everyItem(allOf(
		//		hasProperty("path", not(emptyString())),
		//		hasProperty("startLine", not(equalTo(-1))),
		//		hasProperty("endLine", not(equalTo(-1)))
		//	))
		//);

		report.delta().breakingChanges().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void testSourceLocationsBrokenUses() {
		assertThat(report.clientReports().size(), is(1));
		report.clientReports().get(0).brokenUses().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void testGitHubLocationsDelta() {
		report.delta().breakingChanges().forEach(d -> {
			assertThat(d.fileUrl(), not(emptyOrNullString()));
			assertThat(d.diffUrl(), not(emptyOrNullString()));
		});
	}

	@Test
	void testGitHubLocationsBrokenUses() {
		assertThat(report.clientReports().size(), is(1));
		assertThat(report.clientReports().get(0).url(), not(emptyOrNullString()));
		report.clientReports().get(0).brokenUses().forEach(d -> assertThat(d.url(), not(emptyOrNullString())));
	}

	@Test
	void testGithubClientsArePresent() {
		assertThat(report.clientReports().size(), is(1));
		assertThat(report.clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(report.clientReports().get(0).brokenUses().size(), is(greaterThan(1)));
	}

}
