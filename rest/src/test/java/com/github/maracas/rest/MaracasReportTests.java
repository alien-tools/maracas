package com.github.maracas.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.rest.data.ClientDetections;
import com.github.maracas.rest.data.Delta;
import com.github.maracas.rest.data.Detection;
import com.github.maracas.rest.data.MaracasReport;

class MaracasReportTests {
	private MaracasReport report;

	@BeforeEach
	void setUp() {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
		Path c1 = Paths.get("../test-data/comp-changes/client/src");
		Path sources = Paths.get("../test-data/comp-changes/old/src");
		String libGithub = "tdegueul/comp-changes";
		String clientGithub = "tdegueul/comp-changes-client";

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(v1)
			.newJar(v2)
			.sources(sources)
			.client(c1)
			.build();
		AnalysisResult result = new Maracas().analyze(query);

		report = new MaracasReport(
			Delta.fromMaracasDelta(result.delta(), libGithub, "main", "../test-data/comp-changes/old/"),
			Arrays.asList(new ClientDetections(clientGithub,
				result.allDetections()
					.stream()
					.map(d -> Detection.fromMaracasDetection(d, clientGithub, "main", c1.toString()))
					.collect(Collectors.toList())
			))
		);
	}

	@Test
	void testSourceLocationsDelta() {
		// Hamcrest's hasProperty doesn't work with records yet
		//assertThat(
		//	report.delta().brokenDeclarations(),
		//	everyItem(allOf(
		//		hasProperty("path", not(emptyString())),
		//		hasProperty("startLine", not(equalTo(-1))),
		//		hasProperty("endLine", not(equalTo(-1)))
		//	))
		//);

		report.delta().brokenDeclarations().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void testSourceLocationsDetections() {
		assertThat(report.clientDetections().size(), is(1));
		report.clientDetections().get(0).getDetections().forEach(d -> {
			assertThat(d.path(),      not(emptyOrNullString()));
			assertThat(d.startLine(), greaterThan(0));
			assertThat(d.startLine(), greaterThan(0));
		});
	}

	@Test
	void testGitHubLocationsDelta() {
		report.delta().brokenDeclarations().forEach(d -> {
			assertThat(d.url(), not(emptyOrNullString()));
		});
	}

	@Test
	void testGitHubLocationsDetections() {
		assertThat(report.clientDetections().size(), is(1));
		assertThat(report.clientDetections().get(0).getUrl(), not(emptyOrNullString()));
		report.clientDetections().get(0).getDetections().forEach(d -> {
			assertThat(d.url(),       not(emptyOrNullString()));
		});
	}

	@Test
	void testGithubClientsArePresent() {
		assertThat(report.clientDetections().size(), is(1));
		assertThat(report.clientDetections().get(0).getUrl(), is("tdegueul/comp-changes-client"));
		assertThat(report.clientDetections().get(0).getDetections().size(), is(greaterThan(1)));
	}

}
