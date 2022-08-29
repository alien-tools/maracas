package com.github.maracas.rest;

import com.github.maracas.rest.data.BreakingChange;
import com.github.maracas.rest.data.PullRequestResponse;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.http.MediaType;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.subString;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PullRequestControllerTests extends AbstractControllerTest {
	@Test
	void testAnalyzePRSync() {
		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "comp-changes", 6));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().delta().breakingChanges(), not(empty()));
		assertThat(res.report().clientReports().size(), equalTo(1));
		assertThat(res.report().clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(res.report().allBrokenUses().size(), greaterThan(0));
	}

	@Test
	void testAnalyzePRPoll() {
		PullRequestResponse res = resultAsPR(analyzePRPoll("alien-tools", "comp-changes", 6));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().delta().breakingChanges(), not(empty()));
		assertThat(res.report().clientReports().size(), equalTo(1));
		assertThat(res.report().clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(res.report().allBrokenUses().size(), greaterThan(0));
	}

	@Test
	void testAnalyzePRPush() {
		PullRequestResponse res = resultAsPR(analyzePRPush("alien-tools", "comp-changes", 6));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().delta().breakingChanges(), not(empty()));
		assertThat(res.report().clientReports().size(), equalTo(1));
		assertThat(res.report().clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(res.report().allBrokenUses().size(), greaterThan(0));
	}

	@Test
	void testUnknownRepository() throws Exception {
		mvc.perform(post("/github/pr/alien-tools/NOPE/3"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository alien-tools/NOPE")));

		mvc.perform(post("/github/pr/alien-tools/NOPE/3?callback=foo"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository alien-tools/NOPE")));

		mvc.perform(post("/github/pr-sync/alien-tools/NOPE/3"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch repository alien-tools/NOPE")));
	}

	@Test
	void testUnknownPR() throws Exception {
		mvc.perform(post("/github/pr/alien-tools/comp-changes/9999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository alien-tools/comp-changes")));

		mvc.perform(post("/github/pr/alien-tools/comp-changes/9999?callback=foo"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository alien-tools/comp-changes")));

		mvc.perform(post("/github/pr-sync/alien-tools/comp-changes/9999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository alien-tools/comp-changes")));
	}

	@Test
	void testPRExistsButNotAnalyzed() throws Exception {
		mvc.perform(get("/github/pr/alien-tools/comp-changes/1"))
			.andExpect(status().isNotFound());
	}

	@Test
	void testPRWithSuppliedBreakbotConfiguration() {
		String bbConfig = """
			clients:
			  - repository: alien-tools/comp-changes-client""";

		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "comp-changes", 6, bbConfig));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().delta().breakingChanges(), not(empty()));
		assertThat(res.report().clientReports().size(), equalTo(1));
		assertThat(res.report().clientReports().get(0).url(), is("alien-tools/comp-changes-client"));
		assertThat(res.report().allBrokenUses().size(), greaterThan(0));
	}

	@Test
	void testPRWithBuggyBuildConfiguration() throws Exception {
		String bbConfig = """
			build:
			  module: unknown/""";

		mvc.perform(post("/github/pr-sync/alien-tools/comp-changes/6").content(bbConfig))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.message", containsString("BuildException")));
	}

	@Test
	void testAnalyzeUnknownPRPush() throws Exception {
		String owner = "this-does-not-exist";
		String repository = "this-does-not-exist";
		int prId = 9999;

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
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Couldn't fetch repository this-does-not-exist/this-does-not-exist")));

			// Check whether our mock server got the error callback
			mockServer.verify(
				request()
					.withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
					.withMethod("POST")
					.withBody(json("{\"message\": \"Couldn't fetch repository this-does-not-exist/this-does-not-exist\", report: null}")),
				exactly(1)
			);
		}
	}

	@Test
	void testPRPushWithBuggyBuildConfiguration() throws Exception {
		String owner = "alien-tools";
		String repository = "comp-changes";
		int prId = 2;
		String bbConfig = """
			build:
			  module: unknown/""";

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
						.content(bbConfig)
				)
				.andExpect(status().isAccepted())
				.andExpect(header().stringValues("Location",
					"/github/pr/%s/%s/%d".formatted(owner, repository, prId)))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", is("processing")));

			Thread.sleep(5000);

			// Check whether our mock server got the error callback
			mockServer.verify(
				request()
					.withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
					.withMethod("POST")
					.withHeader("installationId", String.valueOf(installationId))
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
					.withBody(subString("Don't know how to build")),
				exactly(1)
			);
		}
	}

	@Test
	void testPRWithBuggyClientConfiguration() throws Exception {
		String bbConfig = """
			clients:
			  - repository: unknown/repository""";

		mvc.perform(post("/github/pr-sync/alien-tools/comp-changes/6").content(bbConfig))
			.andExpect(jsonPath("$.report.clientReports[0].error", containsString("Couldn't fetch repository")));
	}

	@Test
	void testPRWithInvalidBreakbotFile() throws Exception {
		String bbConfig = "nope";

		mvc.perform(post("/github/pr-sync/alien-tools/comp-changes/6").content(bbConfig))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", containsString("Couldn't parse .github/breakbot.yml")));
	}

	@Test
	void testPRPushWithInvalidBreakbotFile() throws Exception {
		String owner = "alien-tools";
		String repository = "comp-changes";
		int prId = 2;
		String bbConfig = "nope";

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
						.content(bbConfig)
				)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", containsString("Couldn't parse .github/breakbot.yml")));

			Thread.sleep(5000);

			// Check whether our mock server got the error callback
			mockServer.verify(
				request()
					.withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
					.withMethod("POST")
					.withHeader("installationId", String.valueOf(installationId))
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
					.withBody(subString("Couldn't parse .github/breakbot.yml")),
				exactly(1)
			);
		}
	}

	@Test
	void testPRWithExcludeCriteria() {
		String bbConfig = """
			excludes:
			  - '@main.unstableAnnon.Beta'
			  - '*test*'
			  - '*tests*'
			  - '*unstablePkg*'""";

		PullRequestResponse response = resultAsPR(analyzePRSync("alien-tools", "comp-changes", 6, bbConfig));

		Collection<String> brokenDecls =
			response.report().delta().breakingChanges().stream().
			map(BreakingChange::declaration).toList();
		assertThat(brokenDecls, not(hasItem(containsString("test"))));
		assertThat(brokenDecls, not(hasItem(containsString("tests"))));
		assertThat(brokenDecls, not(hasItem(containsString("unstablePkg"))));
		assertThat(brokenDecls, not(hasItem(containsString("main.unstableAnnon.classRemoved.ClassRemoved"))));
	}
}
