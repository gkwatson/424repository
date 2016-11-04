package com.bg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgreSQLJDBC{
	private Connection c = null;
	
	public void connect() throws Exception{
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager
					.getConnection("jdbc:postgresql://localhost:5432/postgres",
							"postgres", "loveCMSC424");
			c.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}
	
	public void insert() throws Exception{
		Statement stmt = null;
		try {
			
			stmt = c.createStatement();
			String sql = "INSERT INTO CANDIDATE (C_ID, C_NAME) " 
			+ "VALUES (1, 'Paul' );";
			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO CANDIDATE (C_ID, C_NAME) " 
			+ "VALUES (2, 'Hillary' );";
			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO CANDIDATE (C_ID, C_NAME) " 
			+ "VALUES (3, 'John' );";
			stmt.executeUpdate(sql);
			
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		System.out.println("Records created successfully");
	}
	
	public String findCand() throws Exception{
		Statement stmt = null;
		String returnString = "";
		try {
			
			stmt = c.createStatement();
			String sql = "SELECT C_ID, C_NAME FROM CANDIDATE";
			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			while(res.next()){
				//Retrieve by column name
				int c_id  = res.getInt("c_id");
				String c_name = res.getString("c_name");
				
				//Format
				returnString += "ID: " + c_id + ", ";
				returnString += "NAME: " + c_name + "\n";
			}
	        returnString += "\n";
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public void disconnect() throws Exception {
		try{
			c.close();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
	}
}