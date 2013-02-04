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
package com.imaginea.mongodb.controllers;

import com.imaginea.mongodb.exceptions.ErrorCodes;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Servlet to perform troubleshoot operations. Hereby user can change
 * logger level and can report logs to us in case of any problems.
 */

public class TroubleShootController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Map<String, Level> logLevels = new HashMap<String, Level>();

    public TroubleShootController() {
        super();
        logLevels.put("Fatal", Level.FATAL);
        logLevels.put("Error", Level.ERROR);
        logLevels.put("Warn", Level.WARN);
        logLevels.put("Info", Level.INFO);
        logLevels.put("Debug", Level.DEBUG);

    }

    /**
     * Handles a GET Request at path mViewer/admin for changing the logger level
     *
     * @param request  Request made by user
     * @param response
     * @throws ServletException ,IOException,IllegalArgumentException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IllegalArgumentException {

        response.setContentType("application/x-json");
        PrintWriter out = response.getWriter();
        JSONObject respObj = new JSONObject();

        String loggerLevel = request.getParameter("level");

        try {

            if (!logLevels.containsKey(loggerLevel)) {
                JSONObject error = new JSONObject();
                error.put("code", ErrorCodes.LOGGING_LEVEL_UNDEFINED);
                error.put("message", "Undefined Logging level");
                JSONObject temp = new JSONObject();
                temp.put("error", error);
                respObj.put("response", temp);

            } else {

                Level newLevel = logLevels.get(loggerLevel);

                Logger rootLogger = LogManager.getRootLogger(); // To get the
                // Root Logger

                String oldLevel = rootLogger.getEffectiveLevel().toString();
                rootLogger.setLevel(newLevel);

                JSONObject temp = new JSONObject();
                temp.put("result", "Logger Level Changed from " + oldLevel + " to " + rootLogger.getLevel());
                respObj.put("response", temp);
            }
            out.write(respObj.toString());
            out.close();
        } catch (JSONException e) {
            throw new ServletException("Error forming JSON Object in Servlet", e.getCause());
        }

    }

}
