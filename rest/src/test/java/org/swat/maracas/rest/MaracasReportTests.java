package org.swat.maracas.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.Detection;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.spoon.VersionAnalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;

class MaracasReportTests {

	@Test
	void test() throws Exception {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path c1 = Paths.get("/home/dig/repositories/comp-changes-data/client/src");

		VersionAnalyzer analyzer = new VersionAnalyzer(v1, v2);
		analyzer.computeDelta();
		analyzer.analyzeClient(c1);

		Delta delta = Delta.fromMaracasDelta(analyzer.getDelta());
		ObjectMapper mapper = new ObjectMapper();
    // pretty print
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delta);
    System.out.println(json);

		analyzer.getDetections().forEach(d -> {
			try {
				Detection detect = Detection.fromMaracasDetection(d);
				String dJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(detect);
				System.out.println(dJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	void test2() throws Exception {
		Path v1 = Paths.get("/home/dig/repositories/comp-changes-data/old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/comp-changes-data/new/target/comp-changes-0.0.2.jar");
		Path c1 = Paths.get("/home/dig/repositories/comp-changes-data/client/src");

		VersionAnalyzer analyzer = new VersionAnalyzer(v1, v2);
		analyzer.computeDelta();
		analyzer.analyzeClient(c1);

		MaracasReport report = new MaracasReport(
			Delta.fromMaracasDelta(analyzer.getDelta()),
			analyzer.getDetections()
				.stream()
				.map(Detection::fromMaracasDetection)
				.collect(Collectors.toSet())
		);

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
		System.out.println(json);
	}

}
