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
		
		String queryName = request.getParameter("queryName");
		String extra = request.getParameter("extra");
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		//Print results of query
		try {
			String quer = "";
			
			if(queryName.equals("candidate")){
				quer = db.candQuery(extra);
			}
			
			else if(queryName.equals("electionYear")){
				quer = db.electionYearQuery(extra);
			}
			
			else if(queryName.equals("reelection")){
				quer = db.reelectionQuery();
			}
			
			else if(queryName.equals("swingCandidate")){
				quer = db.swingCandQuery();
			}
			
			else if(queryName.equals("party")){
				quer = db.partyQuery(extra);
			}
			
			else if(queryName.equals("pollAccuracy")){
				quer = db.pollAccuracy(extra);
			}
			
			else if(queryName.equals("swingState")){
				quer = db.swingStateQuery(extra);
			}
			
			else if(queryName.equals("electoralVote")){
				quer = db.electoralVotesQuery(extra);
			}
			
			else if(queryName.equals("popularVote")){
				quer = db.popularVoteQuery();
			}

			out.println("<div>" + quer + "</div>");
			
			//Return to Welcome Page Link
			out.println("<br><br><a href=\"WelcomePage.jsp\">Back to Query Select</a>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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