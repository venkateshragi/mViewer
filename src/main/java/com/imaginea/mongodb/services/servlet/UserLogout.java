/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.imaginea.mongodb.services.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.mongodb.Mongo;

/**
 * Servlet that listens at a Logout Request and destroys User Id from the the
 * maps in UserLogin class and also from session.
 */

/**
 * Path mapping for this servlet as mentioned in web.xml is {/logout}
 */

public class UserLogout extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/**
	 * Define Logger for this class
	 */
	private static Logger logger = Logger.getLogger(UserLogout.class);

	public UserLogout() throws IOException {
		super();

		// configure Logger
		// TODO Configure in config.xml by different layouts.
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_UserLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Listens to a GET Logout request from User.
	 *
	 * @param request
	 *            :Logout Request with a tokenId as parameter *
	 * @param response
	 *            Logout status.
	 *
	 */

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Declare Response Objects and PrintWriter
		response.setContentType("application/x-json");
		JSONObject respObj = new JSONObject();
		PrintWriter out = response.getWriter();

		// Error JSON Object to be sent in case of any exception caught
		JSONObject error = new JSONObject();

		logger.debug("New Logout Request [ " + DateProvider.getDateTime());

		String tokenId = request.getParameter("tokenId");
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
						JSONObject temp = new JSONObject();

						// Get User for a given Token Id
						String userMappingkey = UserLogin.tokenIDToUserMapping
								.get(tokenId);
						if (userMappingkey == null) {
							// Invalid User
							error.put("message", "User not mapped to token Id");
							error.put("code", ErrorCodes.INVALID_USER);
							logger.fatal(error);

							temp.put("error", error);
							respObj.put("response", temp);

						} else {
							session.removeAttribute("tokenId");

							String user = UserLogin.tokenIDToUserMapping
									.get(tokenId);
							UserLogin.tokenIDToUserMapping.remove(tokenId);

							// All tokens finished
							if (!UserLogin.tokenIDToUserMapping
									.containsValue(user)) {
								// Delete Mongo Instance too
								Mongo m = UserLogin.userToMongoInstanceMapping
										.get(user);
								m.close();
								UserLogin.userToMongoInstanceMapping
										.remove(user);

							}

							String status = "User Logged Out";
							JSONObject logoutStatus = new JSONObject();
							logoutStatus.put("result", status);
							respObj.put("response", logoutStatus);
							logger.info(logoutStatus + "Details [" + user + "]"
									+ DateProvider.getDateTime());

						}
					}
				}
			}
			out.write(respObj.toString());
		} catch (JSONException e) {

			try {
				error.put("message", e.getMessage());
				error.put("code", ErrorCodes.JSON_EXCEPTION);
				error.put("stackTrace", e.getStackTrace());
				logger.error(error);
				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);

				out.write(respObj.toString());

			} catch (JSONException j) {

				ServletException s = new ServletException(
						"Error forming JSON Error Response", j.getCause());
				logger.error("Exception");
				logger.error("Message:  [ " + s.getMessage() + "]");
				logger.error("StackTrace: " + s.getStackTrace() + "]");

				throw s;
			}

		}
	}
}
