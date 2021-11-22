package com.github.maracas.rest;

import static com.github.maracas.rest.TestHelpers.*;
import static org.hamcrest.Matchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.model.NottableString.string;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.rest.data.BrokenDeclaration;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PullRequestControllerTests {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Value("${maracas.clone-path}")
	private String clonePath;
	@Value("${maracas.report-path}")
	private String reportPath;

	@AfterEach
	public void cleanData() throws IOException {
		FileUtils.deleteDirectory(new File(clonePath));
		FileUtils.deleteDirectory(new File(reportPath));
	}

	@Test
	void testSubmitAndCheckPRSync() throws Exception {
		checkReportHasDetections(mvc.perform(post("/github/pr-sync/tdegueul/comp-changes/3")));
	}

	@Test
	void testSubmitPRPoll() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/3"))
			.andExpect(status().isAccepted())
			.andExpect(header().stringValues("Location", "/github/pr/tdegueul/comp-changes/3"))
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(content().json("{'message': 'processing', 'report': null}"));

		checkReportHasDetections(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));
	}

	@Test
	void testSubmitPRPush() throws Exception {
		int port = 8080;
		String callback = "http://localhost:" + port + "/breakbot/pr/tdegueul/comp-changes/3";

		try (ClientAndServer mockServer = ClientAndServer.startClientAndServer(port)) {
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
				.andExpect(content().json("{'message': 'processing', 'report': null}"));

			mvc.perform(get("/github/pr/tdegueul/comp-changes/3"))
				.andExpect(status().isProcessing())
				.andExpect(content().json("{'message': 'processing', 'report': null}"));

			checkReportHasDetections(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));

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

		try (ClientAndServer mockServer = ClientAndServer.startClientAndServer(port)) {
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
				.andExpect(header().stringValues("Location", "/github/pr/tdegueul/comp-changes/3"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("{'message': 'processing', 'report': null}"));

			mvc.perform(get("/github/pr/tdegueul/comp-changes/3"))
				.andExpect(status().isProcessing())
				.andExpect(content().json("{'message': 'processing', 'report': null}"));

			checkReportHasDetections(waitForPRAnalysis(mvc, "/github/pr/tdegueul/comp-changes/3"));

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
	void testUnknownRepository() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/NOPE/3"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository tdegueul/NOPE")));

		mvc.perform(post("/github/pr/tdegueul/NOPE/3?callback=foo"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository tdegueul/NOPE")));

		mvc.perform(post("/github/pr-sync/tdegueul/NOPE/3"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository tdegueul/NOPE")));
	}

	@Test
	void testUnknownPR() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository tdegueul/comp-changes")));

		mvc.perform(post("/github/pr/tdegueul/comp-changes/9999?callback=foo"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository tdegueul/comp-changes")));

		mvc.perform(post("/github/pr-sync/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository tdegueul/comp-changes")));
	}

	@Test
	void testPRExistsButNotAnalyzed() throws Exception {
		mvc.perform(get("/github/pr/tdegueul/comp-changes/1"))
			.andExpect(status().isNotFound());
	}

	@Test
	void testPRWithSuppliedBreakbotConfiguration() throws Exception {
		checkReportHasDetections(mvc.perform(
			post("/github/pr-sync/tdegueul/comp-changes/2")
				.content("""
					clients:
					  - repository: tdegueul/comp-changes-client""")
		));
	}

	@Test
	void testPRWithBuggyBuildConfiguration() throws Exception {
		mvc.perform(
				post("/github/pr-sync/tdegueul/comp-changes/2")
						.content("""
							build:
							  pom: unknown.xml""")
		)
			.andExpect(status().isInternalServerError()) // FIXME: Well, shouldn't be a 500 though
			.andExpect(jsonPath("$.message", containsString("BuildException")));
	}

	@Test
	void testPRWithBuggyClientConfiguration() throws Exception {
		checkReportHasClientError(mvc.perform(
				post("/github/pr-sync/tdegueul/comp-changes/2")
						.content("""
							clients:
							  - repository: unknown/repository""")
		))
			.andExpect(jsonPath("$.report.clientDetections[0].error", containsString("Couldn't analyze client")));
	}

	@Test
	void testPRWithExcludeCriteria() throws Exception {
		String config = """
			excludes:
			  - '@main.unstableAnnon.Beta'
			  - '*test*'
			  - '*tests*'
			  - '*unstablePkg*'""";

		PullRequestResponse response = objectMapper.readValue(
			checkReportHasDelta(mvc.perform(post("/github/pr-sync/tdegueul/comp-changes/2").content(config)))
				.andReturn().getResponse().getContentAsString(),
			PullRequestResponse.class);

		Collection<String> brokenDecls =
			response.report().delta().brokenDeclarations().stream().
			map(BrokenDeclaration::declaration).toList();

		assertThat(brokenDecls, not(hasItem(containsString("test"))));
		assertThat(brokenDecls, not(hasItem(containsString("tests"))));
		assertThat(brokenDecls, not(hasItem(containsString("unstablePkg"))));
		assertThat(brokenDecls, not(hasItem(containsString("main.unstableAnnon.classRemoved.ClassRemoved"))));
	}

	private PullRequestResponse analyzePRPush(String owner, String repository, int prId) {
		try {
			int mockPort = 8080;
			int installationId = 123456789;
			String callback = String.format("http://localhost:%d/breakbot/pr/%s/%s/%d", mockPort, owner, repository, prId);

			try (ClientAndServer mockServer = ClientAndServer.startClientAndServer(mockPort)) {
				// Start a mock server that waits for our callback request
				mockServer.when(
						request().withPath(String.format("/breakbot/pr/%s/%s/%d", owner, repository, prId))
				).respond(
						response().withBody("received")
				);

				// Check whether our analysis request is properly received
				mvc.perform(
								post(String.format("/github/pr/%s/%s/%d?callback=%s", owner, repository, prId, callback))
										.header("installationId", installationId)
						)
						.andExpect(status().isAccepted())
						.andExpect(header().stringValues("Location",
								String.format("/github/pr/%s/%s/%d", owner, repository, prId)))
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(jsonPath("$.message", is("processing")));

				// Check whether the analysis is ongoing
				mvc.perform(get("/github/pr/%s/%s/%d", owner, repository, prId))
						.andExpect(status().isProcessing())
						.andExpect(jsonPath("$.message", is("processing")));

				// Wait for the analysis to finish
				ResultActions result = waitForPRAnalysis(mvc, String.format("/github/pr/%s/%s/%d", owner, repository, prId));

				// Check whether our mock server got the callback
				mockServer.verify(
						request()
								.withPath(String.format("/breakbot/pr/%s/%s/%d", owner, repository, prId))
								.withMethod("POST")
								.withHeader("installationId", String.valueOf(installationId))
								.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON),
						exactly(1)
				);

				return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), PullRequestResponse.class);
			}
		} catch (Exception e) {
			return null;
		}
	}

	private PullRequestResponse analyzePRSync(String owner, String repository, int prId) {
		return analyzePRSync(owner, repository, prId, null);
	}

	private PullRequestResponse analyzePRSync(String owner, String repository, int prId, String config) {
		try {
			ResultActions result = mvc.perform(post(String.format("/github/pr-sync/%s/%s/%d")).content(config));
			checkReportIsValid(result);
			return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), PullRequestResponse.class);
		} catch (Exception e) {
			return null;
		}
	}
}
