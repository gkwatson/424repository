package com.bg;

public class Main {
	public static void main( String args[] ) throws Exception {
		PostgreSQLJDBC db = new PostgreSQLJDBC();
		db.connect();
		db.insert();
	}
}
