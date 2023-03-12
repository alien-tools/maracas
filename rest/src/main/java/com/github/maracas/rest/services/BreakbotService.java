package com.github.maracas.rest.services;

import com.github.maracas.forges.Repository;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.data.PullRequestResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
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
	private final GitHub github;
	private final String breakbotFile;

	private static final Logger logger = LogManager.getLogger(BreakbotService.class);

	public BreakbotService(GitHub github, @Value("${maracas.breakbot-file:.github/breakbot.yml}") String breakbotFile) {
		this.github = github;
		this.breakbotFile = breakbotFile;
	}

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

	public BreakbotConfig buildBreakbotConfig(Repository repository, String breakbotYaml) {
		return StringUtils.isEmpty(breakbotYaml)
			? readBreakbotConfig(repository)
			: BreakbotConfig.fromYaml(breakbotYaml);
	}

	public BreakbotConfig readBreakbotConfig(Repository repository) {
		try (InputStream configIn = github.getRepository(repository.fullName()).getFileContent(breakbotFile).read()) {
			BreakbotConfig res = BreakbotConfig.fromYaml(configIn);
			if (res != null)
				return res;
		} catch (IOException e) {
			logger.error(e);
		}

		return BreakbotConfig.defaultConfig();
	}
}
