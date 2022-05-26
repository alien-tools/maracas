package com.github.maracas.experiments.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class to deal with the GitHub API and REST requests.
 */
public final class GitHubUtil {
	/**
	 * This class should not be instantiated.
	 */
	private GitHubUtil() {}

	/**
	 * Returns a response out from a REST post to the GitHub API.
	 *
	 * @param graphqlQuery GraphQL query
	 * @param url          URL pointing to the GitHub service (e.g. https://api.github.com/graphql)
	 * @param githubToken  GitHub access token
	 * @return REST response
	 */
	public static ResponseEntity<String> postQuery(String graphqlQuery, String url,
		String githubToken) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", String.join(" ", new String[]{"Bearer", githubToken}));
		headers.add("Content-Type", "application/graphql");
		String jsonQuery = graphqlAsJson(graphqlQuery);
		ResponseEntity<String> response = rest.postForEntity(url,
			new HttpEntity<>(jsonQuery, headers), String.class);
		return response;
	}

	/**
	 * Transforms a GraphQL query into a JSON object.
	 *
	 * @param query GraphQL query
	 * @return JSON representation of the query
	 */
	private static String graphqlAsJson(String query) {
		return "{ \"query\": \""
			+ query.replace("\n", "")  // Remove new line characters
			.replace("\t", "")         // Remove tab characters
			.replace("\"", "\\\"")     // Escape double-quotes character
			+ "\"";
	}
}
