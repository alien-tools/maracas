package com.github.maracas.experiments.utils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Helper class.
 */
public final class Util {
	/**
	 * This class should not be instantiated.
	 */
	private Util() {}

	/**
	 * Converts a string in the form "yyyy-MM-ddThh:mm:ssZ" to a {@link LocalDate}
	 * object.
	 *
	 * @param strDate String in the form "yyyy-MM-ddThh:mm:ssZ" representing a date
	 * @return {@link LocalDate} based on input string
	 */
	public static LocalDate stringToLocalDate(String strDate) {
		return Date.from(Instant.parse(strDate))
			.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
	}

	/**
	 * Indicates if a {@link LocalDate} instance lays within the boundaries of
	 * the current date and an arbitrary number of passed days.
	 *
	 * @param lastActivity        {@link LocalDate} instance
	 * @param lastAllowedActivity Number of days to consider back
	 * @return {@code true} if the {@link LocalDate} instance lays within the
	 *         boundaries; {@code false} otherwise
	 */
	public static boolean isActive(LocalDate lastActivity, int lastAllowedActivity) {
		LocalDate now = LocalDate.now();
		Duration duration = Duration.between(lastActivity.atStartOfDay(), now.atStartOfDay());
		return duration.toDays() <= lastAllowedActivity;
	}

	/**
	 *
	 * @param url
	 * @return
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
