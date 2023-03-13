package com.github.maracas.rest;

import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = {"maracas.breakbot-file=.not-found"})
class ConfigLessGithubControllerTests extends AbstractControllerTest {
	@Test
	void analyze_PR_push_configLess() {
		PullRequestResponse res = resultAsPR(analyzePRPush("alien-tools", "repository-fixture", 1));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().reports(), hasSize(2));

		PackageReport reportA = res.report().reports().get(0);
		assertThat(reportA.delta().breakingChanges(), hasSize(1));
		assertThat(reportA.clientReports(), is(empty()));
		assertThat(reportA.allBrokenUses(), is(empty()));

		PackageReport reportB = res.report().reports().get(1);
		assertThat(reportB.delta().breakingChanges(), hasSize(1));
		assertThat(reportB.clientReports(), is(empty()));
		assertThat(reportB.allBrokenUses(), is(empty()));
	}
}
