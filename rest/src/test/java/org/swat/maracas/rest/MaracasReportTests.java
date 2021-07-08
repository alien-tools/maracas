package org.swat.maracas.rest;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.data.Delta;
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
//			Detection detect = Detection.fromMaracasDetection(d);
		});
	}

}
