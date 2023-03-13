package com.github.maracas.rest;

import com.github.maracas.rest.data.BreakingChangeDto;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.PackageReport;
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
	void pr_sync() {
		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:module-a")).findFirst().get();
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), hasSize(2));
		assertThat(reportA.allBrokenUses(), hasSize(1));

		PackageReport reportB = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:nested-b")).findFirst().get();
		assertThat(reportB.delta().breakingChanges(), hasSize(1));
		assertThat(reportB.clientReports(), hasSize(2));
		assertThat(reportB.allBrokenUses(), hasSize(1));
	}

	@Test
	void pr_poll() {
		PullRequestResponse res = resultAsPR(analyzePRPoll("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:module-a")).findFirst().get();
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), hasSize(2));
		assertThat(reportA.allBrokenUses(), hasSize(1));

		PackageReport reportB = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:nested-b")).findFirst().get();
		assertThat(reportB.delta().breakingChanges(), hasSize(1));
		assertThat(reportB.clientReports(), hasSize(2));
		assertThat(reportB.allBrokenUses(), hasSize(1));
	}

	@Test
	void pr_push() {
		PullRequestResponse res = resultAsPR(analyzePRPush("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:module-a")).findFirst().get();
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), hasSize(2));
		assertThat(reportA.allBrokenUses(), hasSize(1));

		PackageReport reportB = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:nested-b")).findFirst().get();
		assertThat(reportB.delta().breakingChanges(), hasSize(1));
		assertThat(reportB.clientReports(), hasSize(2));
		assertThat(reportB.allBrokenUses(), hasSize(1));
	}

	@Test
	void unknown_repository() throws Exception {
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
	void unknown_pr() throws Exception {
		mvc.perform(post("/github/pr/alien-tools/repository-fixture/9999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", is("Couldn't fetch PR 9999 from repository alien-tools/repository-fixture")));
	}

	@Test
	void pr_exists_but_not_analyzed() throws Exception {
		mvc.perform(get("/github/pr/alien-tools/repository-fixture/1"))
			.andExpect(status().isNotFound());
	}

	@Test
	void pr_with_supplied_breakbot_configuration() {
		String bbConfig = """
			clients:
			  repositories:
			    - repository: alien-tools/comp-changes-client""";

		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "repository-fixture", 1, bbConfig));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:module-a")).findFirst().get();
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), hasSize(1));
		assertThat(reportA.clientReports().get(0).url(), is(equalTo("alien-tools/comp-changes-client")));
		assertThat(reportA.allBrokenUses(), is(empty()));

		PackageReport reportB = res.report().reports().stream().filter(r -> r.id().equals("com.github.alien-tools:nested-b")).findFirst().get();
		assertThat(reportB.delta().breakingChanges(), hasSize(1));
		assertThat(reportB.clientReports(), hasSize(1));
		assertThat(reportA.clientReports().get(0).url(), is(equalTo("alien-tools/comp-changes-client")));
		assertThat(reportA.allBrokenUses(), is(empty()));
	}

	@Test
	void pr_with_unknown_or_buggy_client() {
		String bbConfig = """
			clients:
			  repositories:
			    - repository: alien-tools/unknown-client
			    - repository: alien-tools/client-fixture-a
			    - repository: alien-tools/comp-changes-client-error""";

		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "repository-fixture", 1, bbConfig));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().get(0);
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), hasSize(3));

		ClientReport unknown = reportA.clientReports().stream().filter(r -> r.url().equals("alien-tools/unknown-client")).findFirst().get();
		assertThat(unknown.error(), containsString("Couldn't fetch repository alien-tools/unknown-client"));
		assertThat(unknown.brokenUses(), is(empty()));

		ClientReport fixture = reportA.clientReports().stream().filter(r -> r.url().equals("alien-tools/client-fixture-a")).findFirst().get();
		assertThat(fixture.error(), is(nullValue()));
		assertThat(fixture.brokenUses(), hasSize(1));

		ClientReport error = reportA.clientReports().stream().filter(r -> r.url().equals("alien-tools/comp-changes-client-error")).findFirst().get();
		assertThat(error.error(), containsString("Unable to read the pom"));
		assertThat(error.brokenUses(), is(empty()));
	}

	@Test
	void pr_with_buggy_build_configuration() throws Exception {
		String bbConfig = """
			build:
			  goals: [unknown]""";

		mvc.perform(post("/github/pr-sync/alien-tools/repository-fixture/1").content(bbConfig))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.report.reports[0].id", equalTo("com.github.alien-tools:module-a")))
			.andExpect(jsonPath("$.report.reports[0].error", containsString("Unknown lifecycle phase")))
			.andExpect(jsonPath("$.report.reports[0].delta", nullValue()))
			.andExpect(jsonPath("$.report.reports[0].clientReports", empty()))
			.andExpect(jsonPath("$.report.reports[1].id", equalTo("com.github.alien-tools:nested-b")))
			.andExpect(jsonPath("$.report.reports[1].error", containsString("Unknown lifecycle phase")))
			.andExpect(jsonPath("$.report.reports[1].delta", nullValue()))
			.andExpect(jsonPath("$.report.reports[1].clientReports", empty()));
	}

	@Test
	void unknown_pr_push() throws Exception {
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
					.withBody(json("{\"message\": \"Couldn't fetch repository this-does-not-exist/this-does-not-exist\", \"report\": null}")),
				exactly(1)
			);
		}
	}

	@Test
	void pr_push_with_buggy_build_configuration() throws Exception {
		String owner = "alien-tools";
		String repository = "repository-fixture";
		int prId = 1;
		String bbConfig = """
			build:
			  goals: [unknown]""";

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

			Thread.sleep(10000);

			// Check whether our mock server got the error callback
			mockServer.verify(
				request()
					.withPath("/breakbot/pr/%s/%s/%d".formatted(owner, repository, prId))
					.withMethod("POST")
					.withHeader("installationId", String.valueOf(installationId))
					.withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
					.withBody(subString("Unknown lifecycle phase")),
				exactly(1)
			);
		}
	}

	@Test
	void pr_with_buggy_client_configuration() throws Exception {
		String bbConfig = """
			clients:
			  repositories:
			    - repository: unknown/repository""";

		mvc.perform(post("/github/pr-sync/alien-tools/repository-fixture/1").content(bbConfig))
			.andExpect(jsonPath("$.report.reports[0].clientReports[0].error", containsString("Couldn't fetch repository")));
	}

	@Test
	void pr_with_invalid_breakbot_file() throws Exception {
		String bbConfig = "nope";

		mvc.perform(post("/github/pr-sync/alien-tools/repository-fixture/1").content(bbConfig))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", containsString("Couldn't parse .github/breakbot.yml")));
	}

	@Test
	void pr_push_with_invalid_breakbot_file() throws Exception {
		String owner = "alien-tools";
		String repository = "repository-fixture";
		int prId = 1;
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
	void pr_with_exclude_criteria() {
		String bbConfig = """
			excludes:
			  - '@main.unstableAnnon.Beta'
			  - '*test*'
			  - '*tests*'
			  - '*unstablePkg*'""";

		PullRequestResponse response = resultAsPR(analyzePRSync("alien-tools", "comp-changes", 6, bbConfig));

		Collection<String> brokenDecls =
			response.report().reports().get(0).delta().breakingChanges().stream().
				map(BreakingChangeDto::declaration).toList();
		assertThat(brokenDecls, not(hasItem(containsString("test"))));
		assertThat(brokenDecls, not(hasItem(containsString("tests"))));
		assertThat(brokenDecls, not(hasItem(containsString("unstablePkg"))));
		assertThat(brokenDecls, not(hasItem(containsString("main.unstableAnnon.classRemoved.ClassRemoved"))));
	}
}
