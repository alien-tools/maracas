package org.swat.maracas.rest.breakbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Config {
	private List<String> githubClients = new ArrayList<>();

	@JsonProperty("clients")
	public void unpackClients(Map<String, List<String>> clients) {
		githubClients = clients.get("github");
	}

	public List<String> getGithubClients() {
		return githubClients;
	}

	public static Config defaultConfig() {
		return new Config();
	}

	public static Config fromYaml(InputStream in) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(in, Config.class);
	}
}
