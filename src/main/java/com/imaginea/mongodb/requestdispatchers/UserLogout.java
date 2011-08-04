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
 
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam; 
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import com.mongodb.Mongo;

/**
 * Listens at a logout Request made by the user and destroys user id from the
 * the mappings in UserLogin class and also from the session. The corresponding
 * mongo instance is also destroyed when all the tokenId corresponding to its
 * user mapping are destroyed.
 * 
 * @author Rachit Mittal
 * @since 11 July 2011
 */

@Path("/logout")
public class UserLogout extends BaseRequestDispatcher {

	private static final long serialVersionUID = 1L;

	/**
	 * Define Logger for this class
	 */
	private static Logger logger = Logger.getLogger(UserLogout.class);

	/**
	 * Listens to a logout reuest made by user to end his session from mViewer.
	 * 
	 * @param request
	 *            Logout Request made bye user with a tokenId as parameter
	 * @param dbInfo
	 *            Mongo Db Configuration provided by user to connect to.
	 * @return Logout status
	 * 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String doGet(@QueryParam("dbInfo") final String dbInfo) {
		String response = ErrorTemplate.execute(logger, new ResponseCallback() {
			public Object execute() throws Exception {
				// Not checking in logout if session has dbInfo or not
				Mongo m = UserLogin.mongoConfigToInstanceMapping.get(dbInfo);
				if (m != null) {
					int noOfActiveUsers = UserLogin.mongoConfigToUsersMapping.get(dbInfo);
					if (noOfActiveUsers == 0) {
						m.close();
						UserLogin.mongoConfigToInstanceMapping.remove(dbInfo);
					} else {
						UserLogin.mongoConfigToUsersMapping.put(dbInfo, noOfActiveUsers - 1);
					}
				}
				String status = "User Logged Out";
				return status;
			}
		});
		return response;
	}
}
