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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.imaginea.mongodb.requestdispatchers.UserLogin;

/**
 * Defines resources for performing create/drop operations on databases present
 * in Mongo we are currently connected to. Also provide resources to get list of
 * all databases present in mongo and also statistics of a particular database.
 * <p>
 * These resources map different HTTP equests made by the client to access these
 * resources to services file which performs these operations. The resources
 * also form a JSON response using the output recieved from the serives files.
 * GET and POST request resources for databases are defined here. For PUT and
 * DELETE functionality , a POST request with an action parameter taking values
 * PUT and DELETE is made.
 * 
 * @author Rachit Mittal
 * @since 2 July 2011
 * 
 */

@Path("/db")
public class DatabaseRequestDispatcher extends BaseRequestDispatcher {

	private final static Logger logger = Logger.getLogger(DatabaseRequestDispatcher.class);

	/**
	 * Default constructor
	 */
	public DatabaseRequestDispatcher() {
	}

	/**
	 * Maps GET Request to get list of databases present in mongo db to a
	 * service function that returns the list. Also forms the JSON response for
	 * this request and sent it to client. In case of any exception from the
	 * service files an error object if formed.
	 * 
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * 
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @return String of JSON Format with list of all Databases.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDbList(@QueryParam("tokenId") String tokenId, @Context HttpServletRequest request) {

		// TODO @Context can we write somewhere else
		// TODO how is JSONException is provided to the consumer/client, try in
		// REST client, can just write a simple json string ourself in worst
		// case

		String response = null;
		if (logger.isInfoEnabled()) {
			logger.info("Recieved a Get DB List Request [" + DateProvider.getDateTime() + "]");
		}
		try {
			response = validateTokenId(tokenId, logger, request);
			if (response != null) {
				return response;
			}
			// Get User for a given Token Id
			String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
			if (userMappingkey == null) {
				return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
			}

			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File.
			DatabaseService databaseService = new DatabaseServiceImpl(userMappingkey);
			// Get the result;
			List<String> dbNames = databaseService.getDbList();
			temp.put("result", dbNames);
			resp.put("response", temp);
			resp.put("totalRecords", dbNames.size());
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
			}
		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";

		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(), ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");

		}
		return response;

	}

	/**
	 * Maps POST Request to perform create/drop operations on databases present
	 * in mongo db to a service function that returns the list. Also forms the
	 * JSON response for this request and sent it to client. In case of any
	 * exception from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database for which to perform create/drop operation
	 *            depending on action patameter
	 * 
	 * @param action
	 *            Query Paramater with value PUT for identifying a create
	 *            database request and value DELETE for dropping a database.
	 * 
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * 
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * @return : String with status of operation performed.
	 * 
	 */
	@POST
	@Path("/{dbName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postDbRequest(@PathParam("dbName") String dbName, @QueryParam("action") String action, @QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) {

		if (logger.isInfoEnabled()) {
			logger.info("Recieved POST Request for Db with action [ " + action + "] [" + DateProvider.getDateTime() + "]");
		}
		if (action == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.ACTION_PARAMETER_ABSENT, "ACTION_PARAMETER_ABSENT");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(), null, "ERROR");
		}
		String response = null;
		try {
			response = validateTokenId(tokenId, logger, request);
			if (response != null) {
				return response;
			}
			// Get User for a given Token Id
			String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
			if (userMappingkey == null) {
				return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
			}
			// Create Instance of Service File.
			DatabaseService databaseService = new DatabaseServiceImpl(userMappingkey);
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			if (action.equals("PUT")) {
				temp.put("result", databaseService.createDb(dbName));
			} else if (action.equals("DELETE")) {
				temp.put("result", databaseService.dropDb(dbName));
			}
			resp.put("response", temp);
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
			}
		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (ValidationException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(), ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;

	}
}
