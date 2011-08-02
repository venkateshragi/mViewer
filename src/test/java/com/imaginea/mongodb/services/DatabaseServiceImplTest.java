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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.DeleteDatabaseException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InsertDatabaseException;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.common.exceptions.ValidationException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Test service functions for performing operations like create/drop on
 * databases present in Mongo Db.
 * 
 * @author Rachit Mittal
 * @since 16 July 2011
 * 
 */
public class DatabaseServiceImplTest extends BaseRequestDispatcher {

	/**
	 * Instance of class to be tested.
	 */
	private DatabaseServiceImpl testDbService;
	/**
	 * Provides Mongo Instance.
	 */
	private MongoInstanceProvider mongoInstanceProvider;
	private static Mongo mongoInstance;

	/**
	 * Logger Object
	 */
	private static Logger logger = Logger.getLogger(DatabaseServiceImplTest.class);

	private static final String logConfigFile = "src/main/resources/log4j.properties";
	 
	
	/**
	 * Constructs a mongoInstanceProvider Object
	 * @throws Exception 
	 */
	public DatabaseServiceImplTest() throws Exception {
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
		testDbService = new DatabaseServiceImpl(userMappingKey);
	}

	/**
	 * Tests get databases list service function of Mongo Db. Hereby we first create a
	 * Database and check whether get Service shows that Db in the list of Db
	 * Names
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void getDbList() throws DatabaseException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("admin");
		testDbNames.add(null);
		testDbNames.add("");

		if (logger.isInfoEnabled()) {
			logger.info("Testing GetDb service");
		}
		for (String dbName : testDbNames) {
			if (logger.isInfoEnabled()) {
				logger.info("Test Case : Db [ " + dbName + "]");
				logger.info("Create a Db first");
			}
			if (dbName != null) {
				if (!dbName.equals("")) {
					if (!mongoInstance.getDatabaseNames().contains(dbName)) {
						mongoInstance.getDB(dbName).getCollectionNames();
					}
				}
			}

			List<String> dbNames;
			try {
				dbNames = testDbService.getDbList();
				if (logger.isInfoEnabled()) {
					logger.info(" Response from Service : [" + dbNames + "]");
				}
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
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (MongoException m) // while dropping Db
			{
				DatabaseException e = new DatabaseException(ErrorCodes.GET_DB_LIST_EXCEPTION, "Error Testing Database List", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("Test Completed");
		}
	}

	/**
	 * Tests service function that creates database in Mongo Db. Here we create
	 * a new database using the Service and check if the database created is
	 * present in the list of databases in Mongo.
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void createDb() throws DatabaseException {
		if (logger.isInfoEnabled()) {
			logger.info("Testing CreateDb service");
		}
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);
		for (String dbName : testDbNames) {

			try {

				if (logger.isInfoEnabled()) {
					logger.info("Create Db using the Service");
				}
				testDbService.createDb(dbName);

				if (logger.isInfoEnabled()) {
					logger.info("Get List of Databases from Mongo");
				}
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

			} catch (DatabaseException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (ValidationException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (MongoException m) {
				InsertDatabaseException e = new InsertDatabaseException("Error Testing Database insert operation", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests service function that deletes database in Mongo Db. Here we delete
	 * database using the Service and check if the database created is present
	 * in the list of databases in Mongo.
	 * 
	 * @throws DatabaseException
	 */

	@Test
	public void dropDb() throws DatabaseException {

		if (logger.isInfoEnabled()) {
			logger.info("Testing drop database service");
		}
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		for (String dbName : testDbNames) {

			try {

				if (logger.isInfoEnabled()) {
					logger.info(" Create testDb if not present");
				}
				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							mongoInstance.getDB(dbName).getCollectionNames();
						}
					}
				}

				if (logger.isInfoEnabled()) {
					logger.info("Delete Db using the Service");
				}

				testDbService.dropDb(dbName);

				if (logger.isInfoEnabled()) {
					logger.info("Get List of Databases from Mongo");
				}
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

			} catch (DatabaseException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (ValidationException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (MongoException m) {
				DeleteDatabaseException e = new DeleteDatabaseException("Error Testing Database delete operation", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}

	}

	/**
	 * 
	 * Tests service function that gets statistcs of a database in Mongo Db.
	 * Hereby we create an empty Db and verify that the collections field in
	 * database statistics is empty.
	 * 
	 * @throws DatabaseException
	 *             ,JSONException
	 */
	@Test
	public void getDbStats() throws JSONException, DatabaseException {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Get Db Stats Service");
		}
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");

		for (String dbName : testDbNames) {
			try {

				if (logger.isInfoEnabled()) {
					logger.info("Create an empty database");
				}
				if (mongoInstance.getDatabaseNames().contains(dbName)) {
					// Delete if exist
					mongoInstance.dropDatabase(dbName);
				}
				mongoInstance.getDB(dbName).getCollectionNames(); // Create

				JSONArray dbStats = testDbService.getDbStats(dbName);

				for (int i = 0; i < dbStats.length(); i++) {
					JSONObject temp = (JSONObject) dbStats.get(i);
					if (temp.get("Key").equals("collections")) {
						int noOfCollections = Integer.parseInt((String) temp.get("Value"));
						if (logger.isInfoEnabled()) {
							logger.info("Number of Collections : " + noOfCollections);
						}
						assertEquals(noOfCollections, 0); // As Empty Db
						break;
					}
				}

			} catch (JSONException e) {
				formErrorResponse(logger, e.getMessage(), ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
				throw e;
			} catch (DatabaseException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (ValidationException e) {
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				assert (true);

			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(ErrorCodes.GET_DB_STATS_EXCEPTION, "Error Testing Database stats operation", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("Test Completed");
		}
	}
	
	@AfterClass
	public static void destroyMongoProcess() {
		mongoInstance.close();
	}
}
