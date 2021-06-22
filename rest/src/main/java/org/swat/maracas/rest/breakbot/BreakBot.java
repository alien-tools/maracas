package org.swat.maracas.rest.breakbot;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.swat.maracas.rest.GithubService;
import org.swat.maracas.rest.data.Delta;

import net.minidev.json.JSONObject;

public class BreakBot {
	private final URI callbackUri;
	private static final Logger logger = LogManager.getLogger(GithubService.class);

	public BreakBot(URI callbackUri) {
		this.callbackUri = callbackUri;
	}

	public boolean sendDelta(Delta d) {
		Map<String, String> bodyValues = new HashMap<>();
		bodyValues.put("delta", d.toJson());
		String body = new JSONObject(bodyValues).toJSONString();

		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(body, headers);
		String res = rest.postForObject(callbackUri, request, String.class);

		logger.info("Sent delta back to BreakBot ({}): {}", callbackUri, res);
		return "ok".equals(res);
	}
}
