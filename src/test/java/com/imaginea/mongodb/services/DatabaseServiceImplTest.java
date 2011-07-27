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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteDatabaseException;
import com.imaginea.mongodb.common.exceptions.DuplicateDatabaseException;
import com.imaginea.mongodb.common.exceptions.EmptyDatabaseNameException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDatabaseException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.UndefinedDatabaseException;
import com.imaginea.mongodb.services.servlet.UserLogin;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test all the Service functions on Database present in MongoDb.
 *
 * @author Rachit Mittal
 *
 */
public class DatabaseServiceImplTest {

	/**
	 * Instance of class to be tested.
	 */
	private DatabaseServiceImpl testDbService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceBehaviour;
	private Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger
			.getLogger(DatabaseServiceImplTest.class);

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
	public DatabaseServiceImplTest() throws MongoHostUnknownException,
			IOException, FileNotFoundException, JSONException {
		super();

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/DatabaseTestLogs.txt", true);

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

		// Creates Mongo Instance.
		mongoInstance = mongoInstanceBehaviour.getMongoInstance();

		// Add User to queue in UserLogin class which service file uses.
		String userMappingKey = testUsername + "_" + mongoInstance.getAddress()
				+ "_" + mongoInstance.getConnectPoint();

		UserLogin.userToMongoInstanceMapping.put(userMappingKey, mongoInstance);

		// Class to be tested
		testDbService = new DatabaseServiceImpl(userMappingKey);

	}

	/**
	 * Tests getAllDb() service of Mongo Db. Hereby we first create a Database
	 * and check whether get Service shows that Db in the list of Db Names
	 *
	 * @throws DatabaseException
	 *             ,DeleteDatabaseException,JSONException
	 */
	@Test
	public void getDBRequest() throws DatabaseException, JSONException,
			DeleteDatabaseException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("admin");
		testDbNames.add(null);
		testDbNames.add("");

		logger.info("Testing GetDb service");
		for (String dbName : testDbNames) {
			logger.info("Test Case : Db [ " + dbName + "]");
			logger.info("Create a Db first");
			if (dbName != null) {
				if (!dbName.equals("")) {
					if (!mongoInstance.getDatabaseNames().contains(dbName)) {
						mongoInstance.getDB(dbName).getCollectionNames();
					}
				}
			}

			List<String> dbNames;
			try {
				dbNames = testDbService.getAllDb();
				logger.info(" Response from Service : [" + dbNames + "]");
				if (dbName == null) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("")) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("admin")) {
					assert (dbNames.contains(dbName));
				} else {
					assert (dbNames.contains(dbName));
					// Db not populate by test Cases
					mongoInstance.dropDatabase(dbName);
				}

			} catch (DatabaseException e) {

				// log error
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
			} catch (MongoException m) // while dropping Db
			{
				DeleteDatabaseException e = new DeleteDatabaseException(
						"Error Deleting Database", m.getCause());
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
		logger.info("Test Completed");
	}

	/**
	 * Tests CreateDb() Service on Database in Mongo Db. Here we create a new
	 * Database using the Service and check if the Database created is present
	 * in the list of Databases in Mongo.
	 *
	 * @throws JSONException
	 * @throws CollectionException
	 *             : generated while creating db in service
	 * @throws DatabaseException
	 *             : generated here while testing when creating db
	 */
	@Test
	public void testCreateDb() throws JSONException, CollectionException,
			DatabaseException {
		logger.info("Testing CreateDb service");
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);
		for (String dbName : testDbNames) {

			try {

				logger.info("Create Db using the Service");
				testDbService.createDb(dbName);

				logger.info("Get List of Databases from Mongo");
				List<String> dbNames = mongoInstance.getDatabaseNames();
				if (dbName == null) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("")) {
					assert (!dbNames.contains(dbName));
				} else {
					assert (dbNames.contains(dbName));
					// Db not populate by test Cases
					mongoInstance.dropDatabase(dbName);
				}

			} catch (DuplicateDatabaseException e) {

				// This if for Db <admin> .
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
			} catch (InsertDatabaseException e) {

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

			} catch (MongoException m) // while getting Db List
			{
				DatabaseException e = new DatabaseException(
						ErrorCodes.DB_CREATION_EXCEPTION,
						"DB_CREATION_EXCEPTION", m.getCause());
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

		logger.info("Test Completed");
	}

	/**
	 * Tests DropDb() Service on Database in Mongo Db. Here we delete a new
	 * Database using the Service and check if the Database created is not
	 * present in the list of Databases in Mongo.
	 *
	 * @throws JSONException
	 */

	@Test
	public void testDropDb() throws JSONException, DeleteDatabaseException,
			MongoException {

		logger.info("Testing dropDb service");
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		for (String dbName : testDbNames) {

			try {

				logger.info(" Create testDb if not present");
				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							mongoInstance.getDB(dbName).getCollectionNames();
						}
					}
				}

				logger.info("Delete Db using the Service");

				testDbService.dropDb(dbName);

				logger.info("Get List of Databases from Mongo");
				List<String> dbNames = mongoInstance.getDatabaseNames();

				if (dbName == null) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("")) {
					assert (!dbNames.contains(dbName));
				} else {
					assert (dbNames.contains(dbName));
					// Db not populate by test Cases
					mongoInstance.dropDatabase(dbName);
				}

			} catch (EmptyDatabaseNameException e) {

				// This if for Db "" and null.
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
				// This if for Db that does not exist
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
			} catch (DeleteDatabaseException e) {

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

			} catch (MongoException m) // while getting Db List
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

		logger.info("Test Completed");

	}

	/**
	 * This will test getDbStats(). Hereby we create an empty Db and verify that
	 * the collections field in <dbName> Statistics is empty. Also since for a
	 * Database that does not exist , the result is same as empty Db beacuse
	 * mongo creates Db then .
	 *
	 *
	 */
	@Test
	public void testGetDbStats() throws JSONException, JSONException,
			CollectionException {
		logger.info("Testing Get Db Stats Service");
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");

		// TODO test for more inputs.
		for (String dbName : testDbNames) {
			try {

				logger.info("Create an empty database");
				if (mongoInstance.getDatabaseNames().contains(dbName)) {
					// Delete if exist
					mongoInstance.dropDatabase(dbName);
				}
				mongoInstance.getDB(dbName).getCollectionNames(); // Create

				JSONArray dbStats = testDbService.getDbStats(dbName);

				for (int i = 0; i < dbStats.length(); i++) {
					JSONObject temp = (JSONObject) dbStats.get(i);
					if (temp.get("Key").equals("collections")) {
						int noOfCollections = Integer.parseInt((String) temp
								.get("Value"));
						logger.info("Number of Collections : "
								+ noOfCollections);
						assertEquals(noOfCollections, 0); // As Empty Db
						break;
					}
				}

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
			} catch (EmptyDatabaseNameException e) {
				JSONObject error = new JSONObject();
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				JSONObject response = new JSONObject();
				response.put("response", temp);
				logger.info(response.toString());
				assert (true);

			} catch (UndefinedDatabaseException e) {
				JSONObject error = new JSONObject();
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				JSONObject response = new JSONObject();
				response.put("response", temp);
				logger.info(response.toString());
				assert (true);

			} catch (DatabaseException e) {
				JSONObject error = new JSONObject();
				error.put("message", e.getMessage());
				error.put("code", e.getErrorCode());
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				JSONObject response = new JSONObject();
				response.put("response", temp);
				logger.info(response.toString());
				assert (true);

			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(
						ErrorCodes.GET_DB_STATS_EXCEPTION,
						"GET_DB_STATS_EXCEPTION", m.getCause());
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
