package com.github.maracas.rest;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class MaracasConfiguration {
	public static final Path GH_CACHE = Path.of("./github-cache");

	/**
	 * Attempts to build a cache-enabled OAuth-authenticated GitHub handler
	 * <p>
	 * In normal conditions, this should come from a .github file in the classpath
	 * In CI environments, this should come from a GITHUB_OAUTH environment variable
	 */
	@Bean
	public GitHub gitHub(@Autowired ResourceLoader resourceLoader) throws IOException {
		File cacheDir = GH_CACHE.toFile();
		OkHttpClient.Builder okBuilder = new OkHttpClient().newBuilder();
		if (cacheDir.exists() || cacheDir.mkdirs()) {
			Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);
			okBuilder.cache(cache);
		}
		GitHubConnector connector = new OkHttpGitHubConnector(okBuilder.build());

		Resource githubRes = resourceLoader.getResource("classpath:.github");
		if (githubRes.exists()) {
			try (InputStream in = githubRes.getInputStream()) {
				Properties props = new Properties();
				props.load(in);
				return GitHubBuilder
					.fromProperties(props)
					.withConnector(connector)
					.build();
			}
		}

		return GitHubBuilder
			.fromEnvironment()
			.withConnector(connector)
			.build();
	}
}
