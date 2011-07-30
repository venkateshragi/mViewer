/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
