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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import com.imaginea.mongodb.common.ConfigMongoInstanceProvider;
import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.MongoInstanceProvider;
import com.imaginea.mongodb.common.exceptions.ApplicationException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.MongoHostUnknownException;
import com.imaginea.mongodb.requestdispatchers.BaseRequestDispatcher;
import com.imaginea.mongodb.requestdispatchers.UserLogin; 
import com.mongodb.Mongo;
import com.mongodb.MongoException; 

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

    private MongoInstanceProvider mongoInstanceProvider;
    private static Mongo mongoInstance;

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
    private static final String logConfigFile = "src/main/resources/log4j.properties";

    /**
     * Default constructor binds mongo instance provider to config mongo
     * instance provider that returns according to parameters given in config
     * file mongo.config
     */

    public UserLoginTest() throws Exception {
        try {

            mongoInstanceProvider = new ConfigMongoInstanceProvider();
            PropertyConfigurator.configure(logConfigFile);

        } catch (FileNotFoundException m) {
            ApplicationException e = new ApplicationException(
                    ErrorCodes.FILE_NOT_FOUND_EXCEPTION, m.getMessage(),
                    m.getCause());
            formErrorResponse(logger, e);
            throw e;
        } catch (MongoHostUnknownException e) {
            formErrorResponse(logger, e);
            throw e;

        } catch (IOException m) {
            ApplicationException e = new ApplicationException(
                    ErrorCodes.IO_EXCEPTION, m.getMessage(), m.getCause());
            formErrorResponse(logger, e);
            throw e;
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
     * 
     * @throws MongoHostUnknownException
     */

    @Test
    public void testUserLoginRequest() throws MongoHostUnknownException {
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
            String dbInfo = host + "_" + port;
            Mongo m = UserLogin.mongoConfigToInstanceMapping.get(dbInfo);
            assertNotNull(m);

            if (logger.isInfoEnabled()) {
                logger.info("Test Completed  [" + DateProvider.getDateTime()
                        + "]");
            }

        } catch (MongoException m) {
            MongoHostUnknownException e = new MongoHostUnknownException(
                    m.getMessage(), m);
            formErrorResponse(logger, e);
            throw e;
        }
    }

    @After
    public void destroyMongoProcess() {
        mongoInstance.close();
    }
}
