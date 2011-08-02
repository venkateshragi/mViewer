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

import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;

/**
 * Defines validation functions for validating tokenId given to user. An error
 * JSON object is returned when tokenId is found invalid after validating it
 * against tokenId in session.
 * 
 * @author Rachit Mittal
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
	protected String validateTokenId(String tokenId, Logger logger,
			HttpServletRequest request) {
		String response = null;
		if (tokenId == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(
					ErrorCodes.TOKEN_ID_ABSENT, "Token Id not provided");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
					null, "FATAL");
		}
		// Check if tokenId is in session
		HttpSession session = request.getSession();
		if (session.getAttribute("tokenId") == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(
					ErrorCodes.INVALID_SESSION,
					"Session Expired(Token Id not set in session).");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
					null, "FATAL");
		}
		if (!session.getAttribute("tokenId").equals(tokenId)) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(
					ErrorCodes.INVALID_SESSION,
					"Invalid Session(Token Id does not match with the one in session)");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
					null, "FATAL");
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
	protected static String formErrorResponse(Logger logger, String message,
			String errorCode, StackTraceElement[] stackTrace, String level) {
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
			logger.error(e);
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\","
					+ "\"message\": \"Error while forming JSON Object\"}";
		}
		return response;
	}

	protected static class ResponseTemplate {
		public String execute(Logger logger, ResponseCallback callback) {
			String response = null;
			try {
				response = callback.execute();
			} catch (JSONException e) {
				logger.error(e);
				response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION
						+ "\","
						+ "\"message\": \"Error while forming JSON Object\"}";
			} catch (NumberFormatException e) {
				response = formErrorResponse(logger, "Invalid Port",
						ErrorCodes.ERROR_PARSING_PORT, e.getStackTrace(),
						"ERROR");
			} catch (IllegalArgumentException e) {
				// When port out of range
				response = formErrorResponse(logger, "Port out of range",
						ErrorCodes.PORT_OUT_OF_RANGE, e.getStackTrace(),
						"ERROR");
			} catch (UnknownHostException m) {
				MongoHostUnknownException e = new MongoHostUnknownException(
						"Unknown host", m);
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (MongoInternalException m) {
				MongoHostUnknownException e = new MongoHostUnknownException(
						"Unknown host", m);
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (MongoException m) {
				MongoHostUnknownException e = new MongoHostUnknownException(
						"Unknown host", m);
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (DatabaseException e) {
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (CollectionException e) {
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (DocumentException e) {
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (ValidationException e) {
				response = formErrorResponse(logger, e.getMessage(),
						e.getErrorCode(), e.getStackTrace(), "ERROR");
			} catch (Exception e) {
				response = formErrorResponse(logger, e.getMessage(),
						ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(),
						"ERROR");
			}
			return response;
		}
	}

	protected interface ResponseCallback {
		public String execute() throws Exception;
	}
}
