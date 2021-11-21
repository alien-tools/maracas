package com.github.maracas.rest;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("slow")
class SpoonTests {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private GitHub github;

	@Value("${maracas.clone-path}")
	private String clonePath;
	@Value("${maracas.report-path}")
	private String reportPath;

	@AfterEach
	public void cleanData() throws IOException {
		FileUtils.deleteDirectory(new File(clonePath));
		FileUtils.deleteDirectory(new File(reportPath));
	}

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

		String bbYaml = """
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

		for (GHPullRequest pr : javaPRs)
			mvc.perform(post("/github/pr-sync/INRIA/spoon/" + pr.getNumber()).content(bbYaml))
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
	}
}
