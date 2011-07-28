package com.imaginea.mongodb.requestdispatchers;

import java.io.IOException;
import javax.servlet.ServletException; 
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Defines a Servlet to perform troubleshoot operations. Hereby user can change
 * logger level and can report logs to us in case of any problems.
 */

public class TroubleShoot extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	public TroubleShoot() {
		super(); 
	}

	/**
	 * Handles a GET Request at path mViewer/admin for changing the logger level
	 * 
	 * @param request  Request made by user
	 * @param response
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		 String loggerLevel = request.getParameter("level");
		 //TODO
	}

	 

}
