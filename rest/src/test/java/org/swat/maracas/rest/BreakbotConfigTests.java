package org.swat.maracas.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.swat.maracas.rest.breakbot.BreakBotConfig;

class BreakbotConfigTests {

	@Test
	void testNoClient() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertThat(c.getGithubClients(), is(empty()));
	}

	@Test
	void testOneClient() {
		StringBuilder sb = new StringBuilder();
		sb.append("clients:\n");
		sb.append("  github:\n");
		sb.append("    - a/b\n");
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
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
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertThat(c.getGithubClients(), hasSize(3));
	}

	@Test
	void testCustomBuild() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  command: mvn -B -f anotherpom.xml\n");
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("mvn -B -f anotherpom.xml", c.getBuildCommand());
		assertNull(c.getJarLocation());
	}

	@Test
	void testCustomOutput() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  jar: build/out.jar\n");
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("build/out.jar", c.getJarLocation());
		assertNull(c.getBuildCommand());
	}

	@Test
	void testCustomBuildOutput() {
		StringBuilder sb = new StringBuilder();
		sb.append("build:\n");
		sb.append("  command: mvn -B -f anotherpom.xml\n");
		sb.append("  jar: build/out.jar\n");
		BreakBotConfig c = BreakBotConfig.fromYaml(IOUtils.toInputStream(sb.toString(), Charset.defaultCharset()));
		assertEquals("mvn -B -f anotherpom.xml", c.getBuildCommand());
		assertEquals("build/out.jar", c.getJarLocation());
	}

}
