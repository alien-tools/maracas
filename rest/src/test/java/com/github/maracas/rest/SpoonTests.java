package com.github.maracas.rest;

import com.github.maracas.rest.data.PullRequestResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Tag("slow")
class SpoonTests extends AbstractControllerTest {
	@Autowired
	private GitHub github;

	/**
	 * Checks the latest 5 PRs affecting Java code on INRIA/spoon
	 */
	@Test
	void testLatest5PRs() throws Exception {
		List<GHPullRequest> javaPRs =
			github.getRepository("INRIA/spoon")
				.getPullRequests(GHIssueState.OPEN)
				.stream()
				.filter(pr -> {
					try {
						return pr.listFiles().toList().stream().anyMatch(file -> file.getFilename().endsWith(".java"));
					} catch (IOException e) {
						return false;
					}
				})
				.limit(5)
				.toList();

		String bbConfig = """
			build:
			  properties: skipTests skipDepClean assembly.skipAssembly
			clients:
			  - repository: SpoonLabs/flacoco
			  - repository: SpoonLabs/coming
			  - repository: SpoonLabs/astor
			  - repository: SpoonLabs/npefix
			  - repository: SpoonLabs/nopol
			    sources: nopol/src/main/java
			  - repository: SpoonLabs/npefix
			  - repository: STAMP-project/AssertFixer
			  - repository: Spirals-Team/casper
			  - repository: SpoonLabs/CoCoSpoon
			  - repository: STAMP-project/dspot
			    sources: dspot/src/main/java
			  - repository: SpoonLabs/gumtree-spoon-ast-diff
			  - repository: Spirals-Team/jPerturb
			  - repository: SpoonLabs/metamutator
			  - repository: SpoonLabs/spooet
			  - repository: KTH/spork""";

		for (GHPullRequest pr : javaPRs) {
			PullRequestResponse res = resultAsPR(analyzePRSync("INRIA", "spoon", pr.getNumber(), bbConfig));
			assertThat(res.message(), is("ok"));
			assertThat(res.report(), is(notNullValue()));
			assertThat(res.report().delta(), is(notNullValue()));
		}
	}
}
