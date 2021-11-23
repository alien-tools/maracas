package com.github.maracas.rest.breakbot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BreakbotConfig {
	public static class Build {
		String mvnPom;
		List<String> mvnGoals = new ArrayList<>();
		List<String> mvnProperties = new ArrayList<>();
		String jarLocation;

		public String getMvnPom() { return mvnPom; }
		public void setMvnPom(String p) { mvnPom = p; }
		public List<String> getMvnGoals() { return mvnGoals; }
		public void setMvnGoals(List<String> g) { mvnGoals = g; }
		public List<String> getMvnProperties() { return mvnProperties; }
		public void setMvnProperties(List<String> p) { mvnProperties = p; }
		public String getJarLocation() { return jarLocation; }
	}

	private Build build = new Build();
	private final List<String> excludes = new ArrayList<>();
	private final List<GithubRepositoryConfig> clients = new ArrayList<>();

	private static final Logger logger = LogManager.getLogger(BreakbotConfig.class);

	public Build getBuild() { return build; }
	public List<String> getExcludes() { return excludes; }
	public List<GithubRepositoryConfig> getClients() {
		return clients;
	}

	@JsonProperty("build")
	private void unpackBuild(Map<String, String> buildProps) {
		if (buildProps.containsKey("pom"))
			build.mvnPom = buildProps.get("pom");
		if (buildProps.containsKey("goals"))
			build.mvnGoals = Arrays.asList(buildProps.get("goals").split(" "));
		if (buildProps.containsKey("properties"))
			build.mvnProperties = Arrays.asList(buildProps.get("properties").split(" "));
		if (buildProps.containsKey("jar"))
			build.jarLocation = buildProps.get("jar");
	}

	public static BreakbotConfig defaultConfig() {
		return new BreakbotConfig();
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
