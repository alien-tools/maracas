package com.github.maracas.rest.services;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Service
public class BreakbotService {
	@Autowired
	private GitHub github;
	@Value("${maracas.breakbot-file:.github/breakbot.yml}")
	private String breakbotFile;

	private static final Logger logger = LogManager.getLogger(BreakbotService.class);

	public void sendPullRequestResponse(PullRequestResponse pr, String callback, String installationId) {
		try {
			URI callbackUri = new URI(callback);
			RestTemplate rest = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			if (installationId != null && !installationId.isEmpty())
				headers.set("installationId", installationId);

			HttpEntity<String> request = new HttpEntity<>(pr.toJson(), headers);
			String res = rest.postForObject(callbackUri, request, String.class);

			logger.info("Sent delta back to BreakBot ({}): {}", callbackUri, res);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public BreakbotConfig readBreakbotConfig(String owner, String repository) {
		String fullName = owner + "/" + repository;

		try (InputStream configIn = github.getRepository(fullName).getFileContent(breakbotFile).read()) {
			BreakbotConfig res = BreakbotConfig.fromYaml(configIn);
			if (res != null)
				return res;
		} catch (@SuppressWarnings("unused") IOException e) {
			logger.error(e);
		}

		return BreakbotConfig.defaultConfig();
	}
}
