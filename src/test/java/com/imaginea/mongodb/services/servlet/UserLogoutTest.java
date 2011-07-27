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
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Tests the UserLogout Servlet functionality. Here we will register a user
 * first and then will check if logout invalidates a user's tokenId.
 *
 */
public class UserLogoutTest {

	/**
	 * Class to be tested
	 */
	private UserLogout testClassObj;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(UserLoginTest.class);

	private String testTokenId = "123212178917845678910910";
	private String testUserMapping = "user";

	/**
	 * Configure Logger.
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws JSONException
	 */

	public UserLogoutTest() throws IOException, FileNotFoundException,
			JSONException {
		super();
		try {
			// TODO Configure by file
			SimpleLayout layout = new SimpleLayout();
			FileAppender appender = new FileAppender(layout,
					"logs/LogoutTestLogs.txt", true);

			logger.setLevel(Level.INFO);
			logger.addAppender(appender);

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
	 * Sets up the test enviornment
	 *
	 * @throws Exception
	 */
	@Before
	public void setUpTestEnvironment() throws IOException, JSONException {
		try {
			testClassObj = new UserLogout();

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
	 * Test GET Request made by User to UserLogout.
	 *
	 * @throws MongoHostUnknownException
	 */
	@Test
	public void testUserLoginRequest() throws JSONException,
			MongoHostUnknownException {
		logger.info("Testing Get Request for User Logout");

		try {
			logger.info(" Insert a testTokenId in user mapping table");
			UserLogin.tokenIDToUserMapping.put(testTokenId, testUserMapping);
			logger.info(" Insert a Mongo instance for this  user in mapping table");
			MongoInstanceProvider m = new ConfigMongoInstanceProvider();
			Mongo mongoInstance = m.getMongoInstance();
			UserLogin.userToMongoInstanceMapping.put(testUserMapping,
					mongoInstance);

			// Dummy request create.
			String URI = "http://localhost:8080/MongoViewer/logout";
			logger.info("URL: " + URI);
			MockHttpServletRequest request = new MockHttpServletRequest("GET",
					URI);
			request.addParameter("tokenId", testTokenId);
			MockHttpSession session = new MockHttpSession();
			session.setAttribute("tokenId", testTokenId);
			request.setSession(session);

			MockHttpServletResponse response = new MockHttpServletResponse();

			testClassObj.doGet(request, response);
			logger.info("After Logout mapping Value of Token Id: "
					+ UserLogin.tokenIDToUserMapping.get(testTokenId));
			assertNull(UserLogin.tokenIDToUserMapping.get(testTokenId));

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
