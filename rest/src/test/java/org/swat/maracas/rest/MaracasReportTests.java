package org.swat.maracas.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.Detection;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.spoon.VersionAnalyzer;

class MaracasReportTests {
	private MaracasReport report;

	@BeforeEach
	void setUp() {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path c1 = Paths.get("/home/dig/repositories/comp-changes-data/client/");
		Path sources = Paths.get("/home/dig/repositories/comp-changes-data/old/src");
		String libGithub = "tdegueul/comp-changes";
		String clientGithub = "tdegueul/comp-changes-client";

		VersionAnalyzer analyzer = new VersionAnalyzer(v1, v2);
		analyzer.computeDelta();
		analyzer.analyzeClient(c1);
		analyzer.populateLocations(sources);

		report = new MaracasReport(
			Delta.fromMaracasDelta(analyzer.getDelta(), libGithub, "/home/dig/repositories/comp-changes-data/old/"),
			analyzer.getDetections()
				.stream()
				.map(d -> Detection.fromMaracasDetection(d, clientGithub, c1.toString()))
				.collect(Collectors.toSet())
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
		report.detections().forEach(d -> {
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
		report.detections().forEach(d -> {
			assertThat(d.url(),       not(emptyOrNullString()));
			assertThat(d.clientUrl(), not(emptyOrNullString()));
		});
	}

}
