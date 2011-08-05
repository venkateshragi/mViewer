/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd.
 * Hyderabad, India
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imaginea.mongodb.requestdispatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.imaginea.mongodb.common.exceptions.ApplicationException;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Authenticates User to Mongo Db by checking the user in <system.users>
 * collection of admin database.
 * <p>
 * Here we also create a map of a mongo configuration which is mongo host and
 * mongoPort provided by user to a mongo Instance. This mongo Instance is used
 * for requests made to this database configuration. Also stores a map of active
 * users on a given mongo configuration for closing the mongo instance at time
 * of logout.
 * 
 * 
 * @author Rachit Mittal
 * @since 10 July 2011
 * 
 */

@Path("/login")
public class UserLogin extends BaseRequestDispatcher {
	private static final long serialVersionUID = 1L;

	public static Map<String, Mongo> mongoConfigToInstanceMapping = new HashMap<String, Mongo>();
	public static Map<String, Integer> mongoConfigToUsersMapping = new HashMap<String, Integer>();
	private static Logger logger = Logger.getLogger(UserLogin.class);
 
	/**
	 * Authenticates User by verifying Mongo config details against admin
	 * database and authenticating user to that Db. A facility for guest login
	 * is also allowed when both fields username and password are empty.
	 * <p>
	 * Also stores a mongo instance based on database configuration.
	 * 
	 * @param request
	 *            Request made by user for authentication
	 * @param username
	 *            Name of user as in admin database in mongo
	 * @param password
	 *            password of user as in admin database in mongo
	 * @param mongoHost
	 *            mongo host to connect to
	 * @param mongoPort
	 *            mongo Port to connect to
	 * 	 
	 * 
	 * 
	 */

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String authenticateUser(@FormParam("username") String user, @FormParam("password") final String password, @FormParam("host") String host, @FormParam("port") final String mongoPort,
			@Context final HttpServletRequest request) {

		// Reassign username for guest user in case of empty username and
		// password fields
		if ("".equals(user) && "".equals(password)) {
			user = "guest";
		}
		if ("127.0.0.1".equals(host)) {
			host = "localhost"; // 1 key gor both in map
		}
		final String mongoHost = host;
		final String username = user;
		String response = ErrorTemplate.execute(logger, new ResponseCallback() {
			public Object execute() throws Exception {
				if ("".equals(mongoHost) || "".equals(mongoPort)) {
					ApplicationException e = new ApplicationException(ErrorCodes.MISSING_LOGIN_FIELDS, "Missing Login Fields");
					return formErrorResponse(logger, e);
				}
				Mongo m = new Mongo(mongoHost, Integer.parseInt(mongoPort));
				boolean loginStatus = false;
				if ("guest".equals(username) && "".equals(password)) {
					loginStatus = true;
				} else {
					// Authorize User using <admin> Db
					DB db = m.getDB("admin");
					loginStatus = db.authenticate(username, password.toCharArray());
				}
				if (!loginStatus) {
					ApplicationException e = new ApplicationException(ErrorCodes.INVALID_USERNAME, "Invalid UserName or Password");
					return formErrorResponse(logger, e);
				}
				// Add mongo Configuration Key to key dbInfo in session
				String mongoConfigKey = mongoHost + "_" + mongoPort;
				HttpSession session = request.getSession();

				Object dbInfo = session.getAttribute("dbInfo");
				if (dbInfo == null) {
					List<String> mongosInSession = new ArrayList<String>();
					mongosInSession.add(mongoConfigKey);
					session.setAttribute("dbInfo", mongosInSession);
				} else {
					@SuppressWarnings("unchecked")
					List<String> mongosInSession = (List<String>) dbInfo;
					mongosInSession.add(mongoConfigKey);
					session.setAttribute("dbInfo", mongosInSession);
				}

				// Store a MongoInstance
				if (!mongoConfigToInstanceMapping.containsKey(mongoConfigKey)) {
					Mongo mongoInstance = new Mongo(mongoHost, Integer.parseInt(mongoPort));
					mongoConfigToInstanceMapping.put(mongoConfigKey, mongoInstance);
				}
				Object usersPresent = mongoConfigToUsersMapping.get(mongoConfigKey);
				int noOfUsers = 0;
				if (usersPresent == null) {
					mongoConfigToUsersMapping.put(mongoConfigKey, noOfUsers);
				}
				noOfUsers = mongoConfigToUsersMapping.get(mongoConfigKey) + 1;
				mongoConfigToUsersMapping.put(mongoConfigKey, noOfUsers);

				String status = "Login Success";
				return status;
			}
		});
		return response;
	}
}
