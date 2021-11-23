package com.github.maracas.rest.breakbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public record BreakbotConfig(
	List<String> excludes,
	Build build,
	List<GitHubRepository> clients
) {
	public record Build(
		String pom,
		List<String> goals,
		List<String> properties,
		String jar
	) {
		public Build(String pom, List<String> goals, List<String> properties, String jar) {
			this.pom = pom != null ? pom : "pom.xml";
			this.goals  = goals != null && !goals.isEmpty() ? goals : List.of("package");
			this.properties = properties != null && !properties.isEmpty() ? properties : List.of("skipTests");
			this.jar = jar;
		}
	}

	public record GitHubRepository(
		String repository,
		String sources,
		String branch,
		String sha
	) {

	}

	public BreakbotConfig(List<String> excludes, Build build, List<GitHubRepository> clients) {
		this.excludes = excludes != null ? excludes : Collections.emptyList();
		this.build = build != null ? build : new Build(null, null, null, null);
		this.clients = clients != null ? clients : Collections.emptyList();
	}

	private static final Logger logger = LogManager.getLogger(BreakbotConfig.class);

	public static BreakbotConfig defaultConfig() {
		return new BreakbotConfig(null, null, null);
	}

	public static BreakbotConfig fromYaml(InputStream in) {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(in, BreakbotConfig.class);
		} catch (IOException e) {
			logger.warn("Couldn't parse .breakbot.yml: returning default configuration", e);
			return defaultConfig();
		}
	}

	public static BreakbotConfig fromYaml(String yaml) {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(yaml, BreakbotConfig.class);
		} catch (IOException e) {
			logger.warn("Couldn't parse .breakbot.yml: returning default configuration", e);
			return defaultConfig();
		}
	}
}
