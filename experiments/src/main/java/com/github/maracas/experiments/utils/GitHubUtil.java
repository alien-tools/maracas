package com.github.maracas.experiments.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	 * Class logger
	 */
	private static final Logger logger = LogManager.getLogger(GitHubUtil.class);

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
		try {
			RestTemplate rest = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", String.join(" ", new String[]{"Bearer", githubToken}));
			headers.add("Content-Type", "application/graphql");
			String jsonQuery = graphqlAsJson(graphqlQuery);
			ResponseEntity<String> response = rest.postForEntity(url,
				new HttpEntity<>(jsonQuery, headers), String.class);
			return response;
		} catch (Exception e) {
			logger.info("Too many requests, sleeping...");
			try {
				Thread.sleep(30000);
				postQuery(graphqlQuery, url, githubToken);
			} catch (InterruptedException ie) {
				logger.error(e);
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	/**
	 * Returns a response out from a REST get to the GitHub API.
	 *
	 * @param url         URL pointing to the GitHub service (e.g. https://api.github.com/search)
	 * @param githubToken GitHub access token
	 * @return REST response
	 */
	public static ResponseEntity<String> getQuery(String url, String githubToken) {
		try {
			RestTemplate rest = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", String.join(" ", new String[]{"token", githubToken}));
			ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET,
				new HttpEntity<Object>(headers), String.class);
			return response;
		} catch (Exception e) {
			logger.info("Too many requests, sleeping...");
			try {
				Thread.sleep(30000);
				getQuery(url, githubToken);
			} catch (InterruptedException ie) {
				logger.error(e);
				Thread.currentThread().interrupt();
			}
		}
		return null;
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
				logger.info("Too many requests, sleeping...");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException ee) {
					logger.error(e);
					Thread.currentThread().interrupt();
				}
				return fetchPage(url);
			}
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public static String toGitHubDateFormat(LocalDateTime datetime) {
		LocalDate date = datetime.toLocalDate();
		LocalTime time = datetime.toLocalTime();
		ZoneId zone = ZoneId.of("Europe/Amsterdam");
		ZoneOffset offset = zone.getRules().getOffset(datetime);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		return "%sT%s%s".formatted(date.toString(), time.format(formatter), offset);
	}
}
