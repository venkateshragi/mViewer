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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.exceptions.ApplicationException;
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
 * Defines validation functions for validating dbInfo from session. An error
 * JSON object is returned when dbInfo is found invalid.
 * 
 * @author Rachit Mittal
 */
public class BaseRequestDispatcher {
	/**
	 * Validates dbInfo with the dbInfo Array present in session.
	 * 
	 * @param dbInfo
	 *            Mongo Db config information provided to user at time of login.
	 * @param logger
	 *            Logger to write error message to
	 * @param request
	 *            Request made by client containing session attributes.
	 * @return null if dbInfo is valid else error object.
	 */
	protected static String validateDbInfo(String dbInfo, Logger logger, HttpServletRequest request) {

		String response = null;
		if (dbInfo == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.DB_INFO_ABSENT, "Mongo Config parameters not provided in the URL");
			return formErrorResponse(logger, e);
		}
		// Check if db information is present in session
		HttpSession session = request.getSession();
		@SuppressWarnings("unchecked")
		List<String> mongosInSession = (List<String>) session.getAttribute("dbInfo");

		InvalidHTTPRequestException e = null;
		if (mongosInSession == null) {
			e = new InvalidHTTPRequestException(ErrorCodes.INVALID_SESSION, "No Mongo Config parameters present in Session.");
			return formErrorResponse(logger, e);
		}
		if (!mongosInSession.contains(dbInfo)) {
			e = new InvalidHTTPRequestException(ErrorCodes.INVALID_SESSION, "Provided Mongo Config parameters not present in session");
			return formErrorResponse(logger, e);
		}
		return response;
	}

	/**
	 * Returns the JSON error response after an invalid dbInfo is found.
	 * 
	 * @param logger
	 *            Logger to log the error response.
	 * @param message
	 *            Error Message to be returned in the JSON Error Object.
	 * @param errorCode
	 *            Error Code to be returned in the JSON Error Object.
	 * @return JSON Error String.
	 */
	protected static String formErrorResponse(Logger logger, ApplicationException e) {

		String response = null;
		JSONObject jsonErrorResponse = new JSONObject();
		JSONObject error = new JSONObject();
		try {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject tempResponse = new JSONObject();
			tempResponse.put("error", error);
			jsonErrorResponse.put("response", tempResponse);
			response = jsonErrorResponse.toString();
		} catch (JSONException m) {
			logger.error(m);
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
		}
		return response;
	}

	/**
	 *  Catches error for a block of code and from JSON error Response.
	 * 
	 *
	 */
	protected static class ErrorTemplate {
		public static String execute(Logger logger, ResponseCallback callback) {
			Object response = null;
			try {
				Object dispatcherResponse = callback.execute();
				JSONObject tempResult = new JSONObject();
				JSONObject jsonResponse = new JSONObject();
				tempResult.put("result", dispatcherResponse);
				jsonResponse.put("response", tempResult);
				// resp.put("totalRecords", dispatcherResponse.size());
				response = jsonResponse.toString();
			} catch (JSONException e) {
				logger.error(e);
				response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
			} catch (NumberFormatException m) {
				ApplicationException e = new ApplicationException(ErrorCodes.ERROR_PARSING_PORT, "Invalid Port", m.getCause());
				response = formErrorResponse(logger, e);
			} catch (IllegalArgumentException m) {
				// When port out of range
				ApplicationException e = new ApplicationException(ErrorCodes.PORT_OUT_OF_RANGE, "Port out of range", m.getCause());
				response = formErrorResponse(logger, e);

			} catch (UnknownHostException m) {
				MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
				response = formErrorResponse(logger, e);
			} catch (MongoInternalException m) {
				MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
				response = formErrorResponse(logger, e);
			} catch (MongoException m) {
				MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
				response = formErrorResponse(logger, e);
			} catch (DatabaseException e) {
				response = formErrorResponse(logger, e);
			} catch (CollectionException e) {
				response = formErrorResponse(logger, e);
			} catch (DocumentException e) {
				response = formErrorResponse(logger, e);
			} catch (ValidationException e) {
				response = formErrorResponse(logger, e);
			} catch (Exception m) {
				ApplicationException e = new ApplicationException(ErrorCodes.ANY_OTHER_EXCEPTION, m.getMessage(), m.getCause());
				response = formErrorResponse(logger, e);
			}

			return response.toString();
		}
	}

	protected static class ResponseTemplate {
		public String execute(Logger logger, String dbInfo, HttpServletRequest request, ResponseCallback callback) {
			Object response = null;
			response = validateDbInfo(dbInfo, logger, request);
			if (response != null) {
				return response.toString();
			}
			return ErrorTemplate.execute(logger,callback);
		}
	}

	protected interface ResponseCallback {
		public Object execute() throws Exception;
		// object can be collecitn
	}
}
