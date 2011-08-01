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

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin;
import com.imaginea.mongodb.requestdispatchers.UserLogout;
import com.mongodb.Mongo;

/**
 * Tests the GET request made by user to logout from the application. Here we
 * will register a user first and then will check if logout invalidates a user's
 * tokenId.
 * 
 * @author Rachit Mittal
 * @since 15 July 2011
 */
public class UserLogoutTest extends BaseRequestDispatcher {
	private MongoInstanceProvider mongoInstanceProvider;
	private Mongo mongoInstance;

	/**
	 * Class to be tested
	 */
	private UserLogout testLogoutResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(UserLoginTest.class);

	private String testTokenId = "123212178917845678910910";
	private String testUserMapping = "user";

	public UserLogoutTest() throws MongoHostUnknownException, IOException,
			FileNotFoundException, JSONException {
		try {

			mongoInstanceProvider = new ConfigMongoInstanceProvider();
			// Start Mongod
			Runtime run = Runtime.getRuntime();
			Process p = run.exec("c:\\mongo\\bin\\mongod");
			p.destroy();

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
		testLogoutResource = new UserLogout();
		mongoInstance = mongoInstanceProvider.getMongoInstance();
	}

	/**
	 * Test GET Request made by User to logout from the application.
	 * 
	 */
	@Test
	public void testUserLoginRequest() {
		if (logger.isInfoEnabled()) {
			logger.info("Testing Get Request for User Logout");
			logger.info(" Insert a testTokenId in user mapping table");
		}

		UserLogin.tokenIDToUserMapping.put(testTokenId, testUserMapping);
		if (logger.isInfoEnabled()) {
			logger.info(" Insert a Mongo instance for this user in mapping table");
		}

		UserLogin.userToMongoInstanceMapping
				.put(testUserMapping, mongoInstance);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute("tokenId", testTokenId);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		testLogoutResource.doGet(testTokenId, request);

		if (logger.isInfoEnabled()) {
			logger.info("After Logout mapping Value of Token Id: "
					+ UserLogin.tokenIDToUserMapping.get(testTokenId));
		}
		assertNull(UserLogin.tokenIDToUserMapping.get(testTokenId));

		if (logger.isInfoEnabled()) {
			logger.info("Test Completed");
		}

	}
}
