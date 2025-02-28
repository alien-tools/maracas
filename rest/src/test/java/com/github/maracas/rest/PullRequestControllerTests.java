package com.github.maracas.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.rest.data.ModuleReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.exactly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PullRequestControllerTests {
	@Autowired
	protected MockMvc mvc;
	@Autowired
	protected ObjectMapper objectMapper;

	@Value("${maracas.clone-path}")
	protected String clonePath;
	@Value("${maracas.report-path}")
	protected String reportPath;

	@BeforeEach
	public void cleanData() {
		FileUtils.deleteQuietly(new File(clonePath));
		FileUtils.deleteQuietly(new File(reportPath));
	}

	@Test
	void pr_sync() {
		PullRequestResponse res = resultAsPR(analyzePRSync("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		List<ModuleReport> reports = res.report().reports();
		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:module-a"))
			.findFirst()
			.ifPresentOrElse(reportA -> {
				assertThat(reportA.delta().breakingChanges(), hasSize(1));
				assertThat(reportA.clientReports(), hasSize(2));
				assertThat(reportA.allBrokenUses(), hasSize(1));
			}, Assertions::fail);

		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:nested-b"))
			.findFirst()
			.ifPresentOrElse(reportB -> {
				assertThat(reportB.delta().breakingChanges(), hasSize(1));
				assertThat(reportB.clientReports(), hasSize(2));
				assertThat(reportB.allBrokenUses(), hasSize(1));
			}, Assertions::fail);
	}

	@Test
	void pr_poll() {
		PullRequestResponse res = resultAsPR(analyzePRPoll("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		List<ModuleReport> reports = res.report().reports();
		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:module-a"))
			.findFirst()
			.ifPresentOrElse(reportA -> {
				assertThat(reportA.delta().breakingChanges(), hasSize(1));
				assertThat(reportA.clientReports(), hasSize(2));
				assertThat(reportA.allBrokenUses(), hasSize(1));
			}, Assertions::fail);

		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:nested-b"))
			.findFirst()
			.ifPresentOrElse(reportB -> {
				assertThat(reportB.delta().breakingChanges(), hasSize(1));
				assertThat(reportB.clientReports(), hasSize(2));
				assertThat(reportB.allBrokenUses(), hasSize(1));
			}, Assertions::fail);

		assertThat(Path.of(reportPath).resolve("alien-tools-repository-fixture-1-21a0098.json").toFile(), aFileWithSize(greaterThan(0L)));
	}

	@Test
	void pr_push() {
		PullRequestResponse res = resultAsPR(analyzePRPush("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		List<ModuleReport> reports = res.report().reports();
		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:module-a"))
			.findFirst()
			.ifPresentOrElse(reportA -> {
				assertThat(reportA.delta().breakingChanges(), hasSize(1));
				assertThat(reportA.clientReports(), hasSize(2));
				assertThat(reportA.allBrokenUses(), hasSize(1));
			}, Assertions::fail);

		reports.stream()
			.filter(r -> r.id().equals("com.github.alien-tools:nested-b"))
			.findFirst()
			.ifPresentOrElse(reportB -> {
				assertThat(reportB.delta().breakingChanges(), hasSize(1));
				assertThat(reportB.clientReports(), hasSize(2));
				assertThat(reportB.allBrokenUses(), hasSize(1));
			}, Assertions::fail);

		assertThat(Path.of(reportPath).resolve("alien-tools-repository-fixture-1-21a0098.json").toFile(), aFileWithSize(greaterThan(0L)));
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
	void unknown_pr_push() throws Exception {
		String owner = "this-does-not-exist";
		String repository = "this-does-not-exist";
		int prId = 9999;

		int mockPort = 8181;
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

	protected MvcResult analyzePRPush(String owner, String repository, int prId) {
		try {
			int mockPort = 8181;
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

	protected MvcResult analyzePRSync(String owner, String repository, int prId) {
		try {
			ResultActions result = mvc.perform(post("/github/pr-sync/%s/%s/%d".formatted(owner, repository, prId)));
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

	protected static void checkReportIsValid(ResultActions res) throws Exception {
		res
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

		return future.get(60, TimeUnit.SECONDS);
	}

	/*
	 * These shall be migrated to forges/
	 *
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
	}*/
}
