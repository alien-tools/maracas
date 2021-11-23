package com.github.maracas.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AbstractControllerTest {
	@Autowired
	protected MockMvc mvc;
	@Autowired
	protected ObjectMapper objectMapper;

	@Value("${maracas.clone-path}")
	protected String clonePath;
	@Value("${maracas.report-path}")
	protected String reportPath;

	@BeforeEach
	public void cleanData() throws IOException {
		FileUtils.deleteDirectory(new File(clonePath));
		FileUtils.deleteDirectory(new File(reportPath));
	}

	protected MvcResult analyzePRPush(String owner, String repository, int prId, String config) {
		try {
			int mockPort = 8080;
			int installationId = 123456789;
			String callback = "http://localhost:%d/breakbot/pr/%s/%s/%d".formatted(mockPort, owner, repository, prId);

			try (ClientAndServer mockServer = ClientAndServer.startClientAndServer(mockPort)) {
				// Start a mock server that waits for our callback request
				mockServer.when(
					request().withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
				).respond(
					response().withBody("received")
				);

				// Check whether our analysis request is properly received
				mvc.perform(
						post("/github/pr/%s/%s/%d?callback=%s".formatted(owner, repository, prId, callback))
							.header("installationId", installationId)
							.content(config)
					)
					.andExpect(status().isAccepted())
					.andExpect(header().stringValues("Location",
						"/github/pr/%s/%s/%d".formatted(owner, repository, prId)))
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.message", is("processing")));

				// Check whether the analysis is ongoing
				mvc.perform(get("/github/pr/%s/%s/%d".formatted(owner, repository, prId)))
					.andExpect(status().isProcessing())
					.andExpect(jsonPath("$.message", is("processing")));

				// Wait for the analysis to finish
				ResultActions result = waitForPRAnalysis(mvc, "/github/pr/%s/%s/%d".formatted(owner, repository, prId));

				// Check whether our mock server got the callback
				mockServer.verify(
					request()
						.withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
						.withMethod("POST")
						.withHeader("installationId", String.valueOf(installationId))
						.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON),
					exactly(1)
				);

				return result.andReturn();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected MvcResult analyzePRPush(String owner, String repository, int prId) {
		return analyzePRPush(owner, repository, prId, "");
	}

	protected MvcResult analyzePRSync(String owner, String repository, int prId) {
		return analyzePRSync(owner, repository, prId, "");
	}

	protected MvcResult analyzePRSync(String owner, String repository, int prId, String config) {
		try {
			ResultActions result = mvc.perform(
				post("/github/pr-sync/%s/%s/%d".formatted(owner, repository, prId)).content(config)
			);
			checkReportIsValid(result);
			return result.andReturn();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected MvcResult analyzePRPoll(String owner, String repository, int prId) {
		try {
			mvc.perform(post("/github/pr/%s/%s/%d".formatted(owner, repository, prId)))
				.andExpect(status().isAccepted())
				.andExpect(header().stringValues("Location", "/github/pr/%s/%s/%d".formatted(owner, repository, prId)))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", is("processing")));

			ResultActions result = waitForPRAnalysis(mvc, "/github/pr/%s/%s/%d".formatted(owner, repository, prId));
			return result.andReturn();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected PullRequestResponse resultAsPR(MvcResult result) {
		try {
			return objectMapper.readValue(result.getResponse().getContentAsString(), PullRequestResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static ResultActions checkReportIsValid(ResultActions res) throws Exception {
		return res
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message", is("ok")));
	}

	// Sigh
	protected static ResultActions waitForPRAnalysis(MockMvc mvc, String uri) throws Exception {
		CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(() -> {
			ResultActions res = null;
			try {
				do {
					res = mvc.perform(get(uri));
					Thread.sleep(1000);
				} while(res.andReturn().getResponse().getStatus() != HttpStatus.SC_OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		});

		return future.get(30, TimeUnit.SECONDS);
	}
}
