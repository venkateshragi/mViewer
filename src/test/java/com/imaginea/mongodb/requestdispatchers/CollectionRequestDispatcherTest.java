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
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.UserLogin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the collection request dispatcher resource that handles the GET and
 * POST request for Collections present in Mongo. Tests the get and post
 * functions mentioned in the resource with some dummy request and test
 * collection and database names and check the functionality.
 * 
 * 
 * @author Rachit Mittal
 * @since 15 July 2011
 * 
 */
public class CollectionRequestDispatcherTest extends BaseRequestDispatcher {

	private MongoInstanceProvider mongoInstanceProvider;
	private static  Mongo mongoInstance;
	/**
	 * Object of class to be tested
	 */
	private CollectionRequestDispatcher testCollResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(CollectionRequestDispatcherTest.class);

	/**
	 * A test token Id
	 */
	private String testDbInfo = "localhost_";
	private static final String logConfigFile = "src/main/resources/log4j.properties";
 
	
	
	/**
	 * Constructs a mongoInstanceProvider Object.
	 * 
	 * @throws MongoHostUnknownException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public CollectionRequestDispatcherTest() throws Exception {

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
		// Class to be tested
		testCollResource = new CollectionRequestDispatcher();
		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String user = "username_" + mongoInstance.getAddress() + "_" + mongoInstance.getConnectPoint();
		UserLogin.tokenIDToUserMapping.put(testTokenId, user);
		UserLogin.userToMongoInstanceMapping.put(user, mongoInstance);

	}

	/**
	 * Tests the GET Request which gets names of all collections present in
	 * Mongo. Here we construct the test collection first and will test if this
	 * created collection is present in the response of the GET Request made.
	 * 
	 * @throws CollectionException
	 * 
	 * 
	 */

	@Test
	public void getCollList() throws CollectionException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		List<String> testCollNames = new ArrayList<String>();
		testCollNames.add("foo");
		testCollNames.add("");
		if (logger.isInfoEnabled()) {
			logger.info("Testing GET Coll Resource");
		}
		for (String dbName : testDbNames) {
			for (String collName : testCollNames) {
				if (logger.isInfoEnabled()) {
					logger.info("Test Case  [" + dbName + "] and collection Name [" + collName + "]");
					logger.info("Create collection inside Db [" + dbName + "]");
				}
				try {
					if (dbName != null && collName != null) {
						if (!dbName.equals("") && !collName.equals("")) {
							if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
								DBObject options = new BasicDBObject();
								mongoInstance.getDB(dbName).createCollection(collName, options);
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
					String collList = testCollResource.getCollList(dbName, testTokenId, request);

					// response has a JSON Object with result as key and value
					// as
					DBObject response = (BasicDBObject) JSON.parse(collList);
					if (logger.isInfoEnabled()) {
						logger.info("Response : [" + collList + "]");
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
						// DB exists
						DBObject result = (BasicDBObject) response.get("response");
						BasicDBList collNames = ((BasicDBList) result.get("result"));
						if (collName == null) {
							assert (!collNames.contains(collName));
						} else if (collName.equals("")) {
							assert (!collNames.contains(collName));
						} else {
							assert (collNames.contains(collName));
							mongoInstance.dropDatabase(dbName);
						}
					}
				} catch (MongoException m) {
					CollectionException e = new CollectionException(ErrorCodes.GET_COLLECTION_LIST_EXCEPTION, "GET_COLLECTION_LIST_EXCEPTION",
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
	 * Tests a Create collection POST Request which creates a collection inside
	 * a database in MongoDb.
	 * 
	 * @throws CollectionException
	 * 
	 */

	@Test
	public void createCollection() throws CollectionException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		List<String> testCollNames = new ArrayList<String>();
		testCollNames.add("foo");
		testCollNames.add("");
		testCollNames.add(null);

		if (logger.isInfoEnabled()) {
			logger.info("Testing Create Coll Resource");
		}
		for (String dbName : testDbNames) {
			for (String collName : testCollNames) {

				if (logger.isInfoEnabled()) {
					logger.info("Test Case  [" + dbName + "] and collection Name [" + collName + "]");
					logger.info("Create Db [" + dbName + "] first");
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
						logger.info("Create collection using service");
					}

					MockHttpSession session = new MockHttpSession();
					session.setAttribute("tokenId", testTokenId);

					MockHttpServletRequest request = new MockHttpServletRequest();
					request.setSession(session);

					// if capped = false , size irrelevant
					String collList = testCollResource.postCollRequest(dbName, collName, "off" , 0, 0, "PUT", testTokenId, request);
					DBObject response = (BasicDBObject) JSON.parse(collList);

					if (dbName == null) {
						DBObject error = (BasicDBObject) response.get("response");
						String code = (String) ((BasicDBObject) error.get("error")).get("code");
						assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

					} else if (dbName.equals("")) {
						DBObject error = (BasicDBObject) response.get("response");
						String code = (String) ((BasicDBObject) error.get("error")).get("code");
						assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
					} else {
						// DB exists

						if (collName == null) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);

						} else if (collName.equals("")) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);
						} else {

							Set<String> collNames = mongoInstance.getDB(dbName).getCollectionNames();
							if (logger.isInfoEnabled()) {
								logger.info("GET Collection List [" + collNames + "]");
							}
							assert (collNames.contains(collName));
							mongoInstance.dropDatabase(dbName);
						}
					}
				} catch (MongoException m) {
					CollectionException e = new CollectionException(ErrorCodes.COLLECTION_CREATION_EXCEPTION, "COLLECTION_CREATION_EXCEPTION",
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
	 * Tests a delete collection POST Request which deletes a collection inside
	 * a database in MongoDb.
	 * 
	 * @throws CollectionException
	 * 
	 */

	@Test
	public void deleteCollection() throws CollectionException {

		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		List<String> testCollNames = new ArrayList<String>();
		testCollNames.add("foo");
		testCollNames.add("");
		testCollNames.add(null);

		if (logger.isInfoEnabled()) {
			logger.info("Testing Create Coll Resource");
		}
		for (String dbName : testDbNames) {
			for (String collName : testCollNames) {
				if (logger.isInfoEnabled()) {
					logger.info("Test Case  [" + dbName + "] and collection Name [" + collName + "]");
					logger.info("Create collection inside Db [" + dbName + "]");
				}
				try {
					if (dbName != null && collName != null) {
						if (!dbName.equals("") && !collName.equals("")) {
							if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
								DBObject options = new BasicDBObject();
								mongoInstance.getDB(dbName).createCollection(collName, options);
							}
						}
					}

					if (logger.isInfoEnabled()) {
						logger.info("Delete collection using service");
					}

					MockHttpSession session = new MockHttpSession();
					session.setAttribute("tokenId", testTokenId);

					MockHttpServletRequest request = new MockHttpServletRequest();
					request.setSession(session);

					// if capped = false , size irrelevant
					String collList = testCollResource.postCollRequest(dbName, collName, "off", 0, 0, "DELETE", testTokenId, request);
					DBObject response = (BasicDBObject) JSON.parse(collList);

					if (dbName == null) {
						DBObject error = (BasicDBObject) response.get("response");
						String code = (String) ((BasicDBObject) error.get("error")).get("code");
						assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

					} else if (dbName.equals("")) {
						DBObject error = (BasicDBObject) response.get("response");
						String code = (String) ((BasicDBObject) error.get("error")).get("code");
						assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
					} else {
						// DB exists

						if (collName == null) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);

						} else if (collName.equals("")) {
							DBObject error = (BasicDBObject) response.get("response");
							String code = (String) ((BasicDBObject) error.get("error")).get("code");
							assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);
						} else {

							Set<String> collNames = mongoInstance.getDB(dbName).getCollectionNames();
							logger.info("GET Collection List [" + collNames + "]");

							assert (!collNames.contains(collName));
						}
					}
				} catch (MongoException m) {
					CollectionException e = new CollectionException(ErrorCodes.COLLECTION_DELETION_EXCEPTION, "COLLECTION_DELETION_EXCEPTION",
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

	@AfterClass
	public static void destroyMongoProcess() {
		mongoInstance.close();
	}
}