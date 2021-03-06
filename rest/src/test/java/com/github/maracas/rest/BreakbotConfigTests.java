package com.github.maracas.rest;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.breakbot.BreakbotException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BreakbotConfigTests {
	@Test
	void testDefaultConfiguration() {
		BreakbotConfig c = BreakbotConfig.defaultConfig();
		assertThat(c.excludes(), is(empty()));
		assertThat(c.build().module(), is(""));
		assertThat(c.build().sources(), is(emptyString()));
		assertThat(c.build().goals(), is(empty()));
		assertThat(c.build().properties(), is(anEmptyMap()));
		assertThat(c.build().jar(), nullValue());
		assertThat(c.clients(), empty());
	}

	@Test
	void testInvalidConfiguration() {
		String s = "nope";
		Assertions.assertThrows(BreakbotException.class, () -> BreakbotConfig.fromYaml(s));
	}

	@Test
	void testOneClient() {
		String s = """
			clients:
			  - repository: a/b""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.clients(), hasSize(1));

		BreakbotConfig.GitHubRepository r = c.clients().get(0);
		assertThat(r.repository(), is("a/b"));
		assertThat(r.sources(), nullValue());
		assertThat(r.branch(), nullValue());
		assertThat(r.sha(), nullValue());
	}

	@Test
	void testSeveralClients() {
		String s = """
			clients:
			  - repository: a/b
			  - repository: a/c
			  - repository: b/d""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.clients(), hasSize(3));

		BreakbotConfig.GitHubRepository r1 = c.clients().get(0);
		assertThat(r1.repository(), is("a/b"));
		assertThat(r1.sources(), nullValue());
		assertThat(r1.branch(), nullValue());
		assertThat(r1.sha(), nullValue());

		BreakbotConfig.GitHubRepository r2 = c.clients().get(1);
		assertThat(r2.repository(), is("a/c"));
		assertThat(r2.sources(), nullValue());
		assertThat(r2.branch(), nullValue());
		assertThat(r2.sha(), nullValue());

		BreakbotConfig.GitHubRepository r3 = c.clients().get(2);
		assertThat(r3.repository(), is("b/d"));
		assertThat(r3.sources(), nullValue());
		assertThat(r3.branch(), nullValue());
		assertThat(r3.sha(), nullValue());
	}

	@Test
	void testClientsWithSources() {
		String s = """
			clients:
			  - repository: a/b
			    sources: src
			  - repository: a/c""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.clients(), hasSize(2));

		BreakbotConfig.GitHubRepository r1 = c.clients().get(0);
		assertThat(r1.repository(), is("a/b"));
		assertThat(r1.sources(), is("src"));
		assertThat(r1.branch(), nullValue());
		assertThat(r1.sha(), nullValue());

		BreakbotConfig.GitHubRepository r2 = c.clients().get(1);
		assertThat(r2.repository(), is("a/c"));
		assertThat(r2.sources(), nullValue());
		assertThat(r2.branch(), nullValue());
		assertThat(r2.sha(), nullValue());
	}

	@Test
	void testCustomBuild() {
		String s = """
			build:
			  module: submodule/
			  goals: [a, b]
			  properties:
			    skipTests: true
			    skipDepClean: true""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.build().module(), is("submodule/"));
		assertThat(c.build().sources(), is(emptyString()));
		assertThat(c.build().goals(), allOf(iterableWithSize(2), hasItem("a"), hasItem("b")));
		assertThat(c.build().properties(), allOf(
			aMapWithSize(2),
			hasEntry("skipTests", "true"),
			hasEntry("skipDepClean", "true"))
		);
		assertThat(c.build().jar(), nullValue());
	}

	@Test
	void testCustomSources() {
		String s = """
			build:
			  module: module/
			  sources: module/src/main/java""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.build().module(), is("module/"));
		assertThat(c.build().sources(), is("module/src/main/java"));
	}

	@Test
	void testCustomOutput() {
		String s = """
			build:
			  jar: build/out.jar""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.build().module(), is(""));
		assertThat(c.build().goals(), is(empty()));
		assertThat(c.build().properties(), is(anEmptyMap()));
		assertThat(c.build().jar(), is("build/out.jar"));
	}

	@Test
	void testCustomBuildOutput() {
		String s = """
			build:
			  module: sub/
			  goals: [custom]
			  properties:
			    -x: test
			  jar: build/out.jar""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.build().module(), is("sub/"));
		assertThat(c.build().goals(), allOf(iterableWithSize(1), hasItem("custom")));
		assertThat(c.build().properties(), allOf(aMapWithSize(1), hasEntry("-x", "test")));
		assertThat(c.build().jar(), is("build/out.jar"));
	}

	@Test
	void testClientWithCommitOrBranch() {
		String s = """
			clients:
			  - repository: a/b
			    sources: src
			    sha: a3b98f
			  - repository: a/c
			    sha: 52f1aa
			  - repository: b/d
			  - repository: b/e
			    branch: dev""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.clients(), hasSize(4));
		assertThat(c.clients().get(0).sha(), is("a3b98f"));
		assertThat(c.clients().get(0).branch(), nullValue());
		assertThat(c.clients().get(1).sha(), is("52f1aa"));
		assertThat(c.clients().get(1).branch(), nullValue());
		assertThat(c.clients().get(2).sha(), nullValue());
		assertThat(c.clients().get(2).branch(), nullValue());
		assertThat(c.clients().get(3).sha(), nullValue());
		assertThat(c.clients().get(3).branch(), is("dev"));
	}

	@Test
	void testWithExcludes() {
		String s = """
			excludes:
			  # '@' and '*' cannot start a YAML token, we have to quote
			  - '@Beta'
			  - '*internal*'""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.excludes(), hasSize(2));
		assertThat(c.excludes(), hasItems("@Beta", "*internal*"));
	}

	@Test
	void testIgnoreUnknownProperties() {
		String s = """
			a: b
			excludes:
			  # '@' and '*' cannot start a YAML token, we have to quote
			  - '@Beta'
			  - '*internal*'
			b: c""";
		BreakbotConfig c = BreakbotConfig.fromYaml(s);
		assertThat(c.excludes(), hasSize(2));
		assertThat(c.excludes(), hasItems("@Beta", "*internal*"));
	}
}
