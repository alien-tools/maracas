package org.swat.maracas.rest.breakbot;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.swat.maracas.rest.data.Delta;

public class BreakBot {
	private final URI callbackUri;
	private final String installationId;
	private static final Logger logger = LogManager.getLogger(BreakBot.class);

	public BreakBot(URI callbackUri, String installationId) {
		this.callbackUri = callbackUri;
		this.installationId = installationId;
	}

	public boolean sendDelta(Delta d) {
		try {
			RestTemplate rest = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			if (installationId != null && !installationId.isEmpty())
				headers.set("installationId", installationId);

			String body = d.toJson();
			HttpEntity<String> request = new HttpEntity<>(body, headers);
			String res = rest.postForObject(callbackUri, request, String.class);

			logger.info("Sent delta back to BreakBot ({}): {}", callbackUri, res);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
}
