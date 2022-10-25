package com.github.maracas.rest;

import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
			  properties:
			    maven.test.skip: true
			    skipDepClean: true
			    assembly.skipAssembly: true
			    jacoco.skip: true
			    mdep.skip: true
			clients:
			  repositories:
			    - repository: SpoonLabs/flacoco
			    - repository: SpoonLabs/coming
			    - repository: SpoonLabs/astor
			    - repository: SpoonLabs/npefix
			    - repository: SpoonLabs/nopol
			      module: nopol
			    - repository: STAMP-project/AssertFixer
			    - repository: Spirals-Team/casper
			    - repository: SpoonLabs/CoCoSpoon
			    - repository: STAMP-project/dspot
			      module: dspot
			    - repository: SpoonLabs/gumtree-spoon-ast-diff
			    - repository: Spirals-Team/jPerturb
			    - repository: SpoonLabs/metamutator
			    - repository: SpoonLabs/spooet
			    - repository: KTH/spork""";

		javaPRs.parallelStream().forEach(pr -> {
			PullRequestResponse res = resultAsPR(analyzePRSync("INRIA", "spoon", pr.getNumber(), bbConfig));
			assertThat(res.message(), is("ok"));
			assertThat(res.report(), is(notNullValue()));
			assertThat(res.report().reports(), is(not(empty())));

			PackageReport report = res.report().reports().get(0);
			assertThat(report.delta(), is(notNullValue()));
			report.clientReports().forEach(r -> {
				assertThat(r.error(), is(nullValue()));
			});
		});
	}
}
