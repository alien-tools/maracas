package org.swat.maracas.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class MaracasConfiguration {
	@Bean
	public GitHub gitHub(@Autowired ResourceLoader resourceLoader) throws IOException {
		Resource githubRes = resourceLoader.getResource("classpath:.github");
		try (InputStream in = githubRes.getInputStream()) {
			Properties props = new Properties();
			props.load(in);
			return GitHubBuilder.fromProperties(props).build();
		}
	}
}
