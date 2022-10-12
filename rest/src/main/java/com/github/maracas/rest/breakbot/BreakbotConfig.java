package com.github.maracas.rest.breakbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BreakbotConfig(
	List<String> excludes,
	Build build,
	Clients clients
) {
	public record Build(
		String module,
		List<String> goals,
		Map<String, String> properties,
		String jar
	) {
		public Build(String module, List<String> goals, Map<String, String> properties, String jar) {
			this.module = module != null ? module : "";
			this.goals = goals != null && !goals.isEmpty() ? goals : Collections.emptyList();
			this.properties = properties != null && !properties.isEmpty() ? properties : Collections.emptyMap();
			this.jar = jar;
		}
	}

	public record Clients(
		int top,
		int stars,
		List<GitHubRepository> repositories
	) {
		public Clients(int top, int stars, List<GitHubRepository> repositories) {
			this.top = Math.max(top, 0);
			this.stars = Math.max(stars, 0);
			this.repositories = repositories != null ? repositories : Collections.emptyList();
		}
	}

	public record GitHubRepository(
		String repository,
		String branch,
		String module,
		String sha
	) {

	}

	public BreakbotConfig(List<String> excludes, Build build, Clients clients) {
		this.excludes = excludes != null ? excludes : Collections.emptyList();
		this.build = build != null ? build : new Build(null, null, null, null);
		this.clients = clients != null ? clients : new Clients(0, 0, Collections.emptyList());
	}

	public static BreakbotConfig defaultConfig() {
		return new BreakbotConfig(null, null, null);
	}

	public static BreakbotConfig fromYaml(InputStream in) {
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			return mapper.readValue(in, BreakbotConfig.class);
		} catch (IOException e) {
			throw new BreakbotException("Couldn't parse .github/breakbot.yml: ", e);
		}
	}

	public static BreakbotConfig fromYaml(String yaml) {
		return fromYaml(IOUtils.toInputStream(yaml, Charset.defaultCharset()));
	}
}
