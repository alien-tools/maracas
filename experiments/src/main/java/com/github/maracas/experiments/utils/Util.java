package com.github.maracas.experiments.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
}
