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
	 * the initial date and an arbitrary number of passed days.
	 *
	 * @param lastActivity        Last activity {@link LocalDate}
	 * @param initialDate         Initial {@link LocalDate}
	 * @param lastAllowedActivity Number of days to consider back
	 * @return {@code true} if the {@link LocalDate} instance lays within the
	 *         boundaries; {@code false} otherwise
	 */
	public static boolean isActive(LocalDate lastActivity, LocalDate initialDate,
		int lastAllowedActivity) {
		Duration duration = Duration.between(lastActivity.atStartOfDay(),
			initialDate.atStartOfDay());
		return duration.toDays() <= lastAllowedActivity;
	}

	/**
	 * Indicates if a string in the form of "yyyy-MM-ddThh:mm:ssZ" lays within
	 * the boundaries of the current date and an arbitrary number of passed days.
	 *
	 * @param lastActivity        Last activity in the form of "yyyy-MM-ddThh:mm:ssZ"
	 * @param initialDate         Initial {@link LocalDate}
	 * @param lastAllowedActivity Number of days to consider back
	 * @return {@code true} if the {@link LocalDate} instance lays within the
	 *         boundaries; {@code false} otherwise
	 */
	public static boolean isActive(String lastActivity, LocalDate initialDate,
		int lastAllowedActivity) {
		LocalDate lastActivityDate = Util.stringToLocalDate(lastActivity);
		return isActive(lastActivityDate, initialDate, lastAllowedActivity);
	}

	/**
	 * Indicates if a string is a null value--that is, if the string is equal
	 * to the string "null".
	 *
	 * @param value Target string
	 * @return {@code true} if the string is equal to "null", {@code false} otherwise
	 */
	public static boolean isNullValue(String value) {
		return value.equalsIgnoreCase("null");
	}
}
