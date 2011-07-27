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
import com.mongodb.MongoException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.UserTokenInformation;
import com.imaginea.mongodb.common.UserTokenInformationProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Authenticates User to Mongo Db by checking the user in <system.users> collection of
 * <admin> database. Also generate a token Id for this particular username and
 * stores it in session..
 *
 * Also creates a map of <tokenId> to <mappingkey>  which is a combination of username along with
 * mongoIP and mongoPort provided by user.
 *
 * Also Creates a map of a <mappingkey> so that can use a single mongoInstance
 * per <mappingkey>.
 *
 *
 * @author Rachit Mittal
 *
 */

/**
 * Path mapping for this servlet as mentioned in web.xml is {/login}
 */
public class UserLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Stores a map of tokenId to a <mappingkey> which is combination of
	 * <UserName_MongoIP_MongoPort>. Used by Request Dispatchers to select a
	 * user name for given token Id.
	 */
	public static Map<String, String> tokenIDToUserMapping = new HashMap<String, String>();

	/**
	 * Stores a map of <mappingkey> which is combination of
	 * <UserName_MongoIP_MongoPort> to a MongoInstance. Used by
	 * SessionMongoInstanceProvider to get a mongoInstance for a username.
	 */
	public static Map<String, Mongo> userToMongoInstanceMapping = new HashMap<String, Mongo>();

	private static Logger logger = Logger.getLogger(UserLogin.class);

	/**
	 * Constructor for Servlet and also configures Logger.
	 *
	 * @throws IOException
	 *             If Log file cannot be written
	 */
	public UserLogin() throws IOException {
		super();
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_Userlogs.txt", true);
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Authenticates User by verifying MongoConfig Details and authenticating
	 * user to that Db. Also generates a tokenId and store it in session so that
	 * anyone cannot use just the tokenId to contatc the Database. Also stores a
	 * MongoInstance to be used by service files based on token Id and
	 * <mappingKey> which is a combination of username , mongoHost and
	 * mongoPort.
	 *
	 * @param request
	 *            : User Authentication Request
	 *
	 * @param response
	 *            : Token Id.
	 *
	 * @throws ServletException
	 *             : Thrown When Writing to Error Object is failed as Servlet
	 *             dont allow JSONException to throw
	 *
	 * @throws IOException
	 *             : Cannot get PrintWriter Object
	 *
	 */

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException

	{
		logger.info("New Connection Request [" + DateProvider.getDateTime()
				+ "]");
		// Get Data from request

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String mongoHost = request.getParameter("host");
		String mongoPort = request.getParameter("port");

		logger.info("Response Recieved : UserName [" + username + "] , host ["
				+ mongoHost + "] ,port [" + mongoPort + "]");

		// Declare Response Objects and PrintWriter
		response.setContentType("application/x-json");
		JSONObject respObj = new JSONObject();
		PrintWriter out = response.getWriter();

		// Error JSON Object to be sent in case of any exception caught
		JSONObject error = new JSONObject();
		try {

			if (username == null || password == null || mongoHost == null
					|| mongoPort == null) {
				// Missing Login Fields
				error.put("message", "Missing Login Fields");
				error.put("code", ErrorCodes.MISSING_LOGIN_FIELDS);
				// Message of LEVEL ERROR put in logger.
				logger.error(error);

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);
				out.write(respObj.toString());

			} else if (username.equals("") || password.equals("")
					|| mongoHost.equals("") || mongoPort.equals("")) {
				// Missing Login Fields
				error.put("message", "Missing Login Fields");
				error.put("code", ErrorCodes.MISSING_LOGIN_FIELDS);

				// Message of LEVEL ERROR put in logger.
				logger.error(error);

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);
				out.write(respObj.toString());

			}

			else {
				// Authorize User using <admin> Db

				Mongo m = new Mongo(mongoHost, Integer.parseInt(mongoPort));

				DB db = m.getDB("admin");
				boolean loginStatus = db.authenticate(username,
						password.toCharArray());

				if (loginStatus) {

					// User Found
					String mappingKey = username + "_" + mongoHost + "_"
							+ mongoPort;
					UserTokenInformation userToken = new UserTokenInformationProvider(
							mongoHost, Integer.parseInt(mongoPort), username);
					// Genrate Token Id
					String tokenId = userToken.generateTokenId();

					// Sets tokenId for this user in session
					HttpSession session = request.getSession();
					session.setAttribute("tokenId", tokenId);

					// Store ID in the Map against <mappingkey>
					tokenIDToUserMapping.put(tokenId, mappingKey);

					// Store a MongoInstance
					if (!userToMongoInstanceMapping.containsKey(mappingKey)) {
						Mongo mongoInstance = new Mongo(mongoHost,
								Integer.parseInt(mongoPort));
						userToMongoInstanceMapping.put(mappingKey,
								mongoInstance);

					}
					// Form a JSON format token.
					String x = userToken.getTokenId();
					JSONObject token = new JSONObject();
					token.put("id", x);
					token.put("username", userToken.getUsername());
					token.put("host", userToken.getMongoHost());
					token.put("port", userToken.getMongoPort());
					logger.info("Token provided [" + token + ']');
					// Write in response
					JSONObject temp = new JSONObject();
					temp.put("result", token);
					respObj.put("response", temp);
				}

				else {
					// Invalid User

					error.put("message", "Invalid UserName or Password");
					error.put("code", ErrorCodes.INVALID_USERNAME);

					// Message of LEVEL ERROR put in logger.
					logger.error(error);

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					respObj.put("response", temp);

				}
				out.write(respObj.toString());
				out.close();
			}

		} catch (NumberFormatException e) {
			try {
				error.put("message", "Invalid Port");
				error.put("code", "ERROR_PARSING_PORT");
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				// Message of LEVEL ERROR put in logger.
				logger.error(error);

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
		} catch (IllegalArgumentException e) {
			try {
				// When port out of range
				error.put("message", "Port put of range");
				error.put("code", ErrorCodes.PORT_OUT_OF_RANGE);
				error.put("stackTrace", e.getStackTrace());

				// Message of LEVEL ERROR put in logger.
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
		} catch (UnknownHostException m) {

			MongoHostUnknownException e = new MongoHostUnknownException(
					"Unknown host", m);

			try {
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				error.put("stackTrace", e.getStackTrace());
				// Log error
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

		} catch (JSONException e) {


			try {
				error.put("message", e.getMessage());
				error.put("code", ErrorCodes.JSON_EXCEPTION);
				error.put("stackTrace", e.getStackTrace());
				// Log error
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

		} catch (MongoException m) {

			MongoHostUnknownException e = new MongoHostUnknownException(
					"Unknown host", m);

			try {
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				error.put("stackTrace", e.getStackTrace());
				// Log error
				logger.error(error);
				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);

				out.write(respObj.toString());

			} catch (JSONException j) {

				ServletException s = new ServletException(
						"ERROR_FORMING_JSON_ERROR_OBJECT", j.getCause());
				logger.error("Exception");
				logger.error("Message:  [ " + s.getMessage() + "]");
				logger.error("StackTrace: " + s.getStackTrace() + "]");

				throw s;
			}
		} catch (Exception e) {

			try {
				error.put("message", e.getMessage());
				error.put("code", e.getClass());
				error.put("stackTrace", e.getStackTrace());

				// Log error
				logger.error(error);
				JSONObject temp = new JSONObject();
				temp.put("error", error);
				respObj.put("response", temp);

				out.write(respObj.toString());

			} catch (JSONException j) {
				ServletException s = new ServletException(
						"ERROR_FORMING_JSON_ERROR_OBJECT", j.getCause());
				logger.error("Exception");
				logger.error("Message:  [ " + s.getMessage() + "]");
				logger.error("StackTrace: " + s.getStackTrace() + "]");

				throw s;
			}
		}
	}
}
