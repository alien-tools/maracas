package org.swat.maracas.rest.breakbot;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.swat.maracas.rest.data.Delta;

public class BreakBot {
	private final URI callbackUri;

	public BreakBot(URI callbackUri) {
		this.callbackUri = callbackUri;
	}

	public void sendDelta(Delta d) {
		String json = d.toJson();
		WebClient client = WebClient.create();

		MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();

		bodyValues.add("delta", json);

		WebClient.ResponseSpec res =
			client.post()
			.uri(callbackUri)
			.body(BodyInserters.fromFormData(bodyValues))
			.retrieve();

		System.out.println(res);
	}

	public static void main(String[] args) {
		try {
			new BreakBot(new URI("https://breakbot-app.herokuapp.com/probot/publish"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
