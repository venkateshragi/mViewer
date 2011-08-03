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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.types.ObjectId; 
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteDocumentException;
import com.imaginea.mongodb.common.exceptions.DocumentException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDocumentException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.UpdateDocumentException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test all the service functions like create/update/delete documents in
 * collections inside databases present in MongoDb.
 * 
 * @author Rachit Mittal
 * @since 16 July 2011
 * 
 */
public class DocumentServiceImplTest extends BaseRequestDispatcher {

	/**
	 * Instance of class to be tested.
	 */
	private DocumentServiceImpl testDocService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceProvider;
	private static  Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger
			.getLogger(DocumentServiceImplTest.class);

	private static final String logConfigFile = "src/main/resources/log4j.properties";
	 
	
	/**
	 * Constructs a mongoInstanceProvider Object.
	 * @throws Exception 
	 */

	
	public DocumentServiceImplTest() throws Exception {
		try {
		 
			mongoInstanceProvider = new ConfigMongoInstanceProvider();
			PropertyConfigurator.configure(logConfigFile);
		} catch (FileNotFoundException e) {
			formErrorResponse(logger, e.getMessage(),
					ErrorCodes.FILE_NOT_FOUND_EXCEPTION, e.getStackTrace(),
					"ERROR");
			throw e;
		} catch (MongoHostUnknownException e) {
			formErrorResponse(logger, e.getMessage(), e.getErrorCode(),
					e.getStackTrace(), "ERROR");
			throw e;

		} catch (IOException e) {
			formErrorResponse(logger, e.getMessage(), ErrorCodes.IO_EXCEPTION,
					e.getStackTrace(), "ERROR");
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

		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String userMappingKey = "username_" + mongoInstance.getAddress() + "_"
				+ mongoInstance.getConnectPoint();

		UserLogin.userToMongoInstanceMapping.put(userMappingKey, mongoInstance);
		// Class to be tested
		testDocService = new DocumentServiceImpl(userMappingKey);
	}

	/**
	 * Tests get documents service to get all documents in a collection inside a
	 * database. Here we will create a test document in a collection inside a
	 * Database and will check if that document exists in the document list from
	 * the service.
	 * 
	 * @throws DatabaseException
	 *             , CollectionException, DocumentException, ValidationException
	 */
	@Test
	public void getDocList() throws DatabaseException, CollectionException,
			DocumentException, ValidationException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("p", "q"));

		if (logger.isInfoEnabled()) {
			logger.info("Testing Get Documents Service.");
		}
		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {
						if (logger.isInfoEnabled()) {
							logger.info(" Insert document [" + documentName
									+ "]");
						}
						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							// Create Collection first
							mongoInstance.getDB(dbName).createCollection(
									collectionName, null);
						}
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.insert(documentName);

						// Test with null query and with  keys "p" 
						DBObject keys = new BasicDBObject();
						keys.put("p", 1);
						ArrayList<DBObject> documentList = testDocService
								.getQueriedDocsList(dbName, collectionName,
										null, keys, 0, 0);
						if (logger.isInfoEnabled()) {
							logger.info("List of Document from Response: ["
									+ documentList + "]");
						}
					 
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

					} catch (DatabaseException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (CollectionException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (DocumentException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (ValidationException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (MongoException m) // while dropping Db
					{
						DocumentException e = new DocumentException(
								ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION,
								"Error Testing Document List", m.getCause());
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}

	}

	/**
	 * Tests insert document service to insert Document in a Collection inside a
	 * Database. Here we will create a test document in a collection inside a
	 * Database using the service and will check if that document exists in the
	 * document list.
	 * 
	 * @throws DatabaseException
	 *             , CollectionException, DocumentException, ValidationException
	 * 
	 */
	@Test
	public void testInsertDocument() throws DatabaseException,
			CollectionException, DocumentException, ValidationException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		if (logger.isInfoEnabled()) {
			logger.info("Testing Insert Document Service.");
		}

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

						if (logger.isInfoEnabled()) {
							logger.info("Insert the document [ " + documentName
									+ "]");
						}
						testDocService.insertDocument(dbName, collectionName,
								documentName);

						List<DBObject> documentList = new ArrayList<DBObject>();

						DBCursor cursor = mongoInstance.getDB(dbName)
								.getCollection(collectionName).find();
						while (cursor.hasNext()) {
							documentList.add(cursor.next());
						}
						if (logger.isInfoEnabled()) {
							logger.info("Get Document List: [" + documentList
									+ "]");
						}

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

					} catch (DatabaseException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (CollectionException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (DocumentException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (ValidationException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (MongoException m) // while dropping Db
					{
						InsertDocumentException e = new InsertDocumentException(
								"Error Testing Document insert", m.getCause());
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}

	}

	/**
	 * Tests update document service to update Document in a Collection inside a
	 * Database. Here we will update a test document in a collection inside a
	 * Database using the service and will check if that old document is
	 * updated.
	 * 
	 * @throws DatabaseException
	 *             , CollectionException, DocumentException, ValidationException
	 */
	@Test
	public void testUpdateDocument() throws DatabaseException,
			CollectionException, DocumentException, ValidationException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");
		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");
		List<DBObject> testDocumentNames = new ArrayList<DBObject>();
		testDocumentNames.add(new BasicDBObject("test", "test"));

		if (logger.isInfoEnabled()) {
			logger.info("Testing Update Document Service");
		}
		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				for (DBObject documentName : testDocumentNames)
					try {

						DBObject newDocument = new BasicDBObject();
						newDocument.put("test1", "newTest");

						// Create collection first
						if (!mongoInstance.getDB(dbName).getCollectionNames()
								.contains(collectionName)) {

							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(
									collectionName, options);
						}

						if (logger.isInfoEnabled()) {
							logger.info("Insert the old document [ "
									+ documentName + "]");
						}
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.insert(documentName);

						// get Object id of inserted old document
						DBObject document = mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.findOne(documentName);
						ObjectId id = (ObjectId) document.get("_id");

						newDocument.put("_id", id.toString()); // same id

						if (logger.isInfoEnabled()) {
							logger.info("Update to new document [ "
									+ newDocument + "]");
						}

						testDocService.updateDocument(dbName, collectionName,
								id, newDocument);

						DBObject query = new BasicDBObject("_id", id);
						DBCollection collection = mongoInstance.getDB(dbName)
								.getCollection(collectionName);
						document = collection.findOne(query);

						if (document == null) {
							assert (false);
						}
						String value = (String) document.get("test");
						if (logger.isInfoEnabled()) {
							logger.info("Get Value of test key in Document with old document _id : ["
									+ newDocument.get("test") + "]");
						}
					 
						assertEquals(newDocument.get("test"),value);

						// Delete the document
						mongoInstance.getDB(dbName)
								.getCollection(collectionName)
								.remove(newDocument);

					} catch (DatabaseException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (CollectionException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (DocumentException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (ValidationException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (MongoException m) // while dropping Db
					{
						UpdateDocumentException e = new UpdateDocumentException(
								"Error Testing Document update", m.getCause());
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests delete document service to delete document in a Collection inside a
	 * Database. Here we will delete a test document in a collection inside a
	 * Database using the service and will check if that document exists in the
	 * document list.
	 * 
	 * @throws DatabaseException
	 *             , CollectionException, DocumentException, ValidationException
	 */
	@Test
	public void testDeleteDocument() throws DatabaseException,
			CollectionException, DocumentException, ValidationException {
		if (logger.isInfoEnabled()) {
			logger.info("Test Delete Document Service");
		}
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

						if (logger.isInfoEnabled()) {
							logger.info("Insert document [ " + documentName
									+ "]");
						}
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

						if (logger.isInfoEnabled()) {
							logger.info("Delete the document with  _id using service");
						}
						testDocService.deleteDocument(dbName, collectionName,
								id);

						if (logger.isInfoEnabled()) {
							logger.info(" Get Document List");
						}
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

					} catch (DatabaseException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (CollectionException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (DocumentException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (ValidationException e) {
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						assert (true);

					} catch (MongoException m) // while dropping Db
					{
						DeleteDocumentException e = new DeleteDocumentException(
								"Error Testing Document delete", m.getCause());
						formErrorResponse(logger, e.getMessage(),
								e.getErrorCode(), e.getStackTrace(), "ERROR");
						throw e;
					}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}
	@AfterClass
	public static void destroyMongoProcess() {
		mongoInstance.close();
	}
}
