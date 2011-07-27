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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.imaginea.mongodb.services.servlet.UserLogin;
import com.mongodb.CommandResult;
import com.mongodb.Mongo;

/**
 * Defines the Resource file for getting Statistics of Mongo Db. The Statistics can be subcategorised into
 * server statistics or statistics of a particular Db or statistics of a collection.
 *
 * This Resource file will map HTTP request to the relevant method in the service files.
 * @author Rachit Mittal
 *
 */

/**
 * @Path Defines the path to which Jersey servlet maps this Resource. *
 *
 */
@Path("/stats")
public class StatisticsRequestDispatcher {

	/**
	 * Define Logger for this class
	 */
	private static Logger logger = Logger
			.getLogger(StatisticsRequestDispatcher.class);

	/**
	 * Constructor that configures Logger
	 *
	 * @throws IOException
	 */
	public StatisticsRequestDispatcher() throws IOException {

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_StatisticsOperationsLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Serves Get Request with path {/services/Stats} to get Server Stats.
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : String of JSON Format with Db Stats.
	 *
	 * @throws JSONException
	 *             : Forming JSON Error Object Failed
	 * @throws JSONErrorPayload
	 *             : Throws the JSON Error Object to the Front end
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getServerStatsRequest(@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {
		logger.info("Recieved GET Server Stats Request ["
				+ DateProvider.getDateTime() + "]");
		// Contains JSON Resposne which is converted to String for sending a
		// response
		JSONObject response = new JSONObject();

		// Declare Error Object in case of error
		JSONObject error = new JSONObject();

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
					// Get User for a given Token Id
					String userMappingkey = UserLogin.tokenIDToUserMapping
							.get(tokenId);

					Mongo mongoInstance = UserLogin.userToMongoInstanceMapping
							.get(userMappingkey);

					// Get Server Stats
					CommandResult cd = mongoInstance.getDB("admin").command(
							"serverStatus");

					JSONObject stats = new JSONObject();
					stats.put("result", cd);

					response.put("response", stats);

				}
			}
		}

		return response.toString();
	}

	/**
	 * Maps Get Request on Database to getDbStats() Database service. <path>
	 * defined for this resource is </services/db/{dbName}>
	 *
	 * @param dbName
	 *            : Name of Database for which to get DbStats.
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @return : String of JSON Format with Db Stats.
	 *
	 * @throws JSONException
	 *             : Forming JSON Error Object Failed
	 */
	@GET
	@Path("/db/{dbName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDbStatsRequest(@PathParam("dbName") String dbName,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {
		logger.info("Recieved a Get DB Stats Request ["
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

						} else {
							// Create Instance of Service File.
							DatabaseService databaseService = new DatabaseServiceImpl(
									userMappingkey);
							// Get the result;
							JSONArray dbStats = databaseService
									.getDbStats(dbName);
							temp.put("result", dbStats);
							response.put("response", temp);
							response.put("totalRecords", dbStats.length());
							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}
					}
				}
			}
		} catch (UndefinedDatabaseException e) {
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
		} catch (DatabaseException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (JSONException e) {

			// Form a JSON Error Object
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
			error.put("code", ErrorCodes.ANY_OTHER_EXCEPTION);
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		}

		return response.toString();
	}

	/**
	 * Respond to a GET Request and map it to getCollectionStats() Service
	 * function that Stats for a <collectionName>.
	 *
	 * @param dbName
	 *            : Name of Database
	 *
	 * @param collectionName
	 *            : Name of Collection
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : A String of JSON Format with key <result> and value Collection
	 *         Stats.
	 * @throws JSONException
	 *             : If Error While Writing JSONObject
	 * @throws EmptyCollectionNameException
	 *             ,EmptyDatabaseNameException
	 * @throws UndefinedCollectionException
	 *             , UndefinedDatabaseException
	 * @throws JSONErrorPayload
	 *             ,JSONException
	 */
	@GET
	@Path("/db/{dbName}/collection/{collectionName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollStatsRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {
		logger.info("Recieved a Get Coll Stats Request ["
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

						} else {
							// Create Instance of Service File.
							CollectionService collectionService = new CollectionServiceImpl(
									userMappingkey);
							// Get the result;
							JSONArray collectionStats = collectionService
									.getCollectionStats(dbName, collectionName);
							temp.put("result", collectionStats);

							response.put("response", temp);
							response.put("totalRecords",
									collectionStats.length());

							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}
					}
				}
			}

		} catch (UndefinedDatabaseException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (UndefinedCollectionException e) {
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
		} catch (EmptyCollectionNameException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (CollectionException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (JSONException e) {

			// Form a JSON Error Object
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
			error.put("code", ErrorCodes.ANY_OTHER_EXCEPTION);
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		}

		return response.toString();
	}

}
