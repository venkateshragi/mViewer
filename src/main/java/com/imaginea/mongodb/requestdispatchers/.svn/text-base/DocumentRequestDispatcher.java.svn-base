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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.DeleteDocumentException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDocumentDataException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDocumentException;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.exceptions.UpdateDocumentException;
import com.imaginea.mongodb.services.DocumentService;
import com.imaginea.mongodb.services.DocumentServiceImpl;
import com.imaginea.mongodb.services.servlet.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

/**
 * Defines the Resource file for Document Resource and map a HTTP Request to
 * the relevant Document service function. Defines GET and POST resources for
 * documents in MongoDb. PUT and DELETE Functionality achieved using an
 * <action> query parameter.
 *
 * @author Rachit Mittal
 *
 */

/**
 * @Path Defines the path to which Jersey servlet maps this Resource. <dbName>
 *       is the name of database inside which a collection is present and
 *       <collectionName> is the name of collection inside which a document is
 *       present and <document> is the Resource name specific to all documents.
 *       For accessing a particular document , an added parameter <documentName>
 *       in URL is used.
 */
@Path("/{dbName}/{collectionName}/document")
public class DocumentRequestDispatcher {
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
	public DocumentRequestDispatcher() throws IOException {

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_DocumentServicesLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	/**
	 * Respond to a GET Request and map it to getDocuments() Service function
	 * that gets all Documents in a <collectionName> in a <dbName>.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return : A String of JSON Format with List of All Documents in a
	 *         collection.
	 *
	 * @throws JSONException
	 *             : When Exception while writing Error Object.
	 * @throws JSONErrorPayload
	 *             : Sending the Exception to Front end
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDocsRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("query") String query,
			@QueryParam("tokenId") String tokenId,
			@QueryParam("fields") String fields,
			@QueryParam("limit") String limit, @QueryParam("skip") String skip,
			@Context HttpServletRequest request) throws JSONException {

		logger.info("Recieved GET Request for Document  ["
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
							DocumentService documentService = new DocumentServiceImpl(
									userMappingkey);
							// Get the result;
							// Get query
							DBObject queryObj = (DBObject) JSON.parse(query);

							// Get all fields to be returned
							if (fields == null) {
								fields = "";
							}
							StringTokenizer strtok = new StringTokenizer(
									fields, ",");

							DBObject keyObj = new BasicDBObject();

							while (strtok.hasMoreElements()) {
								keyObj.put(strtok.nextToken(), 1);
							}

							int docsLimit = Integer.parseInt(limit);
							int docsSkip = Integer.parseInt(skip);

							ArrayList<DBObject> documentList = documentService
									.getDocuments(dbName, collectionName,
											queryObj, keyObj, docsLimit,
											docsSkip);
							temp.put("result", documentList);
							response.put("response", temp);
							response.put("totalRecords", documentList.size());
							logger.info("Request Completed ["
									+ DateProvider.getDateTime() + "]");
						}
					}
				}
			}

		} catch (EmptyDatabaseNameException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (EmptyCollectionNameException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (DocumentException e) {
			// Form a JSON Error Object
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
	 * Respond to a GET Request and return all document keys in a
	 * <collectionName> inside a <dbName>.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 * @return :
	 *
	 * @throws JSONException
	 *             : When Exception while writing Error Object.
	 * @throws JSONErrorPayload
	 *             : Sending the Exception to Front end
	 */

	@GET
	@Path("/keys")
	@Produces(MediaType.APPLICATION_JSON)
	public String getKeysRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {

		logger.info("Recieved GET Request for Document keys ["
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
							logger.error(error);
							temp.put("error", error);
							response.put("response", temp);

						} else {
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
							response.put("response", temp);
							response.put("totalRecords", completeSet.size());
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
	 * Gets the keys within a nested <doc> and adds it to the <completeSet>.
	 *
	 * @param doc
	 *            : document
	 * @param completeSet
	 *            : collection of all keys
	 * @param prefix
	 *            : For nested docs. For the key <foo.bar.baz>, the prefix would
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
	 *
	 * Maps POST Request on Documents to a Document service depending on
	 * <action> parameter.
	 *
	 * @param dbName
	 *            : Name of Database
	 * @param collectionName
	 *            : Name of Collection
	 * @param documentData
	 *            : Contains the document to be inserted
	 *
	 * @param _id
	 *            : Object id of document to delete or update
	 *
	 * @param newDoc
	 *            : new Document values in case of update
	 *
	 * @param action
	 *            : Query Paramater which decides which service to mapped to
	 *            (POST, PUT or DELETE).
	 *
	 * @param tokenId
	 *            : a token Id given to every user at Login.
	 *
	 * @param request
	 *            : Get the HTTP request context to extract session parameters
	 *
	 * @return : String with Status of operation performed.
	 * @throws JSONException
	 *             : When Exception while writing Error Object.
	 */

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String postDocsRequest(@PathParam("dbName") String dbName,
			@PathParam("collectionName") String collectionName,
			@DefaultValue("POST") @QueryParam("action") String action,
			@FormParam("document") String documentData,
			@FormParam("_id") String _id, @FormParam("keys") String newDoc,
			@QueryParam("tokenId") String tokenId,
			@Context HttpServletRequest request) throws JSONException {

		logger.info("Recieved Post Request for Document keys ["
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
							logger.error(error);
							temp.put("error", error);
							response.put("response", temp);

						} else {
							// Create Instance of Service File.
							DocumentService documentService = new DocumentServiceImpl(
									userMappingkey);

							if (action.equals("PUT")) {

								if (documentData == null) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);
									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else if (documentData.equals("")) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);
									;
									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else {
									DBObject document = (DBObject) JSON
											.parse(documentData);
									response.put("doc", document);
									temp.put("result", documentService
											.insertDocument(dbName,
													collectionName, document));
								}
							} else if (action.equals("DELETE")) {

								if (_id == null) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);
									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else if (_id.equals("")) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);

									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else {
									// Id of Document to delete
									ObjectId id = new ObjectId(_id);
									temp.put("result", documentService
											.deleteDocument(dbName,
													collectionName, id));
								}
							} else if (action.equals("POST")) {
								if (_id == null || newDoc == null) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);
									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else if (_id.equals("") || newDoc.equals("")) {
									UndefinedDocumentException e = new UndefinedDocumentException(
											"Document Data Missing in Request Body");
									error.put("message", e.getMessage());
									error.put("code", e.getErrorCode());
									logger.error(error);

									JSONObject temp1 = new JSONObject();
									temp1.put("error", error);
									response.put("response", temp);
								} else {

									// Id of Document to update
									
									ObjectId id = new ObjectId(_id);
									// New Document Keys
									DBObject keys = (DBObject) JSON
											.parse(newDoc);

									temp.put("result", documentService
											.updateDocument(dbName,
													collectionName, id, keys));
								}
							}
							response.put("response", temp);
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

		} catch (EmptyDatabaseNameException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (EmptyCollectionNameException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (EmptyDocumentDataException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);

		} catch (UndefinedDatabaseException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (UndefinedCollectionException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (UndefinedDocumentException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (DeleteDocumentException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (InsertDocumentException e) {// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (UpdateDocumentException e) {
			// Form a JSON Error Object
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());
			logger.error(error);
			JSONObject temp = new JSONObject();
			temp.put("error", error);
			response.put("response", temp);
		} catch (DocumentException e) {
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
