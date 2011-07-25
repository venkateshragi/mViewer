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
@WebServlet(name = "CollectionOperations", urlPatterns = {
		"/CollectionOperations/coll", "/CollectionOperations/collStats",
		"/CollectionOperations/addColl", "/CollectionOperations/dropColl" })
public class CollectionOperations extends HttpServlet {

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
			if (func.substring(func.lastIndexOf('/')).equals("/coll")) {
				getCollectionNames(request, response);
			} else if (func.substring(func.lastIndexOf('/')).equals(
					"/collStats")) {
				getCollStats(request, response);
			} else if (func.substring(func.lastIndexOf('/')).equals("/addColl")) {
				addColl(request, response);
			} else if (func.substring(func.lastIndexOf('/'))
					.equals("/dropColl")) {
				dropColl(request, response);
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
			Logger.getLogger(CollectionOperations.class.getName()).log(Level.SEVERE,
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
			Logger.getLogger(CollectionOperations.class.getName()).log(Level.SEVERE,
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

	private void dropColl(HttpServletRequest request,
			HttpServletResponse response) throws JSONException, IOException {
		response.setContentType("application/x-json");
		PrintWriter out = response.getWriter();
		JSONObject responseObj = new JSONObject();
		try {
			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");
			DB db = m.getDB(request.getParameter("dbName"));
			DBCollection coll = db.getCollection(request
					.getParameter("collName"));
			coll.drop();
			responseObj.put("result", "success");
			responseObj.put("db", db.getName());
			out.print(responseObj);
		} catch (Exception e) {
			responseObj.put("result", e);
			out.print(responseObj);
		} finally {
			out.close();
		}
	}

	private void addColl(HttpServletRequest request,
			HttpServletResponse response) throws IOException, JSONException {
		response.setContentType("application/x-json");
		PrintWriter out = response.getWriter();
		JSONObject responseObj = new JSONObject();
		try {
			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");
			DB db = (DB) session.getAttribute("db");
			Map<String, Object> options = new HashMap<String, Object>();
			if (request.getParameter("isCapped") != null) {
				options.put("capped", true);
			}
			if (request.getParameter("collSize") != null
					&& Integer.parseInt(request.getParameter("collSize")) != 0) {
				options.put("size",
						Integer.parseInt(request.getParameter("collSize")));
			}
			if (request.getParameter("collMaxSize") != null
					&& Integer.parseInt(request.getParameter("collMaxSize")) != 0) {
				options.put("max",
						Integer.parseInt(request.getParameter("collMaxSize")));
			}
			db.createCollection(request.getParameter("name"),
					new BasicDBObject(options));
			responseObj.put("result", "success");
			responseObj.put("db", db.getName());
			out.print(responseObj);

		} catch (Exception e) {
			responseObj.put("result", e);
			out.print(responseObj);
		} finally {
			out.close();
		}
	}

	private void getCollectionNames(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("application/x-json");
		PrintWriter out = response.getWriter();

		try {
			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");
			String dbName = request.getParameter("dbName");
			if (m != null) {
				DB db = m.getDB(dbName);
				session.setAttribute("db", db);
				Set<String> colls = db.getCollectionNames();
				JSONObject collJSON = new JSONObject();
				collJSON.put("Name", colls);
				out.print(collJSON);

			}
		} catch (JSONException e) {
			out.print("JSON Exception");
		} finally {
			out.close();
		}
	}

	private void getCollStats(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		response.setContentType("application/x-json");
		PrintWriter out = response.getWriter();
		try {
			HttpSession session = request.getSession();
			Mongo m = (Mongo) session.getAttribute("mongo");

			if (m != null) {
				DB db = m.getDB(request.getParameter("dbName"));
				DBCollection coll = db.getCollection(request
						.getParameter("collName"));
				CommandResult stats = coll.getStats();
				Set<String> keys = stats.keySet();
				Iterator it = keys.iterator();
				JSONObject JSONobj = new JSONObject();
				JSONObject temp = new JSONObject();
				JSONArray arr = new JSONArray();
				while (it.hasNext()) {
					temp = new JSONObject();
					String key = it.next().toString();
					temp.put("Key", key);
					String value = stats.get(key).toString();
					temp.put("Value", value);
					String type = stats.get(key).getClass().toString();
					temp.put("Type", type.substring(type.lastIndexOf('.') + 1));
					arr.put(temp);
				}
				JSONobj.put("Result", arr);
				// Object call=request.getAttribute("callback");
				// out.print(call);
				out.print(JSONobj);

			}
		} catch (JSONException e) {
			out.print("JSON Exception");
		} finally {
			out.close();
		}
	}

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
}
