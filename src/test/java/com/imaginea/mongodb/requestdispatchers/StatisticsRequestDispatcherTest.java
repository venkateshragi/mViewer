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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.DatabaseException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.services.servlet.UserLogin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the Database Request Dispatcher Resource that handles the GET and POST
 * request for Databases present in Mongo. Tests the get and post functions
 * metioned in the Resource with some dummy Database names and check the
 * functionality.
 *
 * An ArrayList of various test DbNames possible has been taken and functions
 * are tested for all of them.
 *
 * @author Rachit Mittal
 *
 */
public class StatisticsRequestDispatcherTest {

	private MongoInstanceProvider mongoInstanceBehaviour;
	private Mongo mongoInstance;
	/**
	 * Object of class to be tested
	 */
	private StatisticsRequestDispatcher testStatResource;
	/**
	 * Logger object
	 */
	private static Logger logger = Logger
			.getLogger(StatisticsRequestDispatcherTest.class);

	/**
	 * A test token Id
	 */
	private String testTokenId = "123212178917845678910910";

	/**
	 * Constructs a mongoInstanceProvider Object and configures Logger
	 *
	 * @throws MongoHostUnknownException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public StatisticsRequestDispatcherTest() throws MongoHostUnknownException,
			IOException, FileNotFoundException, JSONException {
		super();

		// TODO Configure by file
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = new FileAppender(layout,
				"logs/mViewer_StatisticsResourceTestLogs.txt", true);

		logger.setLevel(Level.INFO);
		logger.addAppender(appender);

		try {

			mongoInstanceBehaviour = new ConfigMongoInstanceProvider(); // TODO
																		// Beans

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
	public void instantiateTestClass() throws JSONException {
		try {

			// Creates Mongo Instance.
			mongoInstance = mongoInstanceBehaviour.getMongoInstance();
			// Class to be tested
			testStatResource = new StatisticsRequestDispatcher();
			logger.info("Add User to maps in UserLogin servlet");
			String user = "username_" + mongoInstance.getAddress() + "_"
					+ mongoInstance.getConnectPoint();
			UserLogin.tokenIDToUserMapping.put(testTokenId, user);
			UserLogin.userToMongoInstanceMapping.put(user, mongoInstance);

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
	 * Tests the GET Request which gets stats of all databases present in Mongo.
	 * Here we construct an empty database first and will test if number of
	 * collections in that Db are 0.
	 *
	 * @throws JSONException
	 *
	 *
	 */

	@Test
	public void getdbStatsRequest() throws JSONException, MongoException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		logger.info("Testing GetDbStats Resource");
		for (String dbName : testDbNames) {
			logger.info("Test Case  [" + dbName + "]");
			try {

				if (dbName != null) {
					if (!dbName.equals("")) {
						logger.info("Delete this Db first");
						mongoInstance.getDB(dbName).dropDatabase();
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							logger.info("Create a Db first");
							mongoInstance.getDB(dbName).getCollectionNames();
						}
					}
				}

				logger.info("Sends a MockHTTP Request with a Mock Session parameter tokenId");

				MockHttpSession session = new MockHttpSession();
				session.setAttribute("tokenId", testTokenId);

				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setSession(session);

				String resp = testStatResource.getDbStatsRequest(dbName,
						testTokenId,
						request);

				if (dbName == null) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error"))
							.get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

				} else if (dbName.equals("")) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error"))
							.get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
				} else {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject result = (BasicDBObject) response.get("response");

					BasicDBList dbStats = (BasicDBList) result.get("result");

					logger.info("Response : [ " + dbStats + "]");

					for (int i = 0; i < dbStats.size(); i++) {
						BasicDBObject temp = (BasicDBObject) dbStats.get(i);
						if (temp.get("Key").equals("collections")) {
							int noOfCollections = Integer
									.parseInt((String) temp.get("Value"));
							logger.info("Number of Collections : "
									+ noOfCollections);
							assertEquals(0, noOfCollections); // As Empty Db
							break;
						}
					}
					mongoInstance.dropDatabase(dbName);

				}
				logger.info("Test Completed");

			} catch (JSONException e) {

				// log error
				JSONObject error = new JSONObject();
				error.put("message", e.getMessage());
				error.put("code", ErrorCodes.JSON_EXCEPTION);
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				JSONObject response = new JSONObject();
				response.put("response", temp);
				logger.info(response.toString());

				throw e;
			} catch (MongoException m) {
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
			logger.info("Test Completed");
		}
	}

	/**
	 * Tests the GET Request which gets stats of all collections present in
	 * Mongo. Here we construct an empty collection first and will test if
	 * number of documents in that Db are 0.
	 *
	 * @throws JSONException
	 *
	 *
	 */

	@Test
	public void getCollStatsRequest() throws JSONException, MongoException {
		// ArrayList of several test Objects - possible inputs
		List<String> testDbNames = new ArrayList<String>();
		// Add some test Cases.
		testDbNames.add("random");
		testDbNames.add("");
		testDbNames.add(null);

		String testCollName = "test";
		logger.info("Testing GetCollStats Resource");
		for (String dbName : testDbNames) {
			logger.info("Test Case  [" + dbName + "]");
			try {

				if (dbName != null) {
					if (!dbName.equals("")) {
						if (!mongoInstance.getDatabaseNames().contains(dbName)) {
							logger.info("Create a Db first");
							mongoInstance.getDB(dbName).getCollectionNames();

							logger.info("Delete the collection first if exist");
							if (mongoInstance.getDB(dbName)
									.getCollectionNames()
									.contains(testCollName)) {

								mongoInstance.getDB(dbName)
										.getCollection(testCollName).drop();
							}

							logger.info(" Create an empty collection");
							DBObject options = new BasicDBObject();
							mongoInstance.getDB(dbName).createCollection(
									testCollName, options);
						}
					}
				}

				logger.info("Sends a MockHTTP Request with a Mock Session parameter tokenId");

				MockHttpSession session = new MockHttpSession();
				session.setAttribute("tokenId", testTokenId);

				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setSession(session);

				String resp = testStatResource.getCollStatsRequest(dbName,
						testCollName, testTokenId, request);

				if (dbName == null) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error"))
							.get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);

				} else if (dbName.equals("")) {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject error = (BasicDBObject) response.get("response");
					String code = (String) ((BasicDBObject) error.get("error"))
							.get("code");
					assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
				} else {
					DBObject response = (BasicDBObject) JSON.parse(resp);
					DBObject result = (BasicDBObject) response.get("response");
					BasicDBList collStats = (BasicDBList) result.get("result");
					logger.info("Response : [ " + collStats + "]");

					for (int i = 0; i < collStats.size(); i++) {
						BasicDBObject temp = (BasicDBObject) collStats.get(i);
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

				}
				logger.info("Test Completed");

			} catch (JSONException e) {

				// log error
				JSONObject error = new JSONObject();
				error.put("message", e.getMessage());
				error.put("code", ErrorCodes.JSON_EXCEPTION);
				error.put("stackTrace", e.getStackTrace());

				JSONObject temp = new JSONObject();
				temp.put("error", error);
				JSONObject response = new JSONObject();
				response.put("response", temp);
				logger.info(response.toString());

				throw e;
			} catch (MongoException m) {
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
			logger.info("Test Completed");
		}
	}
}