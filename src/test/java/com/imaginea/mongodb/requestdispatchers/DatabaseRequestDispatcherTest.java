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
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.DatabaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the database request dispatcher resource that handles the GET and POST
 * request for performing operations on databases present in Mongo. Hereby we
 * test the get and post resources with dummy request and check the
 * functionality.
 * 
 * An ArrayList of various test DbNames possible has been taken and functions
 * are tested for all of them.
 * 
 * @author Rachit Mittal
 * @since 14 July 2011
 * 
 */
public class DatabaseRequestDispatcherTest extends BaseRequestDispatcher {

	private MongoInstanceProvider mongoInstanceProvider;

	private Mongo mongoInstance;

	/**
	 * Class to be tested.
	 */
	private DatabaseRequestDispatcher testDbResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(DatabaseRequestDispatcherTest.class);

	private String testTokenId = "123212178917845678910910";
	private static final String logConfigFile = "src/main/resources/log4j.properties";
	private static final String mongoProcessPath = "c:\\mongo\\bin\\mongod";
	// Mongod Process to be started
	private static Process p;
	
	/**
	 * Constructs a mongoInstanceProvider Object.
	 * 
	 * @throws MongoHostUnknownException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public DatabaseRequestDispatcherTest() throws MongoHostUnknownException, IOException, FileNotFoundException {
		 
		try {
			// Start Mongod
			Runtime run = Runtime.getRuntime();
			p = run.exec(mongoProcessPath);
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
		// Class to be tested
		testDbResource = new DatabaseRequestDispatcher();
		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String user = "username_" + mongoInstance.getAddress() + "_" + mongoInstance.getConnectPoint();
		UserLogin.tokenIDToUserMapping.put(testTokenId, user);
		UserLogin.userToMongoInstanceMapping.put(user, mongoInstance);

	}

	/**
	 * Tests the GET Request which gets names of all databases present in Mongo.
	 * Here we construct the Test Database first and will test if this created
	 * Database is present in the response of the GET Request made. If it is,
	 * then tested ok. We will try it with multiple test Databases.
	 * 
	 * @throws DatabaseException
	 * 
	 * 
	 */

	@Test
	public void getdbList() throws DatabaseException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);
		testDbNames.add("admin");
		if (logger.isInfoEnabled()) {
			logger.info("Testing GetDb Resource");
		}
		for (String dbName : testDbNames) {
			logger.info("Test Case  [" + dbName + "]");
			try {
				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							mongoInstance.getDB(dbName).getCollectionNames();
						}
					}
				}
				if (logger.isInfoEnabled()) {
				logger.info("Sends a MockHTTP Request with a Mock Session parameter tokenId");
				}
				MockHttpSession session = new MockHttpSession();
				session.setAttribute("tokenId", testTokenId);

				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setSession(session);

				String dbList = testDbResource.getDbList(testTokenId, request);

				// response has a JSON Object with result as key and value as
				DBObject response = (BasicDBObject) JSON.parse(dbList);
				DBObject result = (BasicDBObject) response.get("response");

				BasicDBList dbNames = (BasicDBList) result.get("result");
				if (logger.isInfoEnabled()) {
					logger.info("Response : [ " + dbNames + "]");
				}
				if (dbName == null) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("")) {
					assert (!dbNames.contains(dbName));
				} else if (dbName.equals("admin")) {
					assert (dbNames.contains(dbName)); // dont delete admin
				} else {
					assert (dbNames.contains(dbName));
					mongoInstance.dropDatabase(dbName);

				}
				if (logger.isInfoEnabled()) {
					logger.info("Test Completed");
				}

			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(ErrorCodes.GET_DB_LIST_EXCEPTION, "GET_DB_LIST_EXCEPTION", m.getCause());
				// Write in logger
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
		}
	}

	/**
	 * Tests a POST request made to database resource for creation of a database
	 * in mongo db.
	 * 
	 * @throws DatabaseException
	 */

	@Test
	public void createDatabase() throws DatabaseException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add("admin");
		testDbNames.add(null);
		if (logger.isInfoEnabled()) {
			logger.info("Testing CreateDb Resource");
		}
		for (String dbName : testDbNames) {
			if (logger.isInfoEnabled()) {
				logger.info("Test Case  [" + dbName + "]");
				logger.info("Create Db using service");
			}
			try {

				MockHttpSession session = new MockHttpSession();
				session.setAttribute("tokenId", testTokenId);

				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setSession(session);

				String resp = testDbResource.postDbRequest(dbName, "PUT", testTokenId, request);
				if (logger.isInfoEnabled()) {
					logger.info("Service Response: [ " + resp + "]");
					logger.info("Get Db List");
				}

				List<String> dbNames = mongoInstance.getDatabaseNames();
				if (logger.isInfoEnabled()) {
					logger.info("Response : [" + dbNames + "]");
				}

				if (dbName == null) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error")).get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

				} else if (dbName.equals("")) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error")).get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
				} else if (dbName.equals("admin")) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error")).get("code");
					assertEquals(ErrorCodes.DB_ALREADY_EXISTS, code);
				} else {
					assert (dbNames.contains(dbName));
					mongoInstance.dropDatabase(dbName);
				}

			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(ErrorCodes.DB_CREATION_EXCEPTION, "DB_CREATION_EXCEPTION", m.getCause());
				// Write in logger
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests a POST request made to database resource for deletion of a database
	 * in mongo db.
	 * 
	 * @throws DatabaseException
	 */

	@Test
	public void deleteDatabase() throws DatabaseException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);
		if (logger.isInfoEnabled()) {
			logger.info("Testing Delete Db Resource");
		}
		for (String dbName : testDbNames) {
			if (logger.isInfoEnabled()) {
				logger.info("Test Case  [" + dbName + "]");
				logger.info("Create a Db first");
			}
			try {
				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							mongoInstance.getDB(dbName).getCollectionNames();
						}
					}
				}
				if (logger.isInfoEnabled()) {
					logger.info("Delete Db using service");
				}
				MockHttpSession session = new MockHttpSession();
				session.setAttribute("tokenId", testTokenId);

				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setSession(session);

				String resp = testDbResource.postDbRequest(dbName, "DELETE", testTokenId, request);
				if (logger.isInfoEnabled()) {
					logger.info("Service Response: [ " + resp + "]");
					logger.info("Get Db List");
				}
				List<String> dbNames = mongoInstance.getDatabaseNames();
				if (logger.isInfoEnabled()) {
					logger.info("Response : [" + dbNames + "]");
				}

				if (dbName == null) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error")).get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

				} else if (dbName.equals("")) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error")).get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
				} else {
					assert (!dbNames.contains(dbName));
					mongoInstance.dropDatabase(dbName);
				}

			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(ErrorCodes.DB_DELETION_EXCEPTION, "DB_DELETION_EXCEPTION", m.getCause());

				// Write in logger
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}
	@AfterClass
	public static void destroyMongoProcess() {
		p.destroy();
	}
}