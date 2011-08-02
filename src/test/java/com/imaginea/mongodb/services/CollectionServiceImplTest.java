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
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertCollectionException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test all the Service functions on collections inside Databases present in
 * MongoDb.
 * 
 * @author Rachit Mittal
 * @since 16 July 2011
 * 
 */
public class CollectionServiceImplTest extends BaseRequestDispatcher {

	/**
	 * Instance of class to be tested.
	 */
	private CollectionServiceImpl testCollService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceProvider;
	private static  Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger.getLogger(CollectionServiceImplTest.class);

	private static final String logConfigFile = "src/main/resources/log4j.properties";
 
	/**
	 * Constructs a mongoInstanceProvider Object.
	 * @throws Exception 
	 */
	public CollectionServiceImplTest() throws Exception {
		try {
	 
			mongoInstanceProvider = new ConfigMongoInstanceProvider();
			PropertyConfigurator.configure(logConfigFile);
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
		

		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String userMappingKey = "username_" + mongoInstance.getAddress() + "_" + mongoInstance.getConnectPoint();

		UserLogin.userToMongoInstanceMapping.put(userMappingKey, mongoInstance);
		// Class to be tested
		testCollService = new CollectionServiceImpl(userMappingKey);
	}

	/**
	 * Tests get collection service to get all Collections in a database. Here
	 * we will create a test collection inside a Database and will check if that
	 * collection exists in the collection list from the service.
	 * 
	 * @throws CollectionException
	 */
	@Test
	public void getCollList() throws CollectionException {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Get Collections Service");
		}

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					if (logger.isInfoEnabled()) {
						logger.info("Test Case : Db [ " + dbName + "] collection [" + collectionName + "]");
						logger.info("Create Collection");
					}

					if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
						DBObject options = new BasicDBObject();
						mongoInstance.getDB(dbName).createCollection(collectionName, options);
					}

					if (logger.isInfoEnabled()) {
						logger.info("Get List using Service");
					}
					Set<String> collectionList = testCollService.getCollList(dbName);
					if (logger.isInfoEnabled()) {
						logger.info("Response: [" + collectionList + "]");
					}
					assert (collectionList.contains(collectionName));

					// Db not populate by test Cases
					mongoInstance.dropDatabase(dbName);

				} catch (DatabaseException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (CollectionException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (ValidationException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (MongoException m) // while dropping Db
				{
					CollectionException e = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "Error Testing Collection List",
							m.getCause());
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					throw e;
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}

		}

	}

	/**
	 * Tests insert collection service on collections in a Database. Hereby we
	 * will insert a collection using the service and then will check if that
	 * collection is present in the list of collections.
	 * 
	 * @throws CollectionException
	 * 
	 */
	@Test
	public void insertColl() throws CollectionException {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Insert Collections Service");
		}

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					if (logger.isInfoEnabled()) {
						logger.info("Test Case : Db [ " + dbName + "] collection [" + collectionName + "]");
					}

					// Delete the collection
					if (mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
						mongoInstance.getDB(dbName).getCollection(collectionName).drop();
					}

					if (logger.isInfoEnabled()) {
						logger.info(" Create collection using service");
					}
					testCollService.insertCollection(dbName, collectionName, true, 100000, 100);

					Set<String> collectionList = mongoInstance.getDB(dbName).getCollectionNames();
					logger.info("Get List : [" + collectionList + "]");
					assert (collectionList.contains(collectionName));
					// drop this collection
					mongoInstance.getDB(dbName).getCollection(collectionName).drop();

				} catch (DatabaseException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (CollectionException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (ValidationException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (MongoException m) // while dropping Db
				{
					InsertCollectionException e = new InsertCollectionException("Error Testing Collection insert", m.getCause());
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					throw e;
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests delete collection service on collections in a Database. Hereby we
	 * will delete a collection using the service and then will check if that
	 * collection is not present in the collection list.
	 * 
	 * @throws CollectionException
	 * 
	 */
	@Test
	public void deleteColl() throws CollectionException {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Delete Collections Service");
		}
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {

					if (logger.isInfoEnabled()) {
						logger.info("Test Case : Db [ " + dbName + "] collection [" + collectionName + "]");
						logger.info("Insert Collection if not present");
					}
					if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
						DBObject options = new BasicDBObject();
						mongoInstance.getDB(dbName).createCollection(collectionName, options);
					}

					if (logger.isInfoEnabled()) {
						logger.info(" Delete collection using service ");
					}

					testCollService.deleteCollection(dbName, collectionName);

					Set<String> collectionList = mongoInstance.getDB(dbName).getCollectionNames();
					if (logger.isInfoEnabled()) {
						logger.info("Get List : [" + collectionList + "]");
					}
					assert (!collectionList.contains(collectionName));

				} catch (DatabaseException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (CollectionException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (ValidationException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (MongoException m) // while dropping Db
				{
					DeleteCollectionException e = new DeleteCollectionException("Error Testing Collection delete", m.getCause());
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					throw e;
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests get collection statistics service on collections in a database.
	 * Hereby we will create an empty collection inside a Database and will
	 * check the Number of documents in the Statistics obtained.
	 * 
	 * @throws JSONException
	 *             ,CollectionException
	 * 
	 * 
	 */
	@Test
	public void getCollStats() throws JSONException, CollectionException {

		if (logger.isInfoEnabled()) {
			logger.info("Testing Stats Collections Service");
		}
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					if (logger.isInfoEnabled()) {
						logger.info("Test Case : Db [ " + dbName + "] collection [" + collectionName + "]");
					}

					// Delete the collection first if exist
					if (mongoInstance.getDB(dbName).getCollectionNames().contains(collectionName)) {
						mongoInstance.getDB(dbName).getCollection(collectionName).drop();
					}

					if (logger.isInfoEnabled()) {
						logger.info(" Create an empty collection");
					}
					DBObject options = new BasicDBObject();
					mongoInstance.getDB(dbName).createCollection(collectionName, options);

					JSONArray dbStats = testCollService.getCollStats(dbName, collectionName);

					for (int i = 0; i < dbStats.length(); i++) {
						JSONObject temp = (JSONObject) dbStats.get(i);
						if (temp.get("Key").equals("count")) {
							int noOfDocuments = Integer.parseInt((String) temp.get("Value"));
							if (logger.isInfoEnabled()) {
								logger.info("Number of Documents : " + noOfDocuments);
							}
							assertEquals(noOfDocuments, 0); // As Empty
															// Collection
							break;
						}
					}

				} catch (JSONException e) {
					formErrorResponse(logger, e.getMessage(), ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
					throw e;
				} catch (DatabaseException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (CollectionException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (ValidationException e) {
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
					assert (true);

				} catch (MongoException m) // while dropping Db
				{
					CollectionException e = new CollectionException(ErrorCodes.GET_COLL_STATS_EXCEPTION, "Error Testing Collection stats",
							m.getCause());
					formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
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
