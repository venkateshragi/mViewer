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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.imaginea.mongodb.common.exceptions.InvalidHTTPRequestException;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;

/**
 * Defines resources for performing create/drop operations on databases present
 * in Mongo we are currently connected to. Also provide resources to get list of
 * all databases present in mongo and also statistics of a particular database.
 * <p>
 * These resources map different HTTP equests made by the client to access these
 * resources to services file which performs these operations. The resources
 * also form a JSON response using the output recieved from the serives files.
 * GET and POST request resources for databases are defined here. For PUT and
 * DELETE functionality , a POST request with an action parameter taking values
 * PUT and DELETE is made.
 * 
 * @author Rachit Mittal
 * @since 2 July 2011
 */
@Path("/db")
public class DatabaseRequestDispatcher extends BaseRequestDispatcher {
	private final static Logger logger = Logger.getLogger(DatabaseRequestDispatcher.class);

	/**
	 * Maps GET Request to get list of databases present in mongo db to a
	 * service function that returns the list. Also forms the JSON response for
	 * this request and sent it to client. In case of any exception from the
	 * service files an error object if formed.
	 * 
	 * @param dbInfo
	 *            Mongo Db Configuration provided by user to connect to.
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @return String of JSON Format with list of all Databases.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getDbList(@QueryParam("dbInfo") final String dbInfo, @Context final HttpServletRequest request) {

		String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
			public Object execute() throws Exception {
				// TODO Using Service Provider
				DatabaseService databaseService = new DatabaseServiceImpl(dbInfo);
				List<String> dbNames = databaseService.getDbList();
				return dbNames;
			}
		});
		return response;
	}

	/**
	 * Maps POST Request to perform create/drop operations on databases present
	 * in mongo db to a service function that returns the list. Also forms the
	 * JSON response for this request and sent it to client. In case of any
	 * exception from the service files an error object if formed.
	 * 
	 * @param dbName
	 *            Name of Database for which to perform create/drop operation
	 *            depending on action patameter
	 * @param action
	 *            Query Paramater with value PUT for identifying a create
	 *            database request and value DELETE for dropping a database.
	 * @param request
	 *            Get the HTTP request context to extract session parameters
	 * @param dbInfo
	 *            Mongo Db Configuration provided by user to connect to.
	 * @return : String with status of operation performed.
	 */
	@POST
	@Path("/{dbName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postDbRequest(@PathParam("dbName") final String dbName, @QueryParam("action") final String action, @QueryParam("dbInfo") final String dbInfo,
			@Context final HttpServletRequest request) {

		if (action == null) {
			InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.ACTION_PARAMETER_ABSENT, "ACTION_PARAMETER_ABSENT");
			return formErrorResponse(logger, e);
		}
		String response = new ResponseTemplate().execute(logger, dbInfo, request, new ResponseCallback() {
			public Object execute() throws Exception {
				DatabaseService databaseService = new DatabaseServiceImpl(dbInfo);
				String status = null;
				if ("PUT".equals(action)) {
					status = databaseService.createDb(dbName);
				} else if ("DELETE".equals(action)) {
					status = databaseService.dropDb(dbName);
				}
				return status;
			}
		});
		return response;
	}
}
