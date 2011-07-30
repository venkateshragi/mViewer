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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.services.DocumentService;
import com.imaginea.mongodb.services.DocumentServiceImpl;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

/**
 * Defines resources for performing create/delete/update operations on documents
 * present inside collections in databases in Mongo we are currently connected
 * to. Also provide resources to get list of all documents present inside a
 * collection in a database in mongo.
 * <p>
 * These resources map different HTTP equests made by the client to access these
 * resources to services file which performs these operations. The resources
 * also form a JSON response using the output recieved from the serives files.
 * GET and POST request resources for documents are defined here. For PUT and
 * DELETE functionality , a POST request with an action parameter taking values
 * PUT and DELETE is made.
 * 
 * @author Rachit Mittal
 * @since 6 July 2011
 * 
 */
@Path("/{dbName}/{collectionName}/document")
public class DocumentRequestDispatcher extends BaseRequestDispatcher {

	private final static Logger logger = Logger
			.getLogger(DocumentRequestDispatcher.class);

	/**
	 * Default Constructor
	 */
	public DocumentRequestDispatcher() {

	}

	/**
	 * Maps GET Request to get list of documents inside a collection inside a
	 * database present in mongo db to a service function that returns the list.
	 * Also forms the JSON response for this request and sent it to client. In
	 * case of any exception from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * 
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @return A String of JSON format with list of All Documents in a
	 *         collection.
	 * 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getQueriedDocsList(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("query") String query,
			@QueryParam("tokenId") String tokenId,
			@QueryParam("fields") String fields,
			@QueryParam("limit") String limit, @QueryParam("skip") String skip,
			@Context HttpServletRequest request) throws JSONException {

		if (logger.isInfoEnabled()) {
			logger.info("Recieved GET Request for Document  ["
					+ DateProvider.getDateTime() + "]");
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
				return formErrorResponse(logger, "User not mapped to token Id",
						ErrorCodes.INVALID_USER, null, "FATAL");
			}
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File
			DocumentService documentService = new DocumentServiceImpl(
					userMappingkey);
			// Get query
			DBObject queryObj = (DBObject) JSON.parse(query);
			// Get all fields to be returned
			if (fields == null) {
				fields = "";
			}
			StringTokenizer strtok = new StringTokenizer(fields, ",");
			DBObject keyObj = new BasicDBObject();
			while (strtok.hasMoreElements()) {
				keyObj.put(strtok.nextToken(), 1);
			}
			int docsLimit = Integer.parseInt(limit);
			int docsSkip = Integer.parseInt(skip);

			ArrayList<DBObject> documentList = documentService
					.getQueriedDocsList(dbName, collectionName, queryObj,
							keyObj, docsLimit, docsSkip);
			temp.put("result", documentList);
			resp.put("response", temp);
			resp.put("totalRecords", documentList.size());
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime()
						+ "]");
			}

		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\","
					+ "\"message\": \"Error while forming JSON Object\"}";

		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (CollectionException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (DocumentException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (ValidationException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;
	}

	/**
	 * Maps GET Request to get all keys of document inside a collection inside a
	 * database present in mongo db to a service function that returns the list.
	 * Also forms the JSON response for this request and sent it to client. In
	 * case of any exception from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @return A String of JSON format with all keys in a collection.
	 * 
	 */

	@GET
	@Path("/keys")
	@Produces(MediaType.APPLICATION_JSON)
	public String getKeysRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			logger.info("Recieved GET Request for getting keys in a collection  ["
					+ DateProvider.getDateTime() + "]");
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
				return formErrorResponse(logger, "User not mapped to token Id",
						ErrorCodes.INVALID_USER, null, "FATAL");
			}
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File.
			Mongo mongoInstance = UserLogin.userToMongoInstanceMapping
					.get(userMappingkey);

			DBCursor cursor = mongoInstance.getDB(dbName)
					.getCollection(collectionName).find();

			DBObject doc = new BasicDBObject();
			Set<String> completeSet = new HashSet<String>();
			while (cursor.hasNext()) {
				doc = cursor.next();
				getNestedKeys(doc, completeSet, "");
			}
			completeSet.remove("_id");
			temp.put("result", completeSet);
			resp.put("response", temp);
			resp.put("totalRecords", completeSet.size());
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime()
						+ "]");
			}

		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\","
					+ "\"message\": \"Error while forming JSON Object\"}";
		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;
	}

	/**
	 * Gets the keys within a nested document and adds it to the complete Set.
	 * 
	 * @param doc
	 *            document
	 * @param completeSet
	 *            collection of all keys
	 * @param prefix
	 *            For nested docs. For the key <foo.bar.baz>, the prefix would
	 *            be <foo.bar>
	 */
	private void getNestedKeys(DBObject doc, Set<String> completeSet,
			String prefix) {
		Set<String> allKeys = doc.keySet();
		Iterator<String> it = allKeys.iterator();
		while (it.hasNext()) {
			String temp = it.next();
			completeSet.add(prefix + temp);
			if (doc.get(temp) instanceof BasicDBObject) {
				getNestedKeys((DBObject) doc.get(temp), completeSet, prefix
						+ temp + ".");
			}
		}
	}

	/**
	 * Maps POST Request to perform operations like update/delete/insert
	 * document inside a collection inside a database present in mongo db to a
	 * service function that returns the list. Also forms the JSON response for
	 * this request and sent it to client. In case of any exception from the
	 * service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database
	 * @param collectionName
	 *            Name of Collection
	 * @param documentData
	 *            Contains the document to be inserted
	 * 
	 * @param _id
	 *            Object id of document to delete or update
	 * 
	 * @param keys
	 *            new Document values in case of update
	 * 
	 * @param action
	 *            Query Paramater with value PUT for identifying a create
	 *            database request and value DELETE for dropping a database.
	 * 
	 * @param tokenId
	 *            a token Id given to every user at Login.
	 * 
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * 
	 * @return String with Status of operation performed.
	 * 
	 */

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String postDocsRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@DefaultValue("POST") @QueryParam("action") String action,
			@FormParam("document") String documentData,
			@FormParam("_id") String _id, @FormParam("keys") String keys,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) {

		if (logger.isInfoEnabled()) {
			logger.info("Recieved POST Request for Document  ["
					+ DateProvider.getDateTime() + "]");
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
				return formErrorResponse(logger, "User not mapped to token Id",
						ErrorCodes.INVALID_USER, null, "FATAL");
			}
			JSONObject temp = new JSONObject();
			JSONObject resp = new JSONObject();
			// Create Instance of Service File
			DocumentService documentService = new DocumentServiceImpl(
					userMappingkey);

			if (action.equals("PUT")) {

				if (documentData == null) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");
				} else if (documentData.equals("")) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");

				} else {
					DBObject document = (DBObject) JSON.parse(documentData);
					temp.put("result", documentService.insertDocument(dbName,
							collectionName, document));
				}
			} else if (action.equals("DELETE")) {

				if (_id == null) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");

				} else if (_id.equals("")) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");

				} else {
					ObjectId id = new ObjectId(_id);
					temp.put("result", documentService.deleteDocument(dbName,
							collectionName, id));
				}
			} else if (action.equals("POST")) {
				if (_id == null || keys == null) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");

				} else if (_id.equals("") || keys.equals("")) {
					UndefinedDocumentException e = new UndefinedDocumentException(
							"Document Data Missing in Request Body");
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
							null, "ERROR");

				} else {
					// Id of Document to update

					ObjectId id = new ObjectId(_id);
					// New Document Keys
					DBObject newDoc = (DBObject) JSON.parse(keys);
					temp.put("result", documentService.updateDocument(dbName,
							collectionName, id, newDoc));
				}
			}
			resp.put("response", temp);
			response = resp.toString();
			if (logger.isInfoEnabled()) {
				logger.info("Request Completed [" + DateProvider.getDateTime()
						+ "]");
			}
		} catch (IllegalArgumentException e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.INVALID_OBJECT_ID, e.getStackTrace(), "ERROR");
		} catch (JSONException e) {
			response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\","
					+ "\"message\": \"Error while forming JSON Object\"}";

		} catch (DatabaseException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (CollectionException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (DocumentException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (ValidationException e) {
			response = formErrorResponse(logger, e.getMessage(),
					e.getErrorCode(), e.getStackTrace(), "ERROR");

		} catch (Exception e) {
			response = formErrorResponse(logger, e.getMessage(),
					ErrorCodes.ANY_OTHER_EXCEPTION, e.getStackTrace(), "ERROR");
		}
		return response;
	}
}
