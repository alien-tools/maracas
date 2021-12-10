package com.github.maracas.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.Delta;
import com.github.maracas.rest.data.Detection;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaracasReportTests {
	@Autowired
	private GitHub github;
	private MaracasReport report;

	@BeforeEach
	void setUp() {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
		Path c1 = Paths.get("../test-data/comp-changes/client/src");
		Path sources = Paths.get("../test-data/comp-changes/old/src");

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(v1)
			.newJar(v2)
			.sources(sources)
			.client(c1)
			.build();
		AnalysisResult result = Maracas.analyze(query);

		report = new MaracasReport(
			Delta.fromMaracasDelta(result.delta(), new PullRequest("alien-tools", "comp-changes", 2), "main", "../test-data/comp-changes/old/"),
			List.of(ClientReport.success("alien-tools/comp-changes-client",
				result.allBrokenUses()
					.stream()
					.map(d -> Detection.fromMaracasDetection(d, "alien-tools", "comp-changes-client", "main", c1.toString()))
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
	        if (d.startLine() < 0) {
	            System.out.println(d);
	        }
        });

		report.delta().breakingChanges().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void testSourceLocationsDetections() {
		assertThat(report.clientReports().size(), is(1));
		report.clientReports().get(0).detections().forEach(d -> {
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
	void testGitHubLocationsDetections() {
		assertThat(report.clientReports().size(), is(1));
		assertThat(report.clientReports().get(0).url(), not(emptyOrNullString()));
		report.clientReports().get(0).detections().forEach(d -> assertThat(d.url(), not(emptyOrNullString())));
	}

	@Test
	void testGithubClientsArePresent() {
		assertThat(report.clientReports().size(), is(1));
		assertThat(report.clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(report.clientReports().get(0).detections().size(), is(greaterThan(1)));
	}

}
