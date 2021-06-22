package org.swat.maracas.rest.breakbot;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.swat.maracas.rest.data.Delta;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BreakBot {
	private final URI callbackUri;
	private final int installationId;
	private static final Logger logger = LogManager.getLogger(BreakBot.class);

	static class BreakBotResponse {
		public int installationId;
		public Delta delta;
		public BreakBotResponse(int installationId, Delta delta) {
			this.installationId = installationId;
			this.delta = delta;
		}
	}

	public BreakBot(URI callbackUri, int installationId) {
		this.callbackUri = callbackUri;
		this.installationId = installationId;
	}

	public boolean sendDelta(Delta d) {
		try {
			BreakBotResponse response = new BreakBotResponse(installationId, d);
			ObjectMapper mapper = new ObjectMapper();
			String body = mapper.writeValueAsString(response);

			RestTemplate rest = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
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
