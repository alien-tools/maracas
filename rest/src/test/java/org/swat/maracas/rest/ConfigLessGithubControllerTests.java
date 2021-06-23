package org.swat.maracas.rest;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.swat.maracas.rest.TestHelpers.checkDeltaWithoutDetections;
import static org.swat.maracas.rest.TestHelpers.waitForPRAnalysis;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"maracas.breakbot-file=.not-found"})
class ConfigLessGithubControllerTests {
	@Autowired
	private MockMvc mvc;

	@Value("${maracas.clone-path}")
	private String clonePath;
	@Value("${maracas.delta-path}")
	private String deltaPath;

	@BeforeEach
	public void cleanData() throws IOException {
		FileUtils.deleteDirectory(new File(clonePath));
		FileUtils.deleteDirectory(new File(deltaPath));
	}

	@Test
	void testSubmitPRPushConfigLess() throws Exception {
		int port = 8080;
		String callback = "http://localhost:" + port + "/breakbot/pr/tdegueul/comp-changes/3";

		try (ClientAndServer mockServer = startClientAndServer(port)) {
			mockServer.when(
				request().withPath("/breakbot/pr/tdegueul/comp-changes/3")
			).respond(
				response().withBody("received")
			);

			mvc.perform(
				post("/github/pr/tdegueul/comp-changes/3?callback=" + callback)
				.header("installationId", 123456789)
			)
				.andExpect(status().isAccepted())
				.andExpect(header().stringValues("Location", "/github/pr/tdegueul/comp-changes/3"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("{'message': 'processing', 'delta': null}"));

			ResultActions res = waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3");
			checkDeltaWithoutDetections(res);

			mockServer.verify(
				request()
					.withPath("/breakbot/pr/tdegueul/comp-changes/3")
					.withMethod("POST")
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON),
				exactly(1)
			);
		}
	}
}
