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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException; 
import com.imaginea.mongodb.services.CollectionService;
import com.imaginea.mongodb.services.CollectionServiceImpl;
import com.imaginea.mongodb.services.DatabaseService;
import com.imaginea.mongodb.services.DatabaseServiceImpl;
import com.mongodb.CommandResult;
import com.mongodb.Mongo;

/**
 * Defines resources for getting statistics of mongo Server and statistics of a
 * particualr database present in mongo and statistics of a collection present
 * inside a database.
 * <p>
 * The resources also form a error JSON response when any exception occur while
 * performing the operation.
 * 
 * @author Rachit Mittal
 * @since 7 July 2011
 */
@Path("/stats")
public class StatisticsRequestDispatcher extends BaseRequestDispatcher {
    private final static Logger logger = Logger
            .getLogger(StatisticsRequestDispatcher.class);

    /**
     * Default Constructor
     */
    public StatisticsRequestDispatcher() {
    }

    /**
     * Get Statistics of Mongo Server.
     * 
     * @param dbInfo
     *            Mongo Db Configuration provided by user to connect to.
     * @param request
     *            Get the HTTP request context to extract session parameters
     * @return String of JSON Format with server Stats.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getServerStats(@QueryParam("dbInfo") final String dbInfo,
            @Context final HttpServletRequest request) throws JSONException {

        String response = new ResponseTemplate().execute(logger, dbInfo,
                request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        Mongo mongoInstance = UserLogin.mongoConfigToInstanceMapping
                                .get(dbInfo);
                        // Get Server Stats
                        CommandResult cd = mongoInstance.getDB("admin")
                                .command("serverStatus");
                        return cd;
                    }
                });
        return response;
    }

    /**
     * GET Statistics of a particular database.
     * 
     * @param dbName
     *            : Name of Database for which to get DbStats.
     * @param dbInfo
     *            Mongo Db Configuration provided by user to connect to.
     * @return : String of JSON Format with Db Stats.
     */
    @GET
    @Path("/db/{dbName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDbStats(@PathParam("dbName") final String dbName,
            @QueryParam("dbInfo") final String dbInfo,
            @Context final HttpServletRequest request) throws JSONException {

        String response = new ResponseTemplate().execute(logger, dbInfo,
                request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        DatabaseService databaseService = new DatabaseServiceImpl(
                                dbInfo);
                        JSONArray dbStats = databaseService.getDbStats(dbName);
                        return dbStats;
                    }
                });
        return response;
    }

    /**
     * GET Statistics of Collections in a Database present in mongo.
     * 
     * @param dbName
     *            : Name of Database
     * @param collectionName
     *            : Name of Collection
     * @param request
     *            : Get the HTTP request context to extract session parameters
     * @param dbInfo
     *            Mongo Db Configuration provided by user to connect to.
     * @return : A String of JSON Format with key <result> and value Collection
     *         Stats.
     */
    @GET
    @Path("/db/{dbName}/collection/{collectionName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollStats(@PathParam("dbName") final String dbName,
            @PathParam("collectionName") final String collectionName,
            @QueryParam("dbInfo") final String dbInfo,
            @Context final HttpServletRequest request) throws JSONException {

        String response = new ResponseTemplate().execute(logger, dbInfo,
                request, new ResponseCallback() {
                    public Object execute() throws Exception {

                        CollectionService collectionService = new CollectionServiceImpl(
                                dbInfo);
                        // Get the result;
                        JSONArray collectionStats = collectionService
                                .getCollStats(dbName, collectionName);
                        return collectionStats;
                    }
                });       
        return response;
    }
}
