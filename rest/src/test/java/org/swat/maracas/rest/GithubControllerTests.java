package org.swat.maracas.rest;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
	}

	@Test
	void testSubmitAndCheckPRSync() throws Exception {
		isValidDelta(mvc.perform(get("/github/pr-sync/tdegueul/comp-changes/3")));
	}

	@Test
	void testSubmitPRPoll() throws Exception {
		mvc.perform(post("/github/pr-poll/tdegueul/comp-changes/3"))
    	.andExpect(status().isAccepted())
    	.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
    	.andExpect(result -> "/github/pr/tdegueul/comp-changes/3".equals(result.getResponse().getHeader("Location")));

		isValidDelta(waitForPRAnalysis("/github/pr/tdegueul/comp-changes/3"));
	}

	@Test
	void testSubmitPRPush() throws Exception {
		int port = 8080;
		String callback = "http://localhost:" + port + "/pr/tdegueul/comp-changes/3";

		try (ClientAndServer mockServer = startClientAndServer(port)) {
			mvc.perform(post("/github/pr/tdegueul/comp-changes/3?callback=" + callback))
				.andExpect(status().isAccepted())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(result -> "processing".equals(result.getResponse().getContentAsString()));

			ResultActions res = waitForPRAnalysis("/github/pr/tdegueul/comp-changes/3");
			isValidDelta(res);
			String delta = res.andReturn().getResponse().getContentAsString();

			mockServer.verify(
				request()
					.withPath("/pr/tdegueul/comp-changes/3")
					.withMethod("POST")
					.withBody(new JsonBody(delta)),
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

	private void isValidDelta(ResultActions res) throws Exception {
		res
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message", is("ok")))
			.andExpect(jsonPath("$.delta.breakingChanges", not(empty())))
			.andExpect(jsonPath("$.delta.breakingChanges[*].detections[*]", not(empty())));
	}

	// Sigh
	private ResultActions waitForPRAnalysis(String uri) throws Exception {
		CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(() -> {
			ResultActions res = null;
			try {
				do {
					res = mvc.perform(get(uri));
					Thread.sleep(1000);
				} while(res != null && res.andReturn().getResponse().getStatus() != HttpStatus.SC_OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		});

		return future.get(30, TimeUnit.SECONDS);
	}
}
