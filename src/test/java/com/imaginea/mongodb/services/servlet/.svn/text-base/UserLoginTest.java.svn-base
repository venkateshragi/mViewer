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

package com.imaginea.mongodb.services.servlet;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the UserLogin Servlet functionality. Here we will try to register a
 * user and will check if a valid token Id is provided and then will check if a
 * Mongo Instance is also added corresponding to that tokenId.
 *
 */
public class UserLoginTest {

	/**
	 * Mongo Instance
	 */
	private MongoInstanceProvider mongoInstanceBehaviour;
	private Mongo mongoInstance;
	/**
	 * Class to be tested
	 */
	private UserLogin testClassObj;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(UserLoginTest.class);

	private String testUsername = "name";
	private String testPassword = "pass";

	/**
	 * Configure Logger.
	 */

	public UserLoginTest() throws MongoHostUnknownException, IOException,
			FileNotFoundException, JSONException {
		super();
		try {
			// TODO Configure by file
			SimpleLayout layout = new SimpleLayout();
			FileAppender appender = new FileAppender(layout,
					"logs/LoginTestLogs.txt", true);

			logger.setLevel(Level.INFO);
			logger.addAppender(appender);

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
	 * Sets up the test Envioronment
	 *
	 * @throws Exception
	 */
	@Before
	public void setUpTestEnvironment() throws IOException, JSONException {
		try {
			testClassObj = new UserLogin();
			mongoInstance = mongoInstanceBehaviour.getMongoInstance();

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
	 * Test Post Request made by User to UserLogin servlet for authentication.
	 * Hereby we will check if a token ID is generated and also will check if a
	 * MongoInstacne is set by the servlet.
	 */
	@Test
	public void testUserLoginRequest() throws JSONException {
		logger.info("Testing Post Request for User Login");

		// Insert User in admin table
		try {

			mongoInstance.getDB("admin").addUser(testUsername,
					testPassword.toCharArray());

			// Dummy request create.
			String URI = "http://localhost:8080/MongoViewer/login";
			logger.info("URL : " + URI);

			MockHttpServletRequest request = new MockHttpServletRequest("POST",
					URI);
			String host =mongoInstance.getAddress().getHost();
			Integer port = (Integer) (mongoInstance.getAddress().getPort());
			request.addParameter("username", testUsername);
			request.addParameter("password", testPassword);
			request.addParameter("host", host);
			request.addParameter("port", port.toString());

			MockHttpServletResponse response = new MockHttpServletResponse();

			testClassObj.doPost(request, response);
			logger.info("Response: " + response.getContentAsString());

			BasicDBObject dbObject = null;

			dbObject = (BasicDBObject) JSON
					.parse(response.getContentAsString());

			BasicDBObject result = (BasicDBObject) dbObject.get("response");

			BasicDBObject token = (BasicDBObject) result.get("result");

			// Token = null => Test Failure
			assertNotNull(token);


			// Now check if mongo set for this or not.
			if (token != null) {
				String user = UserLogin.tokenIDToUserMapping.get(token
						.get("id"));
				Mongo m = UserLogin.userToMongoInstanceMapping.get(user);
				assertNotNull(m);
			}

			// Remove User

			mongoInstance.getDB("admin").removeUser(testUsername);
			logger.info("Test Completed");

		} catch (MongoException m) {

			JSONObject error = new JSONObject();
			error.put("message", m.getMessage());
			error.put("code", ErrorCodes.DOCUMENT_CREATION_EXCEPTION);
			error.put("stackTrace", m.getStackTrace());

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			JSONObject response = new JSONObject();
			response.put("response", temp);
			logger.info(response.toString());
		} catch (ServletException e) {
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", "SERVLET_EXCEPTION");
			error.put("stackTrace", e.getStackTrace());

			JSONObject temp = new JSONObject();
			temp.put("error", error);
			JSONObject response = new JSONObject();
			response.put("response", temp);
			logger.info(response.toString());
		} catch (UnsupportedEncodingException e) {
			JSONObject error = new JSONObject();
			error.put("message", e.getMessage());
			error.put("code", "UNSUPPORTED_ENCODING_EXCEPTION");
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
}
