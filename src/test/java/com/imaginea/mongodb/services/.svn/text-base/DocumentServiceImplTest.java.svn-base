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
package com.imaginea.mongodb.services;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.DeleteDocumentException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDocumentDataException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDocumentException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.UndefinedDocumentException;
import com.imaginea.mongodb.common.exceptions.UpdateDocumentException;
import com.imaginea.mongodb.services.servlet.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test all the Service functions on Documents in Collections inside Databases
 * present in MongoDb.
 *
 * @author Rachit Mittal
 *
 */
public class DocumentServiceImplTest {

	/**
	 * Instance of class to be tested.
	 */
	private DocumentServiceImpl testDocService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceBehaviour;
	private Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger
			.getLogger(DocumentServiceImplTest.class);

	/**
	 * A test User used in forming a <mappingKey> from which services file pick
	 * up mongo Instance. <mappingKey> is related to Token Id.
	 */
	private String testUsername = "name";

	/**
	 * Constructs a mongoInstanceProvider Object and configures logger.
	 *
	 * @throws MongoHostUnknownException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public DocumentServiceImplTest() throws MongoHostUnknownException,
			IOException, FileNotFoundException, JSONException {
		super();

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/DocumentTestLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);

		try {

			mongoInstanceBehaviour = new ConfigMongoInstanceProvider();

		} catch (FileNotFoundException e) {
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", "FILE_NOT_FOUND_EXCEPTION");
			error.put("stackTrace", e.getStackTrace());

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			JSONObject response = new JSONObject();
			response.put("response", temp);

			logger.info(response.toString());

		} catch (MongoHostUnknownException e) {
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", e.getErrorCode());
			error.put("stackTrace", e.getStackTrace());

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			JSONObject response = new JSONObject();
			response.put("response", temp);

			logger.info(response.toString());

		} catch (IOException e) {
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", "IO_EXCEPTION");
			error.put("stackTrace", e.getStackTrace());

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			JSONObject response = new JSONObject();
			response.put("response", temp);
			logger.info(response.toString());
		}

	}

	/**
	 * Called before each test function and creates a testObject of class to be
	 * tested and a mongoInstance . Also add a MongoInstance for this user in
	 * UserLogin class.
	 *
	 * @throws JSONException
	 *             While writing Error Object
	 */
	@Before
	public void setUp() {

		// Creates Mongo Instance.
		mongoInstance = mongoInstanceBehaviour.getMongoInstance();

		// Add User to queue in UserLogin class which service file uses.
		String userMappingKey = testUsername + "_" + mongoInstance.getAddress()
				+ "_" + mongoInstance.getConnectPoint();

		UserLogin.userToMongoInstanceMapping.put(userMappingKey, mongoInstance);

		// Class to be tested
		testDocService = new DocumentServiceImpl(userMappingKey);

	}

	/**
	 * Tests getDocument() Service to get all Documents in a Collection inside a
	 * Database. Here we will create a test document in a collection inside a
	 * Database and will check if that document exists in the document list from
	 * the service.
	 *
	 * @throws JSONException
	 * @throws InsertDocumentException
	 */
	@Test
	public void testGetDocuments() throws JSONException,
			InsertDocumentException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		logger.info("Testing Get Documents Service.");
		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {
						logger.info(" Insert document [" + documentName + "]");
						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							// Create Collection first
							mongoInstance.getDB(dbName).createCollection(
									collectionName, null);
						}
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.insert(documentName);

						// Test with null query and all keys
						DBObject keys = new BasicDBObject();
						keys.put("test", 1);
						ArrayList<DBObject> documentList = testDocService
								.getDocuments(dbName, collectionName, null,
										keys, 0, 0);
						logger.info("List of Document from Response: ["
								+ documentList + "]");
						boolean flag = false;
						for (DBObject document : documentList) {
							for (String key : documentName.keySet()) {
								if (document.get(key) != null) {
									assertEquals(document.get(key),
											documentName.get(key));
									flag = true;
								} else {
									flag = false;
									break;
								}
							}
						}
						if (!flag) {
							assert (false);
						}
						// Db not populate by test Cases
						mongoInstance.dropDatabase(dbName);

					} catch (EmptyDatabaseNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);

					} catch (EmptyCollectionNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (MongoException m) {
						InsertDocumentException e = new InsertDocumentException(
								"DOCUMENT_CREATION_EXCEPTION", m.getCause());
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					}
			}
		}

	}

	/**
	 * Tests insertDocument() Service to insert Document in a Collection inside
	 * a Database. Here we will create a test document in a collection inside a
	 * Database using the service and will check if that document exists in the
	 * document list.
	 *
	 * @throws JSONException
	 *             , DocumentException, InsertDocumentException
	 */
	@Test
	public void testInsertDocument() throws JSONException, DocumentException,
			InsertDocumentException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		logger.info("Testing Insert Document Service.");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {

						// Create the collection first in which service will

						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(
									collectionName, options);
						}

						logger.info("Insert the document [ " + documentName
								+ "]");
						testDocService.insertDocument(dbName, collectionName,
								documentName);

						List<DBObject> documentList = new ArrayList<DBObject>();

						DBCursor cursor = mongoInstance.getDB(dbName)
								.getCollection(collectionName).find();
						while (cursor.hasNext()) {
							documentList.add(cursor.next());
						}
						logger.info("Get Document List: [" + documentList + "]");

						boolean flag = false;
						for (DBObject document : documentList) {
							for (String key : documentName.keySet()) {
								if (document.get(key) != null) {
									assertEquals(document.get(key),
											documentName.get(key));
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
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.remove(documentName);

					} catch (EmptyCollectionNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyDatabaseNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyDocumentDataException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedDatabaseException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedCollectionException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (InsertDocumentException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					} catch (MongoException m) // while dropping Db
					{
						DocumentException e = new DocumentException(
								ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION,
								"GET_DOCUMENT_LIST_EXCEPTION", m.getCause());

						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					}
			}
		}

	}

	/**
	 * Tests updateDocument() Service to update Document in a Collection inside
	 * a Database. Here we will update a test document in a collection inside a
	 * Database using the service and will check if that old document is
	 * updated.
	 *
	 * @throws JSONException
	 *             , UpdateDocumentException, DocumentException
	 */
	@Test
	public void testUpdateDocument() throws JSONException,
			UpdateDocumentException, DocumentException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		logger.info("Testing Update Document Service");
		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {

						DBObject newDocument = new BasicDBObject();
						newDocument.put("test", "newTest");

						// Create collection first
						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(
									collectionName, options);
						}

						logger.info("Insert the old document [ " + documentName
								+ "]");
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.insert(documentName);

						// get Object id of inserted old document
						DBObject document = mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.findOne(documentName);
						ObjectId id = (ObjectId) document.get("_id");

						newDocument.put("_id", id.toString()); // same id as
																// previous

						logger.info("Update to new document [ " + newDocument
								+ "]");
						testDocService.updateDocument(dbName, collectionName,
								id, newDocument);

						// Get Document with above id and check the value of
						// "test" key.
						DBObject query = new BasicDBObject("_id", id);
						DBCollection collection = this.mongoInstance.getDB(
								dbName).getCollection(collectionName);
						DBCursor cursor = collection.find(query);

						if (cursor.hasNext()) {
							document = cursor.next(); // _id is primary key
						} else {
							// if updated document not found => Test Case Failed
							assert (false);
						}
						String value = (String) document.get("test");
						logger.info("Get Value of test key in Document with old document _id : ["
								+ newDocument.get("test") + "]");
						// Check is value matches the new Document Value
						assertEquals(value, newDocument.get("test"));

						// Delete the document
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.remove(newDocument);

					} catch (EmptyDocumentDataException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyCollectionNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyDatabaseNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedDocumentException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedDatabaseException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedCollectionException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UpdateDocumentException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					} catch (MongoException m) // while dropping Db
					{
						DocumentException e = new DocumentException(
								ErrorCodes.DOCUMENT_CREATION_EXCEPTION,
								"DOCUMENT_CREATION_EXCEPTION", m.getCause());
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					}
			}
		}
	}

	/**
	 * Tests deleteDocument() Service to delete Document in a Collection inside
	 * a Database. Here we will delete a test document in a collection inside a
	 * Database using the service and will check if that document exists in the
	 * document list.
	 *
	 * @throws JSONException
	 *             , DeleteDocumentException, DocumentException
	 */
	@Test
	public void testDeleteDocument() throws JSONException,
			DeleteDocumentException, DocumentException {
		logger.info("Test Delete Document Service");
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {

						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(
									collectionName, options);
						}

						logger.info("Insert document [ " + documentName + "]");
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.insert(documentName);

						// get Object id of inserted document
						DBObject document = mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.findOne(documentName);

						if (document == null) {
							assert (false);
						}
						ObjectId id = (ObjectId) document.get("_id");

						logger.info("Delete the document with  _id using service");
						testDocService.deleteDocument(dbName, collectionName,
								id);

						logger.info(" Get Document List");
						List<DBObject> documentList = new ArrayList<DBObject>();

						DBCursor cursor = mongoInstance.getDB(dbName)
								.getCollection(collectionName).find();
						while (cursor.hasNext()) {
							documentList.add(cursor.next());
						}

						boolean flag = false;
						for (DBObject doc : documentList) {
							for (String key : documentName.keySet()) {
								if (doc.get(key) == null) {
									flag = true;

								} else {
									flag = false; // key present
									break;
								}
							}
						}
						if (!flag) {
							assert (false);
						}

					} catch (EmptyDocumentDataException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyCollectionNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (EmptyDatabaseNameException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedDocumentException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedDatabaseException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (UndefinedCollectionException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						assert (true);
					} catch (DeleteDocumentException e) {
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					} catch (MongoException m) // while dropping Db
					{
						DocumentException e = new DocumentException(
								ErrorCodes.DOCUMENT_CREATION_EXCEPTION,
								"DOCUMENT_CREATION_EXCEPTION", m.getCause());
						JSONObject error = new JSONObject();
						JSONObject response = new JSONObject();
						error.put("message", e.getMessage());
						error.put("code", e.getErrorCode());
						error.put("stackTrace", e.getStackTrace());

						JSONObject temp = new JSONObject();
						temp.put("error", error);
						response.put("response", temp);

						logger.info(response);
						throw e;
					}
			}
		}
	}
}
