package org.swat.maracas.rest.breakbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class BreakBotConfig {
	private String buildCommand;
	private String jarLocation;
	private List<String> githubClients = new ArrayList<>();

	public String getBuildCommand() {
		return buildCommand;
	}

	public String getJarLocation() {
		return jarLocation;
	}

	public List<String> getGithubClients() {
		return githubClients;
	}

	@JsonProperty("build")
	private void unpackBuild(Map<String, String> build) {
		buildCommand = build.get("command");
		jarLocation = build.get("jar");
	}

	@JsonProperty("clients")
	private void unpackClients(Map<String, List<String>> clients) {
		githubClients = clients.get("github");
	}

	public static BreakBotConfig defaultConfig() {
		return new BreakBotConfig();
	}

	public static BreakBotConfig fromYaml(InputStream in) {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(in, BreakBotConfig.class);
		} catch (IOException e) {
			return defaultConfig();
		}
	}
}
