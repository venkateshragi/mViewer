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
 

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test; 

import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider; 
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
	private static Mongo mongoInstance;

	/**
	 * Class to be tested
	 */
	private UserLogout testLogoutResource;

	/**
	 * Logger object
	 */
	private static Logger logger = Logger.getLogger(UserLoginTest.class);

	private String testdbInfo = "localhost_27017"; 
	private static final String logConfigFile = "src/main/resources/log4j.properties";

	public UserLogoutTest() throws Exception {
		ErrorTemplate.execute(logger, new ResponseCallback() {
			public Object execute() throws Exception {
				mongoInstanceProvider = new ConfigMongoInstanceProvider();
				PropertyConfigurator.configure(logConfigFile);  
				return null;
			}
		});

	}

	/**
	 * Instantiates the class UserLogin which is to be tested and also gets a
	 * mongo instance from mongo instance provider.
	 */
	@Before
	public void instantiateTestClass() {
		testLogoutResource = new UserLogout();
		mongoInstance = mongoInstanceProvider.getMongoInstance();
		// Add User to maps
		UserLogin.mongoConfigToInstanceMapping.put(testdbInfo, mongoInstance);
		UserLogin.mongoConfigToUsersMapping.put(testdbInfo, 1);
	}

	/**
	 * Test GET Request made by User to logout from the application.
	 * 
	 */
	@Test
	public void testUserLoginRequest() {

		testLogoutResource.doGet(testdbInfo);
		assertNotNull(UserLogin.mongoConfigToInstanceMapping.get(testdbInfo));
		Integer value = 0;
		assertEquals(value, UserLogin.mongoConfigToUsersMapping.get(testdbInfo)); 
	}

	@After
	public void destroyMongoProcess() {
		mongoInstance.close();
	}
}
