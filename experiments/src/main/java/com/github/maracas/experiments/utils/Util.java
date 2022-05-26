package com.github.maracas.experiments.utils;

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
	public static final LocalDate stringToLocalDate(String strDate) {
		return Date.from(Instant.parse(strDate))
			.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
	}
}
