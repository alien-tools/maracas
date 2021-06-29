package org.swat.maracas.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.breakbot.BreakbotConfig;

class BreakbotConfigTests {

	@Test
	void testNoClient() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertThat(c.getGithubClients(), is(empty()));
	}

	@Test
	void testOneClient() {
		StringBuilder sb = new StringBuilder();
		sb.append("clients:\n");
		sb.append("  github:\n");
		sb.append("    - a/b\n");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertThat(c.getGithubClients(), hasSize(1));
	}

	@Test
	void testOtherClients() {
		StringBuilder sb = new StringBuilder();
		sb.append("clients:\n");
		sb.append("  github:\n");
		sb.append("    - a/b\n");
		sb.append("    - a/c\n");
		sb.append("    - b/d\n");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertThat(c.getGithubClients(), hasSize(3));
	}

	@Test
	void testCustomBuild() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  pom: anotherpom.xml\n");
		sb.append("  goals: package\n");
		sb.append("  properties: skipTests");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("anotherpom.xml", c.getMvnPom());
		assertThat(c.getMvnGoals(), hasSize(1));
		assertEquals("package", c.getMvnGoals().get(0));
		assertThat(c.getMvnProperties(), hasSize(1));
		assertEquals("skipTests", c.getMvnProperties().get(0));
		assertNull(c.getJarLocation());
	}

	@Test
	void testCustomOutput() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  jar: build/out.jar\n");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("build/out.jar", c.getJarLocation());
		assertNull(c.getMvnPom());
		assertThat(c.getMvnGoals(), is(empty()));
		assertThat(c.getMvnProperties(), is(empty()));
	}

	@Test
	void testCustomBuildOutput() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  pom: anotherpom.xml\n");
		sb.append("  goals: package\n");
		sb.append("  properties: skipTests\n");
		sb.append("  jar: build/out.jar\n");
		BreakbotConfig c = BreakbotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("anotherpom.xml", c.getMvnPom());
		assertThat(c.getMvnGoals(), hasSize(1));
		assertEquals("package", c.getMvnGoals().get(0));
		assertThat(c.getMvnProperties(), hasSize(1));
		assertEquals("skipTests", c.getMvnProperties().get(0));
		assertEquals("build/out.jar", c.getJarLocation());
	}

}
