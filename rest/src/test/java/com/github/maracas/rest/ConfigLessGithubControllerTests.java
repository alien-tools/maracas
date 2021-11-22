package com.github.maracas.rest;

import com.github.maracas.rest.data.PullRequestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = {"maracas.breakbot-file=.not-found"})
class ConfigLessGithubControllerTests extends AbstractControllerTest {
	@Test
	void testAnalyzePRPushConfigLess() throws Exception {
		PullRequestResponse res = resultAsPR(analyzePRPush("alien-tools", "comp-changes", 3));
		assertThat(res.message(), is("ok"));
		assertThat(res.report(), is(notNullValue()));
		assertThat(res.report().delta().brokenDeclarations(), not(empty()));
		assertThat(res.report().clientDetections(), empty());
	}
}
