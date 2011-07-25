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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * 
 * @author aditya
 */
@WebServlet(name = "DocumentOperations", urlPatterns = {
		"/DocumentOperations/getDocs", "/DocumentOperations/getAllKeys" })
public class DocumentOperations extends HttpServlet {

	private static final long serialVersionUID = 3380246006361470605L;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			JSONException {
		String func = request.getRequestURI();
		if (func != null) {
			if (func.substring(func.lastIndexOf('/')).equals("/getDocs")) {
				getDocs(request, response);
			} else if (func.substring(func.lastIndexOf('/')).equals(
					"/getAllKeys")) {
				getAllKeys(request, response);
			}
		}
	}

	// <editor-fold defaultstate="collapsed"
	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (JSONException ex) {
			Logger.getLogger(DocumentOperations.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (JSONException ex) {
			Logger.getLogger(DocumentOperations.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	}

	/**
	 * Returns a short description of the servlet.
	 * 
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";

	}// </editor-fold>

	private void getDocs(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		try {

			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");
			DB db = (DB) session.getAttribute("db");
			if (m != null && db != null) {
				String type = request.getParameter("type");
				String collName = request.getParameter("coll");
				DBCollection coll = db.getCollection(collName);
				session.setAttribute("collection", coll);
				response.setContentType("application/x-json");
				JSONArray json = new JSONArray();
				JSONObject responseObject = new JSONObject();
				int totalRecords = 0, skip = 0, limit = 0, requestedRecords = Integer
						.parseInt(request.getParameter("results")), returnedRecords = 0;
				DBCursor cur = null;
				if (request.getParameter("queryExec") != null) {
					StringTokenizer strtok = new StringTokenizer(
							request.getParameter("fields"), ",");
					limit = Integer.parseInt(request.getParameter("limit"));
					skip = Integer.parseInt(request.getParameter("skip"));
					DBObject keys = new BasicDBObject();
					while (strtok.hasMoreElements()) {
						keys.put(strtok.nextToken(), 1);
					}
					DBObject queryObject = (DBObject) JSON.parse(request
							.getParameter("query"));
					cur = coll.find(queryObject, keys);
					totalRecords = cur.count();
					cur.limit(limit);
					cur.skip(skip);

				} else {
					cur = coll.find();
					totalRecords = cur.count();
					skip = Integer.parseInt(request.getParameter("startIndex"));
					cur.skip(skip);

				}
				if (requestedRecords > totalRecords) {
					returnedRecords = totalRecords;
				} else {
					returnedRecords = requestedRecords;
				}
				while (cur.hasNext()) {
					json.put(cur.next());
				}
				responseObject.put("results", json);
				responseObject.put("total_records", totalRecords);
				responseObject.put("records_returned", returnedRecords);
				responseObject.put("first_index", 0);
				out.print(responseObject);
			}
		} catch (Exception e) {
			out.print(e);
		} finally {
			out.close();
		}
	}

	private void getAllKeys(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/x-json");
		JSONObject responseObject = new JSONObject();
		try {
			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");
			DB db = (DB) session.getAttribute("db");
			if (m != null && db != null) {
				String collName = request.getParameter("coll");
				DBCollection coll = db.getCollection(collName);
				DBObject doc = new BasicDBObject();
				DBCursor cur = coll.find();
				Set<String> completeSet = new TreeSet<String>();
				while (cur.hasNext()) {
					doc = cur.next();
					getNestedKeys(doc, completeSet, "");
				}
				completeSet.remove("_id");
				responseObject.put("Keys", completeSet);
				out.print(responseObject);

			}
		} catch (Exception e) {
			out.print(e);
		} finally {
			out.close();
		}
	}

	private void getNestedKeys(DBObject doc, Set<String> completeSet,
			String prefix) {
		Set<String> allKeys = doc.keySet();
		Iterator<String> it = allKeys.iterator();
		while (it.hasNext()) {
			String temp = it.next();
			completeSet.add(prefix + temp);
			if (doc.get(temp) instanceof BasicDBObject) {
				getNestedKeys((DBObject) doc.get(temp), completeSet, prefix
						+ temp + ".");
			}
		}
	}
}
