package com.github.maracas.experiments.utils;

import java.io.IOException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
	 * Returns a response out from a REST get to the GitHub API.
	 *
	 * @param url         URL pointing to the GitHub service (e.g. https://api.github.com/search)
	 * @param githubToken GitHub access token
	 * @return REST response
	 */
	public static ResponseEntity<String> getQuery(String url, String githubToken) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", String.join(" ", new String[]{"token", githubToken}));
		ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET,
			new HttpEntity<Object>(headers), String.class);
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

	/**
	 * Returns a {@link Document} instance representing a page given a URL.
	 *
	 * @param url URL to fetch
	 * @return {@link Document} instance
	 */
	public static Document fetchPage(String url) {
		var ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
		var ref = "http://www.google.com";

		try {
			Thread.sleep(250);
			return Jsoup.connect(url).userAgent(ua).referrer(ref).get();
		} catch (HttpStatusException e) {
			if (e.getStatusCode() == 429) {
				System.out.println("Too many requests, sleeping...");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException ee) {
					ee.printStackTrace();
					Thread.currentThread().interrupt();
				}
				return fetchPage(url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return null;
	}
}
