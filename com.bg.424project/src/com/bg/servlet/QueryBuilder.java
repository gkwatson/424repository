package com.bg.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bg.PostgreSQLJDBC;

/**
 * Servlet implementation class QueryBuilder
 */
@WebServlet("/QueryBuilder")
public class QueryBuilder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private PostgreSQLJDBC db;
	
	@Override
	protected void doGet(HttpServletRequest request,
	                HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		
		//Print results of query
		try {
			String quer = db.findCand();
			out.println(quer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void init() throws ServletException {
		db = new PostgreSQLJDBC();
		try {
			db.connect();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void destroy() {
		super.destroy();
		try {
			db.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}