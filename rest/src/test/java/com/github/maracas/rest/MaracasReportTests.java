package com.github.maracas.rest;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.github.GitHubForge;
import com.github.maracas.rest.data.BrokenUseDto;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.DeltaDto;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PackageReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaracasReportTests {
	@Autowired
	private GitHub github;
	private MaracasReport report;

	@BeforeEach
	void setUp() {
		LibraryJar v1 = LibraryJar.withSources(
			Path.of("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			SourcesDirectory.of(Path.of("../test-data/comp-changes/old/")));
		LibraryJar v2 = LibraryJar.withoutSources(Path.of("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		SourcesDirectory c1 = SourcesDirectory.of(Path.of("../test-data/comp-changes/client/"));

		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(c1)
			.build();
		AnalysisResult result = new Maracas().analyze(query);

		GitHubForge forge = new GitHubForge(github);
		PullRequest pr = forge.fetchPullRequest("alien-tools", "comp-changes", 6);
		Repository clientRepo = forge.fetchRepository("alien-tools", "comp-changes-client");

		report = new MaracasReport(
			List.of(PackageReport.success(
				"/",
				DeltaDto.of(result.delta(), pr, Path.of("../test-data/comp-changes/old/")),
				List.of(ClientReport.success("alien-tools/comp-changes-client",
					result.allBrokenUses()
						.stream()
						.map(d -> BrokenUseDto.of(d, clientRepo, "main", c1.getLocation()))
						.collect(Collectors.toList())
				))
			))
		);
	}

	@Test
	void source_locations_delta() {
		// Hamcrest's hasProperty doesn't work with records yet
		//assertThat(
		//	report.delta().breakingChanges(),
		//	everyItem(allOf(
		//		hasProperty("path", not(emptyString())),
		//		hasProperty("startLine", not(equalTo(-1))),
		//		hasProperty("endLine", not(equalTo(-1)))
		//	))
		//);

		report.reports().get(0).delta().breakingChanges().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void source_locations_brokenUses() {
		assertThat(report.reports().get(0).clientReports().size(), is(1));
		report.reports().get(0).clientReports().get(0).brokenUses().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void github_locations_delta() {
		report.reports().get(0).delta().breakingChanges().forEach(d -> {
			assertThat(d.fileUrl(), not(emptyOrNullString()));
			assertThat(d.diffUrl(), not(emptyOrNullString()));
		});
	}

	@Test
	void github_locations_brokenUses() {
		assertThat(report.reports().get(0).clientReports().size(), is(1));
		assertThat(report.reports().get(0).clientReports().get(0).url(), not(emptyOrNullString()));
		report.reports().get(0).clientReports().get(0).brokenUses().forEach(d -> assertThat(d.url(), not(emptyOrNullString())));
	}

	@Test
	void github_clients_are_present() {
		assertThat(report.reports().get(0).clientReports().size(), is(1));
		assertThat(report.reports().get(0).clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(report.reports().get(0).clientReports().get(0).brokenUses().size(), is(greaterThan(1)));
	}
}
