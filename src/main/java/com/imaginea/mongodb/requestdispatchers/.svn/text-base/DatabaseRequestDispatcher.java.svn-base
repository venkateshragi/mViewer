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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DuplicateDatabaseException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDatabaseException;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.imaginea.mongodb.services.servlet.UserLogin;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;

/**
 * Defines the Resource file for Database Resource and map a HTTP Request to
 * the relevant database service function. Defines GET and POST resources for
 * Databases in MongoDb. PUT and DELETE Functionality achieved using an
 * <action> query parameter. GET database Stats Resource also present.
 *
 *
 * @author Rachit Mittal
 *
 */

/**
 * @Path Defines the path to which Jersey servlet maps this Resource. <db> is
 *       the Resource name specific to all databases. For accessing a particular
 *       database , an added parameter <dbName> in URL is used.
 *
 *
 */
@Path("/db")
public class DatabaseRequestDispatcher {

	/**
	 * Define Logger for this class
	 */
	private static Logger logger = Logger
			.getLogger(DatabaseRequestDispatcher.class);

	/**
	 * Constructor that configures Logger
	 *
	 * @throws IOException
	 */
	public DatabaseRequestDispatcher() throws IOException {

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_DatabaseOperationsLogs.txt", true);
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Maps Get Request on Database to getAllDb() Database service. <path>
	 * defined for this resource is </services/db>
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : String of JSON Format with list of all Databases.
	 *
	 * @throws JSONException
	 *             : Forming JSON Error Object Failed
	 */

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDbListRequest(@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {
		logger.info("Recieved a Get DB List Request ["
				+ DateProvider.getDateTime() + "]");

		// Contains JSON Resposne which is converted to String for sending a
		// response
		JSONObject response = new JSONObject();

		// Declare Error Object in case of error
		JSONObject error = new JSONObject();

		try {
			if (tokenId == null) {
				InvalidHTTPRequestException e = new InvalidHTTPRequestException(
						ErrorCodes.TOKEN_ID_ABSENT, "Token Id not provided");

				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				logger.fatal(error);

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				response.put("response", temp);

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
					response.put("response", temp);
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
						response.put("response", temp);

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
							response.put("response", temp);

						} else {// Create Instance of Service File.
							DatabaseService databaseService = new DatabaseServiceImpl(
									userMappingkey);
							// Get the result;
							List<String> dbNames = databaseService.getAllDb();
							temp.put("result", dbNames);

							response.put("response", temp);
							response.put("totalRecords", dbNames.size());
							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}

					}
				}
			}

		} catch (JSONException e) {

			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", ErrorCodes.JSON_EXCEPTION);
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (DatabaseException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (Exception e) {
			// For any other exception like if ConfigMongoProvider used then
			// FileNotFoundException
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", "ANY_OTHER_EXCEPTION");
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		}
		return response.toString();

	}

	/**
	 * Maps POST Request on Database to a Database service depending on <action>
	 * parameter. <path> defined for this resource is </rest/db/{dbName}>
	 *
	 * @param dbName
	 *            : Name of Database for which to perform PUT,POST,DELETE
	 *            operation depending on <action> patameter
	 *
	 * @param action
	 *            : Query Paramater which decides which service to mapped to
	 *            (POST, PUT or DELETE).
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 * @return : String with status of operation performed.
	 *
	 */
	@POST
	@Path("/{dbName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postDbRequest(@PathParam("dbName") String dbName,
			@QueryParam("action") String action,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {

		logger.info("Recieved POST Request for Db with action [ " + action
				+ "] [" + DateProvider.getDateTime() + "]");
		// Contains JSON Resposne which is converted to String for sending a
		// response
		JSONObject response = new JSONObject();

		// Declare Error Object in case of error
		JSONObject error = new JSONObject();
		try {
			if (action == null) {

				InvalidHTTPRequestException e = new InvalidHTTPRequestException(
						ErrorCodes.ACTION_PARAMETER_ABSENT,
						"ACTION_PARAMETER_ABSENT");
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				logger.error(error);

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				response.put("response", temp);

			} else {
				if (tokenId == null) {
					InvalidHTTPRequestException e = new InvalidHTTPRequestException(
							ErrorCodes.TOKEN_ID_ABSENT, "Token Id not provided");

					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());
					logger.fatal(error);

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					response.put("response", temp);

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
						response.put("response", temp);
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
							response.put("response", temp);

						} else {
							JSONObject temp = new JSONObject();
							// Get User for a given Token Id
							String userMappingkey = UserLogin.tokenIDToUserMapping
									.get(tokenId);
							if (userMappingkey == null) {
								// Invalid User
								error.put("message",
										"User not mapped to token Id");
								error.put("code", ErrorCodes.INVALID_USER);
								logger.error(error);
								temp.put("error", error);
								response.put("response", temp);

							} else {
								// Create Instance of Service File.
								DatabaseService databaseService = new DatabaseServiceImpl(
										userMappingkey);

								if (action.equals("PUT")) {

									temp.put("result",
											databaseService.createDb(dbName));

								} else if (action.equals("DELETE")) {
									temp.put("result",
											databaseService.dropDb(dbName));

								}

								response.put("response", temp);
							}
						}
					}
				}
			}
		} catch (DuplicateDatabaseException e) {

			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (EmptyDatabaseNameException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (UndefinedDatabaseException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (InsertDatabaseException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (DatabaseException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (JSONException e) {
			error.put("message", e.getMessage());
			error.put("code", ErrorCodes.JSON_EXCEPTION);
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (Exception e) {
			// For any other exception like if ConfigMongoProvider used then
			// FileNotFoundException
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", "ANY_OTHER_EXCEPTION");
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		}

		return response.toString();

	}
}
