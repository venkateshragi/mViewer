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

import java.util.Set;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
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
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.requestdispatchers.UserLogin;

/**
 * Defines resources for performing create/drop operations on collections
 * present inside databases in Mongo we are currently connected to. Also provide
 * resources to get list of all collections in a database present in mongo and
 * also statistics of a particular collection.
 * <p>
 * These resources map different HTTP equests made by the client to access these
 * resources to services file which performs these operations. The resources
 * also form a JSON response using the output recieved from the serives files.
 * GET and POST request resources for collections are defined here. For PUT and
 * DELETE functionality , a POST request with an action parameter taking values
 * PUT and DELETE is made.
 * 
 * @author Rachit Mittal
 * @since 4 July 2011
 * 
 */
@Path("/{dbName}/collection")
public class CollectionRequestDispatcher extends BaseRequestDispatcher {

	// TODO Configure Logger and use.
	private final static Logger logger = Logger.getLogger(CollectionRequestDispatcher.class);

	/**
	 * Default Constructor
	 */
	public CollectionRequestDispatcher() {
	}

	/**
	 * Maps GET Request to get list of collections inside databases present in
	 * mongo db to a service function that returns the list. Also forms the JSON
	 * response for this request and sent it to client. In case of any exception
	 * from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of database
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * 
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @return String of JSON Format with list of all collections.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollList(@PathParam("dbName") String dbName, @QueryParam("tokenId") String tokenId, @Context HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			logger.info("Recieved GET Request for Collection  [" + DateProvider.getDateTime() + "]");
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
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File.
			CollectionService collectionService = new CollectionServiceImpl(userMappingkey);
			// Get the result;
			Set<String> collectionNames = collectionService.getCollList(dbName);
			temp.put("result", collectionNames);
			resp.put("response", temp);
			resp.put("totalRecords", collectionNames.size());
			response=resp.toString();
			
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
			}

		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (CollectionException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (ValidationException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(), ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;

	}

	/**
	 * Maps POST Request to perform create/drop on collections inside databases
	 * present in mongo db to a service function that returns the list. Also
	 * forms the JSON response for this request and sent it to client. In case
	 * of any exception from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param capped
	 *            Specify if the collection is capped
	 * @param size
	 *            Specify the size of collection
	 * @param maxDocs
	 *            specify maximum no of documents in the collection
	 * 
	 * @param collectionName
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
	 * @return String with status of operation performed.
	 */

	@POST
	@Path("/{collectionName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postCollRequest(@PathParam("dbName") String dbName, @PathParam("collectionName") String collectionName,
			@QueryParam("capped") boolean capped, @QueryParam("size") int size, @QueryParam("max") int maxDocs, @QueryParam("action") String action,
			@QueryParam("tokenId") String tokenId, @Context HttpServletRequest request) {

		if (logger.isInfoEnabled()) {
			logger.info("Recieved POST Request for Collection  [" + DateProvider.getDateTime() + "]");
		}
		if (action == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.ACTION_PARAMETER_ABSENT, "ACTION_PARAMETER_ABSENT");
			return formErrorResponse(logger, e.getMessage(), e.getErrorCode(), null,"ERROR");
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
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File.
			CollectionService collectionService = new CollectionServiceImpl(userMappingkey);

			if (action.equals("PUT")) {
				temp.put("result", collectionService.insertCollection(dbName, collectionName, capped, size, maxDocs));

			} else if (action.equals("DELETE")) {
				temp.put("result", collectionService.deleteCollection(dbName, collectionName));
			}
			resp.put("response", temp);
			response=resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime() + "]");
			}

		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (CollectionException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (ValidationException e) {
			response = formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(), ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;

	}
}
