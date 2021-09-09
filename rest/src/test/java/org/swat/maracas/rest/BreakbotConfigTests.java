package org.swat.maracas.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.breakbot.BreakbotConfig;

class BreakbotConfigTests {

	@Test
	void testEmpty() {
		String s = """
			""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getClients(), is(empty()));
	}

	@Test
	void testOneClient() {
		String s = """
			clients:
			  - repository: a/b""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getClients(), hasSize(1));
	}

	@Test
	void testOtherClients() {
		String s = """
			clients:
			  - repository: a/b
			  - repository: a/c
			  - repository: b/d""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getClients(), hasSize(3));
	}

	@Test
	void testClientsWithSources() {
		String s = """
			clients:
			  - repository: a/b
			    sources: src
			  - repository: a/c
			  - repository: b/d
			    sources: src""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getClients(), hasSize(3));
	}

	@Test
	void testCustomBuild() {
		String s = """
			build:
			  pom: anotherpom.xml
			  goals: package
			  properties: skipTests""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getMvnPom(), is("anotherpom.xml"));
		assertThat(c.getMvnGoals(), hasSize(1));
		assertThat(c.getMvnGoals().get(0), is("package"));
		assertThat(c.getMvnProperties(), hasSize(1));
		assertThat(c.getMvnProperties().get(0), is("skipTests"));
		assertThat(c.getJarLocation(), nullValue());
	}

	@Test
	void testCustomOutput() {
		String s = """
			build:
			  jar: build/out.jar""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getJarLocation(), is("build/out.jar"));
		assertThat(c.getMvnPom(), nullValue());
		assertThat(c.getMvnGoals(), is(empty()));
		assertThat(c.getMvnProperties(), is(empty()));
	}

	@Test
	void testCustomBuildOutput() {
		String s = """
			build:
			  pom: anotherpom.xml
			  goals: package
			  properties: skipTests
			  jar: build/out.jar""";
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(s, Charset.defaultCharset()));
		assertThat(c.getMvnPom(), is("anotherpom.xml"));
		assertThat(c.getMvnGoals(), hasSize(1));
		assertThat(c.getMvnGoals().get(0), is("package"));
		assertThat(c.getMvnProperties(), hasSize(1));
		assertThat(c.getMvnProperties().get(0), is("skipTests"));
		assertThat(c.getJarLocation(), is("build/out.jar"));
	}

}
