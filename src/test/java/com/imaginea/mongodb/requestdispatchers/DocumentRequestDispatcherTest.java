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

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.log4j.Logger; 
import org.json.JSONException; 
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider; 
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.UserLogin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the document request dispatcher resource that handles the GET and POST
 * request for documents present in Mongo. Tests the get and post functions
 * metioned in the resource with dummy request and test document and collection
 * names and check the functionality.
 * 
 * 
 * @author Rachit Mittal
 * @since 16 Jul 2011
 * 
 */
public class DocumentRequestDispatcherTest extends BaseRequestDispatcher {

	private MongoInstanceProvider mongoInstanceProvider;
	private Mongo mongoInstance;
	/**
	 * Object of class to be tested
	 */
	private DocumentRequestDispatcher testDocResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(DocumentRequestDispatcherTest.class);

	/**
	 * A test token Id
	 */
	private String testTokenId = "123212178917845678910910";

	/**
	 * Constructs a mongoInstanceProvider Object.
	 * 
	 * @throws MongoHostUnknownException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public DocumentRequestDispatcherTest() throws MongoHostUnknownException, IOException, FileNotFoundException {
		 
		try {
			mongoInstanceProvider = new ConfigMongoInstanceProvider();
		} catch (FileNotFoundException e) {
			formErrorResponse(logger, e.getMessage(), ErrorCodes.FILE_NOT_FOUND_EXCEPTION, e.getStackTrace(), "ERROR");
			throw e;
		} catch (MongoHostUnknownException e) {
			formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
			throw e;

		} catch (IOException e) {
			formErrorResponse(logger, e.getMessage(), ErrorCodes.IO_EXCEPTION, e.getStackTrace(), "ERROR");
			throw e;
		}
	}

	/**
	 * Instantiates the object of class under test and also creates an instance
	 * of mongo using the mongo service provider that reads from config file in
	 * order to test resources.Here we also put our tokenId in session and in
	 * mappings defined in UserLogin class so that user is authentcated.
	 * 
	 */
	@Before
	public void instantiateTestClass() {

		// Creates Mongo Instance.
		mongoInstance = mongoInstanceProvider.getMongoInstance();
		// Class to be tested
		testDocResource = new DocumentRequestDispatcher();
		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String user = "username_" + mongoInstance.getAddress() + "_" + mongoInstance.getConnectPoint();
		UserLogin.tokenIDToUserMapping.put(testTokenId, user);
		UserLogin.userToMongoInstanceMapping.put(user, mongoInstance);

	}

	/**
	 * Tests the GET Request which gets names of all documents present in Mongo.
	 * Here we construct the test document first and will test if this created
	 * document is present in the response of the GET Request made. If it is,
	 * then tested ok.
	 * 
	 * @throws DocumentException
	 * 
	 * 
	 */

	@Test
	public void getDocRequest() throws DocumentException, JSONException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		List<String> testCollNames = new ArrayList<String>();
		testCollNames.add("foo");
		testCollNames.add("");

		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		if (logger.isInfoEnabled()) {
			logger.info("Testing GET Doc Resource");
		}
		for (String dbName : testDbNames) {
			for (String collName : testCollNames) {
				for (DBObject documentName : testDocumentNames)
					try {
						if (logger.isInfoEnabled()) {
							logger.info("Create collection [ " + collName + "] inside Db [" + dbName + "] first");
						}
						if (dbName != null && collName != null) {
							if (!dbName.equals("") && !collName.equals("")) {
								if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
									DBObject options = new BasicDBObject();
									mongoInstance.getDB(dbName).createCollection(collName, options);
								}
								if (logger.isInfoEnabled()) {
									logger.info("Insert document");
								}
								mongoInstance.getDB(dbName).getCollection(collName).insert(documentName);
							}
						}

						if (logger.isInfoEnabled()) {
							logger.info("Sends a MockHTTP Request with a Mock Session parameter tokenId");
						}

						MockHttpSession session = new MockHttpSession();
						session.setAttribute("tokenId", testTokenId);

						MockHttpServletRequest request = new MockHttpServletRequest();
						request.setSession(session);
						String fields = "test,_id";

						String docList = testDocResource.getQueriedDocsList(dbName, collName, null, testTokenId, fields, "100", "0", request);

						DBObject response = (BasicDBObject) JSON.parse(docList);
						if (logger.isInfoEnabled()) {
							logger.info("Response : [" + docList + "]");
						}

						if (dbName == null) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

						} else if (dbName.equals("")) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
						} else {
							if (collName == null) {
								DBObject error = (BasicDBObject) response.get("response");
								String code = (String) ((BasicDBObject) error.get("error")).get("code");
								assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);

							} else if (collName.equals("")) {
								DBObject error = (BasicDBObject) response.get("response");
								String code = (String) ((BasicDBObject) error.get("error")).get("code");
								assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);// DB
																						// exists
							} else {
								DBObject result = (BasicDBObject) response.get("response");
								BasicDBList docs = ((BasicDBList) result.get("result"));
								for (int index = 0; index < docs.size(); index++) {
									DBObject doc = (BasicDBObject) docs.get(index);
									if (doc.get("test") != null) {
										assertEquals(doc.get("test"), documentName.get("test"));
										break;
									}

								}
								mongoInstance.dropDatabase(dbName);
							}
						}

					} catch (JSONException e) {
						throw e;
					} catch (MongoException m) {
						DocumentException e = new DocumentException(ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION, "GET_DOCUMENT_LIST_EXCEPTION",
								m.getCause());
						formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
				if (logger.isInfoEnabled()) {
					logger.info("Test Completed");
				}
			}
		}
	}

	/**
	 * Tests the POST Request which create document in Mongo Db. Here we
	 * construct the Test document using service first and then will check if
	 * that document exists in the list.
	 * 
	 * @throws DocumentException
	 * 
	 * 
	 */
	@Test
	public void createDocRequest() throws DocumentException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		List<String> testCollNames = new ArrayList<String>();
		testCollNames.add("foo");
		testCollNames.add("");

		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		if (logger.isInfoEnabled()) {
			logger.info("Testing Create Doc Resource");
		}
		for (String dbName : testDbNames) {
			for (String collName : testCollNames) {
				for (DBObject documentName : testDocumentNames)
					try {
						if (logger.isInfoEnabled()) {
							logger.info("Create collection [ " + collName + "] inside Db [" + dbName + "] first");
						}
						if (dbName != null && collName != null) {
							if (!dbName.equals("") && !collName.equals("")) {
								if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
									DBObject options = new BasicDBObject();
									mongoInstance.getDB(dbName).createCollection(collName, options);
								}
							}
						}

						if (logger.isInfoEnabled()) {
							logger.info("Create Document using Mock HTTP call");
						}

						// set tokenId in session
						MockHttpSession session = new MockHttpSession();
						session.setAttribute("tokenId", testTokenId);

						MockHttpServletRequest request = new MockHttpServletRequest();
						request.setSession(session);

						String resp = testDocResource.postDocsRequest(dbName, collName, "PUT", documentName.toString(), null, null, testTokenId,
								request);
						DBObject response = (BasicDBObject) JSON.parse(resp);

						if (dbName == null) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

						} else if (dbName.equals("")) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
						} else {
							if (collName == null) {
								DBObject error = (BasicDBObject) response.get("response");
								String code = (String) ((BasicDBObject) error.get("error")).get("code");
								assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);

							} else if (collName.equals("")) {
								DBObject error = (BasicDBObject) response.get("response");
								String code = (String) ((BasicDBObject) error.get("error")).get("code");
								assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);// DB
																						// exists
							} else {
								List<DBObject> documentList = new ArrayList<DBObject>();

								DBCursor cursor = mongoInstance.getDB(dbName).getCollection(collName).find();
								while (cursor.hasNext()) {
									documentList.add(cursor.next());
								}
								if (logger.isInfoEnabled()) {
									logger.info("Get Document List: [" + documentList + "]");
								}

								boolean flag = false;
								for (DBObject document : documentList) {
									for (String key : documentName.keySet()) {
										if (document.get(key) != null) {
											assertEquals(document.get(key), documentName.get(key));
											flag = true;
										} else {
											flag = false;
											break; // break from inner
										}
									}
								}
								if (!flag) {
									assert (false);
								}
								// Delete the document
								mongoInstance.getDB(dbName).getCollection(collName).remove(documentName);
							}
						}

					} catch (MongoException m) {
						DocumentException e = new DocumentException(ErrorCodes.DB_CREATION_EXCEPTION, "DB_CREATION_EXCEPTION", m.getCause());

						formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
				if (logger.isInfoEnabled()) {
					logger.info("Test Completed");
				}
			}
		}
	}
 
 
}