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
import com.imaginea.mongodb.common.exceptions.CollectionException;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
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
 * Tests the Statistics request dispatcher resource that provides statistics of
 * Database and Collection.
 * 
 * 
 * @author Rachit Mittal
 * @since 17 Jul 2011
 * 
 */
public class StatisticsRequestDispatcherTest extends BaseRequestDispatcher {

	private MongoInstanceProvider mongoInstanceProvider;
	private Mongo mongoInstance;
	/**
	 * Object of class to be tested
	 */
	private StatisticsRequestDispatcher testStatResource;
	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(StatisticsRequestDispatcherTest.class);

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
	public StatisticsRequestDispatcherTest() throws MongoHostUnknownException, IOException, FileNotFoundException {
		 
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
		testStatResource = new StatisticsRequestDispatcher();
		if (logger.isInfoEnabled()) {
			logger.info("Add User to maps in UserLogin servlet");
		}
		// Add user to mappings in userLogin for authentication
		String user = "username_" + mongoInstance.getAddress() + "_" + mongoInstance.getConnectPoint();
		UserLogin.tokenIDToUserMapping.put(testTokenId, user);
		UserLogin.userToMongoInstanceMapping.put(user, mongoInstance);

	}

	/**
	 * Tests the GET Request which gets stats of all databases present in Mongo.
	 * Here we construct an empty database first and will test if number of
	 * collections in that Db are 0.
	 * 
	 * @throws DatabaseException
	 * 
	 * 
	 */

	@Test
	public void getdbStatsRequest() throws DatabaseException, JSONException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		if (logger.isInfoEnabled()) {
			logger.info("Testing GetDb Stats Resource");
		}
		for (String dbName : testDbNames) {
			if (logger.isInfoEnabled()) {
				logger.info("Test Case  [" + dbName + "]");
			}
			try {

				if (dbName != null) {
					if (!dbName.equals("")) {
						if (logger.isInfoEnabled()) {
							logger.info("Delete this Db first");
						}
						mongoInstance.getDB(dbName).dropDatabase();
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							if (logger.isInfoEnabled()) {
								logger.info("Create a Db first");
							}
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

				String resp = testStatResource.getDbStats(dbName, testTokenId, request);

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
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject result = (BasicDBObject) response.get("response");

					BasicDBList dbStats = (BasicDBList) result.get("result");

					if (logger.isInfoEnabled()) {
						logger.info("Response : [ " + dbStats + "]");
					}

					for (int i = 0; i < dbStats.size(); i++) {
						BasicDBObject temp = (BasicDBObject) dbStats.get(i);
						if (temp.get("Key").equals("collections")) {
							int noOfCollections = Integer.parseInt((String) temp.get("Value"));
							if (logger.isInfoEnabled()) {
								logger.info("Number of Collections : " + noOfCollections);
							}
							assertEquals(0, noOfCollections); // As Empty Db
							break;
						}
					}
					mongoInstance.dropDatabase(dbName);

				}
				if (logger.isInfoEnabled()) {
					logger.info("Test Completed");
				}

			} catch (JSONException e) {

				formErrorResponse(logger, e.getMessage(), ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
				throw e;
			} catch (MongoException m) {
				DatabaseException e = new DatabaseException(ErrorCodes.GET_DB_LIST_EXCEPTION, "GET_DB_LIST_EXCEPTION", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}

	/**
	 * Tests the GET Request which gets stats of all collections present in
	 * Mongo. Here we construct an empty collection first and will test if
	 * number of documents in that collection are 0.
	 * 
	 * @throws CollectionException
	 * 
	 * 
	 */

	@Test
	public void getCollStatsRequest() throws CollectionException, JSONException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		String testCollName = "test";
		if (logger.isInfoEnabled()) {
			logger.info("Testing Get Collection Stats Resource");
		}
		for (String dbName : testDbNames) {
			if (logger.isInfoEnabled()) {
				logger.info("Test Case  [" + dbName + "]");
			}
			try {

				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							if (logger.isInfoEnabled()) {
								logger.info("Create a Db first");
							}
							mongoInstance.getDB(dbName).getCollectionNames();

							if (logger.isInfoEnabled()) {
								logger.info("Delete the collection first if exist");
							}
							if (mongoInstance.getDB(dbName).getCollectionNames().contains(testCollName)) {

								mongoInstance.getDB(dbName).getCollection(testCollName).drop();
							}

							if (logger.isInfoEnabled()) {
								logger.info(" Create an empty collection");
							}
							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(testCollName, options);
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

				String resp = testStatResource.getCollStats(dbName, testCollName, testTokenId, request);

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
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject result = (BasicDBObject) response.get("response");
					BasicDBList collStats = (BasicDBList) result.get("result");
					logger.info("Response : [ " + collStats + "]");

					for (int i = 0; i < collStats.size(); i++) {
						BasicDBObject temp = (BasicDBObject) collStats.get(i);
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

				}
				if (logger.isInfoEnabled()) {
					logger.info("Test Completed");
				}

			} catch (JSONException e) {

				formErrorResponse(logger, e.getMessage(), ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
				throw e;
			} catch (MongoException m) {
				CollectionException e = new CollectionException(ErrorCodes.GET_COLL_STATS_EXCEPTION, "GET_COLL_STATS_EXCEPTION", m.getCause());
				formErrorResponse(logger, e.getMessage(), e.getErrorCode(), e.getStackTrace(), "ERROR");
				throw e;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed");
			}
		}
	}
}