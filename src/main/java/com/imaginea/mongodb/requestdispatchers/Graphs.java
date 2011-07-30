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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Return values of queries,updates,inserts and deletes being performed on Mongo
 * Db per second.
 * 
 * @author Rachit Mittal
 * @since 16 July 2011
 */
@Path("/graphs")
public class Graphs extends BaseRequestDispatcher {
	private static final long serialVersionUID = 1L;

	/**
	 * Number of records to be shown on graph
	 */
	public int maxLen = 20;

	/**
	 * Keep the record of last values. These Maps are destroyed as user logs out
	 * from the application.
	 */

	public static Map<String, Integer> userToQueriesMap = new HashMap<String, Integer>();
	public static Map<String, Integer> userToInsertsMap = new HashMap<String, Integer>();
	public static Map<String, Integer> userToUpdatesMap = new HashMap<String, Integer>();
	public static Map<String, Integer> userToDeletesMap = new HashMap<String, Integer>();
	public static Map<String, JSONArray> userTOResponseMap = new HashMap<String, JSONArray>();
	public static Map<String, Integer> userToPollingIntervalMap = new HashMap<String, Integer>();
	public static Map<String, Integer> userToTimeStampMap = new HashMap<String, Integer>();
	private static Logger logger = Logger.getLogger(Graphs.class);

	/**
	 * 
	 * Gets Initialise Request for a Graph ans sets values for queries,inserts
	 * and updates performed for a mongo Server for a particular user.
	 * 
	 * @param interval
	 *            Polling interval after which each query request is made ( in
	 *            seconds)
	 * 
	 * @param tokenId
	 *            token Id of user
	 * @param request
	 *            Graph initialization request by user.
	 * @return : Status of Initialization
	 */
	@GET
	@Path("/initiate")
	public String processInitiate(
			@QueryParam("pollingTime") String interval,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) {

		if (logger.isInfoEnabled()) {
			logger.info("New Graphs Initiate Request [ "
					+ DateProvider.getDateTime());
		}

		String response = null;
		try {
			response = validateTokenId(tokenId, logger, request);
			if (response != null) {
				return response;
			}
			// Get user for this tokenId
			String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
			if (userMappingkey == null) {
				return formErrorResponse(logger, "User not mapped to token Id",
						ErrorCodes.INVALID_USER, null, "FATAL");
			}
			Mongo m = UserLogin.userToMongoInstanceMapping.get(userMappingkey);
			CommandResult cr = m.getDB("admin").command("serverStatus");
			BasicDBObject obj = (BasicDBObject) cr.get("opcounters");
			Integer queries = (Integer) obj.get("query");
			Integer inserts = (Integer) obj.get("insert");
			Integer updates = (Integer) obj.get("update");
			Integer deletes = (Integer) obj.get("delete");

			userToQueriesMap.put(userMappingkey, queries);
			userToInsertsMap.put(userMappingkey, inserts);
			userToUpdatesMap.put(userMappingkey, updates);
			userToDeletesMap.put(userMappingkey, deletes);

			userToPollingIntervalMap.put(userMappingkey,
					Integer.parseInt(interval));
			userTOResponseMap.put(userMappingkey, new JSONArray());
			userToTimeStampMap.put(userMappingkey, 0);

			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			temp.put("result", "Initiated");
			resp.put("response", temp);
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Initiated [ " + DateProvider.getDateTime());
			}

		} catch (NumberFormatException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ERROR_PARSING_POLLING_INTERVAL,
					e.getStackTrace(), "ERROR");
		} catch (JSONException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
		} catch (MongoException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ERROR_INITIATING_GRAPH, e.getStackTrace(),
					"ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");

		}
		return response;
	}

	/**
	 * Process query request made after polling interval time set at
	 * initialization time
	 * 
	 * @param tokenId
	 *            token Id of user
	 * @param request
	 *            Graph initialization request by user.
	 * @return Server stats of opcounters key
	 */
	@GET
	@Path("/query")
	public String processQuery(@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			logger.info("New Graphs Query Request [ "
					+ DateProvider.getDateTime());
		}

		String response = null;
		try {
			response = validateTokenId(tokenId, logger, request);
			if (response != null) {
				return response;
			}
			// Get user for this tokenId
			String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
			if (userMappingkey == null) {
				return formErrorResponse(logger, "User not mapped to token Id",
						ErrorCodes.INVALID_USER, null, "FATAL");
			}
			Mongo m = UserLogin.userToMongoInstanceMapping.get(userMappingkey);
			CommandResult cr = m.getDB("admin").command("serverStatus");
			BasicDBObject obj = (BasicDBObject) cr.get("opcounters");

			JSONObject resp = new JSONObject();
			JSONObject temp = new JSONObject();

			Integer timeStamp = userToTimeStampMap.get(userMappingkey)
					+ userToPollingIntervalMap.get(userMappingkey);
			userToTimeStampMap.put(userMappingkey, timeStamp);
			temp.put("TimeStamp", timeStamp);

			Integer oldQueries = userToQueriesMap.get(userMappingkey);
			Integer newQueries = (Integer) obj.get("query");
			userToQueriesMap.put(userMappingkey, newQueries);
			temp.put("QueryValue", newQueries - oldQueries);

			Integer oldInserts = userToInsertsMap.get(userMappingkey);
			Integer newInserts = (Integer) obj.get("insert");
			userToInsertsMap.put(userMappingkey, newInserts);
			temp.put("InsertValue", newInserts - oldInserts);

			Integer oldUpdates = userToUpdatesMap.get(userMappingkey);
			Integer newUpdates = (Integer) obj.get("update");
			userToUpdatesMap.put(userMappingkey, newUpdates);
			temp.put("UpdateValue", newUpdates - oldUpdates);

			Integer oldDeletes = userToDeletesMap.get(userMappingkey);
			Integer newDeletes = (Integer) obj.get("delete");
			userToDeletesMap.put(userMappingkey, newDeletes);
			temp.put("DeleteValue", newDeletes - oldDeletes);

			JSONArray array = userTOResponseMap.get(userMappingkey);
			// Shift the values by one after 20 records reached
			if (array.length() == maxLen) {
				JSONArray tempArray = new JSONArray();
				for (int i = 1; i < maxLen; i++) {
					tempArray.put(array.get(i));
				}
				array = tempArray;
			}
			array.put(temp);
			userTOResponseMap.put(userMappingkey, array);
			resp.put("result", array);
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Query Send [ " + DateProvider.getDateTime());
			}
		} catch (JSONException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
		} catch (MongoException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ERROR_INITIATING_GRAPH, e.getStackTrace(),
					"ERROR");
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");

		}
		return response;
	}

}
