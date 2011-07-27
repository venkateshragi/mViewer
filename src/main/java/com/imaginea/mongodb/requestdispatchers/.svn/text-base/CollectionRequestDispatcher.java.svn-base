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
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;

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
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertCollectionException;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.services.servlet.UserLogin;

/**
 * Defines the Resource file for Collection Resource and map a HTTP Request to
 * the relevant collection service function. Defines GET and POST resources for
 * collections in MongoDb. PUT and DELETE Functionality achieved using an
 * <action> query parameter. GET Collection Stats Resource also present.
 *
 * @author Rachit Mittal
 *
 */

/**
 * @Path Defines the path to which Jersey servlet maps this Resource. <dbName>
 *       is the name of database inside which a collection is present and
 *       <collection> is the Resource name specific to all collections. For
 *       accessing a particular collection , an added parameter <collectionName>
 *       in URL is used.
 */
@Path("/{dbName}/collection")
public class CollectionRequestDispatcher {

	/**
	 * Define Logger for this class
	 */
	private static Logger logger = Logger
			.getLogger(CollectionRequestDispatcher.class);

	/**
	 * Constructor that configures Logger
	 *
	 * @throws IOException
	 */
	public CollectionRequestDispatcher() throws IOException {

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_CollectionOperationsLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Respond to a GET Request and map it to getCollections() Service function
	 * that gets all Collections in a <dbName>.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : A String of JSON Format with key <result> and value
	 *         List<String>Collections in a <dbName>
	 *
	 * @throws JSONException
	 *             : If Error While Writing JSONObject
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollListRequest(@PathParam("dbName") String dbName,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {

		logger.info("Recieved GET Request for Collection  ["
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
							error.put("message", "INVALID_USER");
							error.put("code", ErrorCodes.INVALID_USER);
							logger.fatal(error);
							temp.put("error", error);
							response.put("response", temp);

						} else {
							// Create Instance of Service File.
							CollectionService collectionService = new CollectionServiceImpl(
									userMappingkey);

							// Get the result;
							Set<String> collectionNames = collectionService
									.getCollections(dbName);
							temp.put("result", collectionNames);

							response.put("response", temp);
							response.put("totalRecords", collectionNames.size());
							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}
					}
				}
			}
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

		} catch (CollectionException e) {
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
	 * Maps POST Request on Collection to a Collection service depending on
	 * <action> parameter.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection for which to perform PUT,POST,DELETE
	 *            operation depending on <action> patameter
	 *
	 * @param action
	 *            : Query Paramater which decides which service to mapped to
	 *            (POST, PUT or DELETE).
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @param capped
	 *            : Specify if the collection is capped
	 * @param size
	 *            : Specify the size of collection
	 * @param maxDocs
	 *            : specify maximum no of documents in the collection
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : String with Status of performed operation.
	 *
	 * @throws JSONException
	 *             : Forming JSON Error Object Failed
	 */

	@POST
	@Path("/{collectionName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postCollRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("capped") boolean capped, @QueryParam("size") int size,
			@QueryParam("max") int maxDocs,
			@QueryParam("action") String action,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {
		logger.info("Recieved POST Request for Collection with action [ "
				+ action + "] [" + DateProvider.getDateTime() + "]");
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

							if (action.equals("PUT")) {
								temp.put("result", collectionService
										.insertCollection(dbName,
												collectionName, capped, size,
												maxDocs));

							} else if (action.equals("DELETE")) {
								temp.put("result", collectionService
										.deleteCollection(dbName,
												collectionName));
							}
							response.put("response", temp);

							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}
					}
				}
			}
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

		} catch (DeleteCollectionException e) {
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

		} catch (UndefinedCollectionException e) {
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (InsertCollectionException e) {
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
