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
package com.imaginea.mongodb.requestdispatchers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;

/**
 * Defines validation functions for validating tokenId given to user. An error
 * JSON object is returned when tokenId is found invalid after validating it
 * against tokenId in session.
 * 
 * @author Rachit Mittal
 * 
 */
public class BaseRequestDispatcher {

	/**
	 * Validates tokenId with the one present in session.
	 * 
	 * @param tokenId
	 *            Token Id provided to user at time of login.
	 * @param logger
	 *            Logger to write error message to
	 * @param request
	 *            Request made by client containing session attributes.
	 * @return null if token Id is valid else error object.
	 */
	protected String validateTokenId(String tokenId, Logger logger, HttpServletRequest request) {
		String response = null;
		if (tokenId == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.TOKEN_ID_ABSENT, "Token Id not provided");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(), null, "FATAL");
		}
		// Check if tokenId is in session
		HttpSession session = request.getSession();
		if (session.getAttribute("tokenId") == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.INVALID_SESSION,
					"Session Expired(Token Id not set in session).");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(), null, "FATAL");
		}
		if (!session.getAttribute("tokenId").equals(tokenId)) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.INVALID_SESSION,
					"Invalid Session(Token Id does not match with the one in session)");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(), null, "FATAL");
		}

		return response;
	}

	/**
	 * Returns the JSON error response after an invalid tokenId is found.
	 * 
	 * @param logger
	 *            Logger to log the error response.
	 * @param message
	 *            Error Message to be returned in the JSON Error Object.
	 * @param errorCode
	 *            Error Code to be returned in the JSON Error Object.
	 * @return JSON Error String.
	 */
	protected static String formErrorResponse(Logger logger, String message, String errorCode, StackTraceElement[] stackTrace, String level) {
		String response = null;
		JSONObject resp = new JSONObject();
		JSONObject error = new JSONObject();
		try {

			error.put("message", message);
			error.put("code", errorCode);
			error.put("stackTrace", stackTrace);
			if (level.equals("FATAL")) {
				logger.fatal(error);
			}
			if (level.equals("ERROR")) {
				logger.error(error);
			}
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			resp.put("response", temp);
			response = resp.toString();

		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
		}

		return response;
	}
}
