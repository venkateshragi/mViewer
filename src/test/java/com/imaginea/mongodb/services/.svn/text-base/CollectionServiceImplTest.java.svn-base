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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteCollectionException;
import com.imaginea.mongodb.common.exceptions.DuplicateCollectionException;
import com.imaginea.mongodb.common.exceptions.EmptyCollectionNameException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.UndefinedCollectionException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.services.servlet.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test all the Service functions on collections inside Databases present in
 * MongoDb.
 *
 * @author Rachit Mittal
 *
 */
public class CollectionServiceImplTest {
	/**
	 * Instance of class to be tested.
	 */
	private CollectionServiceImpl testCollService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceBehaviour;
	private Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger
			.getLogger(CollectionServiceImplTest.class);

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
	public CollectionServiceImplTest() throws MongoHostUnknownException,
			IOException, FileNotFoundException, JSONException {
		super();

		// TODO Configure by file
		// TODO Logger writing 4 times
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/CollectionTestLogs.txt", true);

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
	public void instantiateTestClass() {
		// Add User to queue in UserLogin class which service file uses to
		// instantiate.

		// Creates Mongo Instance first.
		mongoInstance = mongoInstanceBehaviour.getMongoInstance();

		String userMappingKey = testUsername + "_" + mongoInstance.getAddress()
				+ "_" + mongoInstance.getConnectPoint();

		UserLogin.userToMongoInstanceMapping.put(userMappingKey, mongoInstance);

		// Class to be tested
		testCollService = new CollectionServiceImpl(userMappingKey);

	}


	/**
	 * Tests getCollection() Service to get all Collections in a Database. Here
	 * we will create a test collection inside a Database and will check if that
	 * collection exists in the collection list from the service.
	 *
	 * @throws DatabaseException
	 *             , CollectionException, JSONException
	 */
	@Test
	public void testGetCollections() throws DatabaseException,
			CollectionException, JSONException {
		logger.info("Testing Get Collections Service");

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					logger.info("Test Case : Db [ " + dbName + "] collection ["
							+ collectionName + "]");
					logger.info("Create Collection");

					if (!mongoInstance.getDB(dbName).getCollectionNames()
							.contains(collectionName)) {
						DBObject options = new BasicDBObject();
						mongoInstance.getDB(dbName).createCollection(
								collectionName, options);
					}

					logger.info("Get List using Service");
					Set<String> collectionList = testCollService
							.getCollections(dbName);
					logger.info("Response: [" + collectionList + "]");
					assert (collectionList.contains(collectionName));

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

				} catch (CollectionException e) {
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
					DatabaseException e = new DatabaseException(
							ErrorCodes.DB_DELETION_EXCEPTION,
							"DB_DELETION_EXCEPTION", m.getCause());
					JSONObject error = new JSONObject();
					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());
					error.put("stackTrace", e.getStackTrace());

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					JSONObject response = new JSONObject();
					response.put("response", temp);
					logger.info(response.toString());
					throw e;

				}
			}
		}

	}

	/**
	 * Tests InsertCollection() service on collections in a Database. Hereby we
	 * will insert a collection using the service and then will check if that
	 * collection is present in the list of collections.
	 *
	 * @throws JSONException
	 *             , DatabaseException
	 *
	 */
	@Test
	public void testInsertCollection() throws JSONException, DatabaseException {
		logger.info("Testing Insert Collections Service");

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					logger.info("Test Case : Db [ " + dbName + "] collection ["
							+ collectionName + "]");

					// Delete the collection
					if (mongoInstance.getDB(dbName).getCollectionNames()
							.contains(collectionName))

					{
						mongoInstance.getDB(dbName)
								.getCollection(collectionName).drop();
					}

					logger.info(" Create collection using service");
					testCollService.insertCollection(dbName, collectionName,
							true, 100000, 100); // size and capped

					Set<String> collectionList = mongoInstance.getDB(dbName)
							.getCollectionNames();
					logger.info("Get List : [" + collectionList + "]");
					assert (collectionList.contains(collectionName));
					// drop this collection
					mongoInstance.getDB(dbName).getCollection(collectionName)
							.drop();

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

				} catch (DuplicateCollectionException e) {
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

				} catch (MongoException m) // while dropping Db
				{
					DatabaseException e = new DatabaseException(
							ErrorCodes.DB_DELETION_EXCEPTION,
							"DB_DELETION_EXCEPTION", m.getCause());
					JSONObject error = new JSONObject();
					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());
					error.put("stackTrace", e.getStackTrace());

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					JSONObject response = new JSONObject();
					response.put("response", temp);
					logger.info(response.toString());
					throw e;
				}
			}
		}
	}

	/**
	 * Tests DeleteCollection() service on collections in a Database. Hereby we
	 * will delete a collection using the service and then will check if that
	 * collection is not present in the collection list.
	 *
	 * @throws DatabaseException
	 * @throws ValidationException
	 *             ,CollectionException
	 *
	 */
	@Test
	public void testDeleteCollection() throws DeleteCollectionException,
			JSONException, CollectionException {
		logger.info("Testing Delete Collections Service");
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {

					logger.info("Test Case : Db [ " + dbName + "] collection ["
							+ collectionName + "]");

					logger.info("Insert Collection if not present");
					if (!mongoInstance.getDB(dbName).getCollectionNames()
							.contains(collectionName)) {
						DBObject options = new BasicDBObject();
						mongoInstance.getDB(dbName).createCollection(
								collectionName, options);
					}

					logger.info(" Delete collection using service ");

					testCollService.deleteCollection(dbName, collectionName);

					Set<String> collectionList = mongoInstance.getDB(dbName)
							.getCollectionNames();
					logger.info("Get List : [" + collectionList + "]");
					assert (!collectionList.contains(collectionName));

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

				} catch (DeleteCollectionException e) {
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

				} catch (MongoException m) // while dropping Db
				{
					CollectionException e = new CollectionException(
							ErrorCodes.GET_COLLECTION_LIST_EXCEPTION,
							"GET_COLLECTION_LIST_EXCEPTION", m.getCause());
					JSONObject error = new JSONObject();
					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());
					error.put("stackTrace", e.getStackTrace());

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					JSONObject response = new JSONObject();
					response.put("response", temp);
					logger.info(response.toString());
					throw e;
				}
			}
		}
	}

	/**
	 * Tests getCollectionStats() service on collections in a Database. Hereby
	 * we will create an empty collection inside a Database and will check the
	 * Number of Documents in the Statistics obtained.
	 *
	 * @throws JSONException
	 *             , JSONParseException,DatabaseException DatabaseException
	 *
	 */
	@Test
	public void testGetCollectionStats() throws JSONException,
			DatabaseException, JSONException {

		logger.info("Testing Stats Collections Service");
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		testDbNames.add("random");

		List<String> testCollectionNames = new ArrayList<String>();
		testCollectionNames.add("foo");

		for (String dbName : testDbNames) {
			for (String collectionName : testCollectionNames) {
				try {
					logger.info("Test Case : Db [ " + dbName + "] collection ["
							+ collectionName + "]");

					// Delete the collection first if exist
					if (mongoInstance.getDB(dbName).getCollectionNames()
							.contains(collectionName)) {

						mongoInstance.getDB(dbName)
								.getCollection(collectionName).drop();
					}

					logger.info(" Create an empty collection");
					DBObject options = new BasicDBObject();
					mongoInstance.getDB(dbName).createCollection(
							collectionName, options);

					JSONArray dbStats = testCollService.getCollectionStats(
							dbName, collectionName);

					for (int i = 0; i < dbStats.length(); i++) {
						JSONObject temp = (JSONObject) dbStats.get(i);
						if (temp.get("Key").equals("count")) {
							int noOfDocuments = Integer.parseInt((String) temp
									.get("Value"));
							logger.info("Number of Documents : "
									+ noOfDocuments);
							assertEquals(noOfDocuments, 0); // As Empty
															// Collection
							break;
						}
					}

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

				} catch (JSONException e) {
					JSONObject error = new JSONObject();
					JSONObject response = new JSONObject();
					error.put("message", e.getMessage());
					error.put("code", ErrorCodes.JSON_EXCEPTION);
					error.put("stackTrace", e.getStackTrace());

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					response.put("response", temp);

					logger.info(response);
					throw e;
				} catch (MongoException m) // while dropping Db
				{
					DatabaseException e = new DatabaseException(
							ErrorCodes.GET_DB_LIST_EXCEPTION,
							"GET_DB_LIST_EXCEPTION", m.getCause());
					JSONObject error = new JSONObject();
					error.put("message", e.getMessage());
					error.put("code", e.getErrorCode());
					error.put("stackTrace", e.getStackTrace());

					JSONObject temp = new JSONObject();
					temp.put("error", error);
					JSONObject response = new JSONObject();
					response.put("response", temp);
					logger.info(response.toString());
					throw e;
				}

			}

		}

	}
}
