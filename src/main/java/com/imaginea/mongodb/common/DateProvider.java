package com.imaginea.mongodb.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Provides Date and Time for Logging different messages.
 *
 * @author Rachit Mittal
 *
 */
public class DateProvider {
	public static final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";

	/**
	 *
	 * @return Current Date and Time
	 */
	public static String getDateTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
}
