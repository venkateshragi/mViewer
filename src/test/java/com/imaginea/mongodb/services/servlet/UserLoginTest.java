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
import javax.servlet.http.HttpServletRequest;

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
import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Tests the UserLogin Resource functionality. Here we will try to register a
 * user and will check if a valid token Id is provided and then will check if a
 * Mongo Instance is also added corresponding to that tokenId.
 * 
 * @author Rachit Mittal
 * @since 15 July 2011
 * 
 */
public class UserLoginTest extends BaseRequestDispatcher {

	/**
	 * Instance variable used to get a mongo instance after binding to an
	 * implementation.
	 */
	private MongoInstanceProvider mongoInstanceProvider;
	/**
	 * Mongo Instance to communicate with mongo
	 */
	private Mongo mongoInstance;

	/**
	 * Class to be tested
	 */
	private UserLogin testLoginResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(UserLoginTest.class);
	/**
	 * test username and password
	 */
	private String testUsername = "name";
	private String testPassword = "pass";

	/**
	 * Default constructor binds mongo instance provider to config mongo
	 * instance provider that returns according to parameters given in config
	 * file mongo.config
	 */

	public UserLoginTest() throws MongoHostUnknownException, IOException,
			FileNotFoundException, JSONException {
		try {

			mongoInstanceProvider = new ConfigMongoInstanceProvider();

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
		}

	}

	/**
	 * Instantiates the class UserLogin which is to be tested and also gets a
	 * mongo instance from mongo instance provider.
	 */
	@Before
	public void setUpTestEnvironment() {
		testLoginResource = new UserLogin();
		mongoInstance = mongoInstanceProvider.getMongoInstance();
	}

	/**
	 * Test Post login Request made by User to UserLogin resource for
	 * authentication. Hereby we will check if a token ID is generated and also
	 * will check if a mongoInstacne is set by the servlet.
	 */

	@Test
	public void testUserLoginRequest() {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Post Request for User Login ["
					+ DateProvider.getDateTime() + "]");
			logger.info("Insert User in admin table");
		}
		try {
			mongoInstance.getDB("admin").addUser(testUsername,
					testPassword.toCharArray());
			String host = mongoInstance.getAddress().getHost();
			Integer port = (Integer) (mongoInstance.getAddress().getPort());
			HttpServletRequest request = new MockHttpServletRequest();
			String resp = testLoginResource.authenticateUser(testUsername,
					testPassword, host, port.toString(), request);

			if (logger.isInfoEnabled()) {
				logger.info("Response: " + resp);
			}

			BasicDBObject dbObject = (BasicDBObject) JSON
					.parse(resp);
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
			if (logger.isInfoEnabled()) {
				logger.info("Test Completed  [" + DateProvider.getDateTime()
						+ "]");
			}

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
