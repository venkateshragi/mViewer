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
package com.imaginea.mongov.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;

/**
 * @author aditya
 */
@WebServlet(name = "Charts", urlPatterns = { "/Charts", "/Charts/query", "/Charts/initiate" })
public class Charts extends HttpServlet {
    private static final long serialVersionUID = -1539358875210511143L;
    private static JSONArray array = new JSONArray();
    private static int num = 0;
    private static int lastNoOfQueries = 0;
    private static int lastNoOfInserts = 0;
    private static int lastNoOfUpdates = 0;
    private static int lastNoOfDeletes = 0;
    int maxLen = 20;
    int jump = 1;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if (requestUri != null) {
            if (requestUri.substring(requestUri.lastIndexOf('/')).equals("/query")) {
                processQuery(request, response);
            } else if (requestUri.substring(requestUri.lastIndexOf('/')).equals("/initiate")) {
                processInitiate(request, response);
            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * 
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private void processQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/x-json");
        PrintWriter out = response.getWriter();
        try {
            HttpSession session = request.getSession();
            DB db = (DB) session.getAttribute("db");
            CommandResult cr = db.command("serverStatus");
            BasicDBObject obj = (BasicDBObject) cr.get("opcounters");
            int currentValue;
            JSONObject JSONobj = new JSONObject();
            JSONObject temp = new JSONObject();
            // array.remove()
            num = num + jump;
            temp.put("TimeStamp", num);
            currentValue = (Integer) obj.get("query");
            temp.put("QueryValue", currentValue - lastNoOfQueries);
            lastNoOfQueries = currentValue;
            currentValue = (Integer) obj.get("insert");
            temp.put("InsertValue", currentValue - lastNoOfInserts);
            lastNoOfInserts = currentValue;
            currentValue = (Integer) obj.get("update");
            temp.put("UpdateValue", currentValue - lastNoOfUpdates);
            lastNoOfUpdates = currentValue;
            currentValue = (Integer) obj.get("delete");
            temp.put("DeleteValue", currentValue - lastNoOfDeletes);
            lastNoOfDeletes = currentValue;
            if (array.length() == maxLen) {
                JSONArray tempArray = new JSONArray();
                for (int i = 1; i < maxLen; i++) {
                    tempArray.put(array.get(i));
                }
                array = tempArray;
            }
            array.put(temp);
            JSONobj.put("Results", array);
            out.print(JSONobj);
        } catch (JSONException e) {
            out.print("JSON Exception");
        } finally {
            out.close();
        }
    }

    private void processInitiate(HttpServletRequest request, HttpServletResponse response) {
        array = new JSONArray();
        // JSONObject temp = new JSONObject();
        // temp.put("Name", 0);
        // temp.put("Value",0);
        // for(int index=0;index<0;index++){
        // array.put(temp);
        // }
        num = 0;
        try {
            HttpSession session = request.getSession();
            DB db = (DB) session.getAttribute("db");
            CommandResult cr = db.command("serverStatus");
            BasicDBObject obj = (BasicDBObject) cr.get("opcounters");
            lastNoOfQueries = (Integer) obj.get("query");
            lastNoOfInserts = (Integer) obj.get("insert");
            lastNoOfUpdates = (Integer) obj.get("update");
            lastNoOfDeletes = (Integer) obj.get("delete");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
