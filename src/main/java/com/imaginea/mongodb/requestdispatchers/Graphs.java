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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Return values of queries,updates,inserts and deletes being performed on Mongo
 * Db per sec.
 * 
 * @author Aditya Gaur, Rachit Mittal
 */
public class Graphs extends HttpServlet {
	private static final long serialVersionUID = -1539358875210511143L;

	private static JSONArray array;
	private static int num = 0;

	// TODO For multiple users - static variable do not work. So static variable
	// per mongoHost
	/**
	 * Keep the record of last values.
	 */
	private static int lastNoOfQueries = 0;
	private static int lastNoOfInserts = 0;
	private static int lastNoOfUpdates = 0;
	private static int lastNoOfDeletes = 0;
	int maxLen = 20;
	int jump = 1;

	private static Logger logger = Logger.getLogger(Graphs.class);

	/**
	 * Constructor for Servlet and also configures Logger.
	 * 
	 * @throws IOException
	 *             If Log file cannot be written
	 */
	public Graphs() throws IOException {
		super();
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		logger.info("Graphs Request Recieved [" + DateProvider.getDateTime()
				+ "]");

		// Declare Response Objects and PrintWriter
		response.setContentType("application/x-json");
		JSONObject respObj = new JSONObject();
		PrintWriter out = response.getWriter();

		// Error JSON Object to be sent in case of any exception caught
		JSONObject error = new JSONObject();
		String tokenId = request.getParameter("tokenId");

		logger.info("Token Id : [" + tokenId + "]");

		try {
			if (tokenId == null) {
				InvalidHTTPRequestException e = new InvalidHTTPRequestException(
						ErrorCodes.TOKEN_ID_ABSENT, "Token Id not provided");

				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				logger.fatal(error);

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);

			} else {

				// Check if tokenId is in session
				HttpSession session = request.getSession();

				if (session.getAttribute("tokenId") == null) {
					InvalidHTTPRequestException e = new InvalidHTTPRequestException(
							ErrorCodes.INVALID_SESSION,
							"Session Expired(Token Id not set in session).");
					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());

					logger.fatal(error);
					JSONObject temp = new JSONObject();
					temp.put("error", error);
					respObj.put("response", temp);
				} else {
					if (!session.getAttribute("tokenId").equals(tokenId)) {
						InvalidHTTPRequestException e = new InvalidHTTPRequestException(
								ErrorCodes.INVALID_SESSION,
								"Invalid Session(Token Id does not match with the one in session)");
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());

						logger.fatal(error);
						JSONObject temp = new JSONObject();
						temp.put("error", error);
						respObj.put("response", temp);

					} else {
						// Present in Session
						String user = UserLogin.tokenIDToUserMapping
								.get(tokenId);
						Mongo mongoInstance = UserLogin.userToMongoInstanceMapping
								.get(user);
						// Need a Db to get ServerStats
						DB db = mongoInstance.getDB("admin");
						String uri = request.getRequestURI();
						JSONObject temp = new JSONObject();
						if (uri != null) {
							if (uri.substring(uri.lastIndexOf('/')).equals(
									"/query")) {
								temp = processQuery(db);
							} else if (uri.substring(uri.lastIndexOf('/'))
									.equals("/initiate")) {
								if (request.getParameter("pollingTime") != null) {
									jump = Integer.parseInt(request
											.getParameter("pollingTime"));
								}
								temp = processInitiate(db);
							}

							respObj.put("response", temp);
						}
						logger.info("Response: [" + respObj + "]");

						out.print(respObj);
					}
				}
			}
		} catch (JSONException e) {
			ServletException s = new ServletException(
					"Error forming JSON Response", e.getCause());
			logger.error("Exception");
			logger.error("Message:  [ " + s.getMessage() + "]");
			logger.error("StackTrace: " + s.getStackTrace() + "]");

			throw s;
		}
	}

	/**
	 * Process <opcounters> query request made after each second by Front end
	 * 
	 * @param db
	 *            : Db Name to egt Server Stats <admin>
	 * @return Server stats of <opcounters> key
	 * @throws IOException
	 * @throws JSONException
	 */
	private JSONObject processQuery(DB db) throws IOException, JSONException {

		CommandResult cr = db.command("serverStatus");
		BasicDBObject obj = (BasicDBObject) cr.get("opcounters");
		int currentValue;
		JSONObject respObj = new JSONObject();
		JSONObject temp = new JSONObject();

		num = num + jump;
		temp.put("TimeStamp", num);
		currentValue = (Integer) obj.get("query");
		temp.put("QueryValue", currentValue - lastNoOfQueries);
		lastNoOfQueries = currentValue;
		currentValue = (Integer) obj.get("insert");
		temp.put("InsertValue", currentValue - lastNoOfInserts);
		lastNoOfInserts = currentValue;
		currentValue = (Integer) obj.get("update");
		temp.put("UpdateValue", currentValue - lastNoOfUpdates);
		lastNoOfUpdates = currentValue;
		currentValue = (Integer) obj.get("delete");
		temp.put("DeleteValue", currentValue - lastNoOfDeletes);
		lastNoOfDeletes = currentValue;

		if (array.length() == maxLen) {
			JSONArray tempArray = new JSONArray();
			for (int i = 1; i < maxLen; i++) {
				tempArray.put(array.get(i));
			}
			array = tempArray;
		}
		array.put(temp);
		respObj.put("result", array);

		return respObj;
	}

	/**
	 * 
	 * Initialize by previous <opcounter> value.
	 * 
	 * @param db
	 *            : Name of Database
	 * @return : Status of Initialization
	 * @throws RuntimeException
	 */

	private JSONObject processInitiate(DB db) throws RuntimeException,
			JSONException {

		JSONObject respObj = new JSONObject();
		array = new JSONArray();
		num = 0;
		try {

			CommandResult cr = db.command("serverStatus");
			BasicDBObject obj = (BasicDBObject) cr.get("opcounters");
			lastNoOfQueries = (Integer) obj.get("query");
			lastNoOfInserts = (Integer) obj.get("insert");
			lastNoOfUpdates = (Integer) obj.get("update");
			lastNoOfDeletes = (Integer) obj.get("delete");

			respObj.put("result", "Initiated");
		} catch (Exception e) {
			// Invalid User
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", ErrorCodes.ERROR_INITIATING_GRAPH);

			respObj.put("error", error);
			logger.info(respObj);
		}
		return respObj;
	}
}
