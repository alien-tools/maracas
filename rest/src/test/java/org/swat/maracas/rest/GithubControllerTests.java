package org.swat.maracas.rest;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.model.NottableString.string;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.swat.maracas.rest.TestHelpers.checkDelta;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GithubControllerTests {
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

//		Mockito.when(maracas.computeDelta(Mockito.any(Path.class), Mockito.any(Path.class), Mockito.any(Path.class)))
//			.thenReturn(ValueFactoryFactory.getValueFactory().list());
//		Mockito.when(maracas.computeImpact(Mockito.any(Path.class), Mockito.any(Path.class), Mockito.any(Path.class), Mockito.any(Path.class)))
//			.thenReturn(ValueFactoryFactory.getValueFactory().list());
	}

	@Test
	void testSubmitAndCheckPRSync() throws Exception {
		checkDelta(mvc.perform(get("/github/pr-sync/tdegueul/comp-changes/3")));
	}

	@Test
	void testSubmitPRPoll() throws Exception {
		mvc.perform(post("/github/pr-poll/tdegueul/comp-changes/3"))
    	.andExpect(status().isAccepted())
    	.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
    	.andExpect(result -> "/github/pr/tdegueul/comp-changes/3".equals(result.getResponse().getHeader("Location")));

		checkDelta(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));
	}

	@Test
	void testSubmitPRPush() throws Exception {
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
				.contentType(MediaType.APPLICATION_JSON)
			)
				.andExpect(status().isAccepted())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(result -> "processing".equals(result.getResponse().getContentAsString()));

			checkDelta(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));

			mockServer.verify(
				request()
					.withPath("/breakbot/pr/tdegueul/comp-changes/3")
					.withMethod("POST")
					.withHeader("installationId", "123456789")
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON),
				exactly(1)
			);
		}
	}

	@Test
	void testSubmitPRPushWithouIdentificationId() throws Exception {
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
				.contentType(MediaType.APPLICATION_JSON)
			)
				.andExpect(status().isAccepted())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(result -> "processing".equals(result.getResponse().getContentAsString()));

			checkDelta(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));

			mockServer.verify(
				request()
					.withPath("/breakbot/pr/tdegueul/comp-changes/3")
					.withMethod("POST")
					.withHeader(not("installationId"), string(".*"))
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON),
				exactly(1)
			);
		}
	}

	@Test
	void testMissingCallback() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/3"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testUnknownRepository() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/NOPE/3"))
			.andExpect(status().isBadRequest());

		mvc.perform(post("/github/pr-poll/tdegueul/NOPE/3"))
			.andExpect(status().isBadRequest());

		mvc.perform(get("/github/pr-sync/tdegueul/NOPE/3"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testUnknownPR() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest());

		mvc.perform(post("/github/pr-poll/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest());

		mvc.perform(get("/github/pr-sync/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testPRExistsButNotAnalyzed() throws Exception {
		mvc.perform(get("/github/pr/tdegueul/comp-changes/1"))
			.andExpect(status().isNotFound());
	}
}
