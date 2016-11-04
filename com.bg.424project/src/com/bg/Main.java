package com.bg;

//Test File
public class Main {
	public static void main( String args[] ) throws Exception {
		PostgreSQLJDBC db = new PostgreSQLJDBC();
		db.connect();
		db.insert();
	}
}
