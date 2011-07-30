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
package com.imaginea.mongodb.requestdispatchers;
 
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
 * Authenticates User to Mongo Db by checking the user in <system.users>
 * collection of admin database. Also generate a token Id for this particular
 * username and stores it in session.
 * <p>
 * Also creates a map of tokenId to mappingkey which is a combination of
 * username along with mongoIP and mongoPort provided by user.Also Creates a map
 * of a mappingkey so that can use a single mongoInstance per mappingkey.
 * 
 * @author Rachit Mittal
 * @since 10 July 2011
 * 
 */

@Path("/login")
public class UserLogin extends BaseRequestDispatcher {
	private static final long serialVersionUID = 1L;

	/**
	 * Stores a mapping of tokenId to a mappingkey which is combination of
	 * userName , mongohost and mongoPort. It is used by request dispatchers to
	 * select a user name for given token Id.
	 */
	public static Map<String, String> tokenIDToUserMapping = new HashMap<String, String>();

	/**
	 * Stores a mapping of mappingkey which is combination of userName ,
	 * mongohost and mongoPort to a MongoInstance. It is used by mongo instance
	 * provider to get a mongo instance for a user.
	 */
	public static Map<String, Mongo> userToMongoInstanceMapping = new HashMap<String, Mongo>();

	private static Logger logger = Logger.getLogger(UserLogin.class);
	public UserLogin()
	{
		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * Authenticates User by verifying Mongo config details against admin
	 * database and authenticating user to that Db. A facility for guest login
	 * is also allowed when both fields username and password are empty.
	 * <p>
	 * Also generates a tokenId and store it in session so that anyone cannot
	 * use just the tokenId to contact the Database. Also stores a mongo
	 * instance to be used by service files based on token Id and mappingKey
	 * which is a combination of username , mongoHost and mongoPort.
	 * 
	 * @param request
	 *            User Authentication Request
	 * 
	 * @param username
	 *            Name of user
	 * @param password
	 *            password of user to access mongo db
	 * @param mongoHost
	 *            mongo host to connect to
	 * @param mongoPort
	 *            mongo Port to connect to
	 * 
	 * @author Rachit Mittal
	 * @since 12 Jul 2011
	 * 
	 * 
	 * 
	 * 
	 */

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String authenticateUser(@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("host") String mongoHost,
			@FormParam("port") String mongoPort,
			@Context HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			logger.info("New Connection Request [" + DateProvider.getDateTime()
					+ "]");
			logger.info("Response Recieved : UserName [" + username
					+ "] , host [" + mongoHost + "] ,port [" + mongoPort + "]");
		}
		String response = null;
		try {
			if (username == null || password == null || mongoHost == null
					|| mongoPort == null) {
				return formErrorResponse(logger, "Missing Login Fields",
						ErrorCodes.MISSING_LOGIN_FIELDS, null, "FATAL");
			}
			if (mongoHost.equals("") || mongoPort.equals("")) {
				return formErrorResponse(logger, "Missing Login Fields",
						ErrorCodes.MISSING_LOGIN_FIELDS, null, "FATAL");
			}
		
			// Try to connect to Mongo 
			Mongo m = new Mongo(mongoHost, Integer.parseInt(mongoPort));
			
			boolean loginStatus = false;
			if (username.equals("") && password.equals("")) {
				// Guest Login
				username = "guest";
				loginStatus = true;
			} else {
				// Authorize User using <admin> Db
			
				DB db = m.getDB("admin");
				loginStatus = db.authenticate(username, password.toCharArray());
			}
			if (!loginStatus) {
				return formErrorResponse(logger,
						"Invalid UserName or Password",
						ErrorCodes.INVALID_USERNAME, null, "FATAL");
			}

			// User Found
			String mappingKey = username + "_" + mongoHost + "_" + mongoPort;
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
				userToMongoInstanceMapping.put(mappingKey, mongoInstance);

			}
			// Form a JSON format token.
			String x = userToken.getTokenId();
			JSONObject token = new JSONObject();
			token.put("id", x);
			token.put("username", userToken.getUsername());
			token.put("host", userToken.getMongoHost());
			token.put("port", userToken.getMongoPort());

			if (logger.isInfoEnabled()) {
				logger.info("Token provided [" + token + ']');
			}
			// Write in response
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			temp.put("result", token);
			resp.put("response", temp);
			response = resp.toString();

		} catch (NumberFormatException e) {
			response = formErrorResponse(logger, "Invalid Port",
					ErrorCodes.ERROR_PARSING_PORT, e.getStackTrace(), "ERROR");
		} catch (IllegalArgumentException e) {
			// When port out of range
			response = formErrorResponse(logger, "Port out of range",
					ErrorCodes.PORT_OUT_OF_RANGE, e.getStackTrace(), "ERROR");
		} catch (UnknownHostException m) {
			MongoHostUnknownException e = new MongoHostUnknownException(
					"Unknown host", m);
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (JSONException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
		} catch (MongoException m) {
			MongoHostUnknownException e = new MongoHostUnknownException(
					"Unknown host", m);
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");

		}
		return response;
	}
}
