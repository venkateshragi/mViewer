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
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.common.DateProvider;
import com.imaginea.mongodb.common.exceptions.ErrorCodes;
import com.mongodb.Mongo;

/**
 * Listens at a logout Request made by the user and destroys user id from the the mappings in UserLogin class and also
 * from the session. The corresponding mongo instance is also destroyed when all the tokenId corresponding to its user
 * mapping are destroyed.
 * 
 * @author Rachit Mittal
 * @since 11 July 2011
 */
@Path("/logout")
public class UserLogout extends BaseRequestDispatcher {
    /**
     * Define Logger for this class
     */
    private static Logger logger = Logger.getLogger(UserLogout.class);

    /**
     * Listens to a logout reuest made by user to end his session from mViewer.
     * 
     * @param request Logout Request made bye user with a tokenId as parameter
     * @param tokenId tokenId given to user at time of login
     * @return Logout status
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String doGet(@QueryParam("tokenId") String tokenId, @Context HttpServletRequest request) {
        if (logger.isInfoEnabled()) {
            logger.info("New Logout Request [ " + DateProvider.getDateTime());
        }
        String response = null;
        try {
            response = validateTokenId(tokenId, logger, request);
            if (response != null) {
                return response;
            }
            // Remove User for a given Token Id if present
            String userMappingkey = UserLogin.tokenIDToUserMapping.get(tokenId);
            if (userMappingkey == null) {
                return formErrorResponse(logger, "User not mapped to token Id", ErrorCodes.INVALID_USER, null, "FATAL");
            }
            JSONObject resp = new JSONObject();
            JSONObject temp = new JSONObject();
            HttpSession session = request.getSession();
            session.removeAttribute("tokenId");
            String user = UserLogin.tokenIDToUserMapping.get(tokenId);
            UserLogin.tokenIDToUserMapping.remove(tokenId);
            // All tokens finished
            if (!UserLogin.tokenIDToUserMapping.containsValue(user)) {
                // Delete Mongo Instance too
                Mongo m = UserLogin.userToMongoInstanceMapping.get(user);
                m.close();
                UserLogin.userToMongoInstanceMapping.remove(user);
            }
            String status = "User Logged Out";
            temp.put("result", status);
            if (logger.isInfoEnabled()) {
                logger.info(temp + "Details [" + user + "]" + DateProvider.getDateTime());
            }
            resp.put("response", temp);
            response = resp.toString();
        } catch (JSONException e) {
            response = formErrorResponse(logger, e.getMessage(), ErrorCodes.JSON_EXCEPTION, e.getStackTrace(), "ERROR");
        }
        return response;
    }
}
