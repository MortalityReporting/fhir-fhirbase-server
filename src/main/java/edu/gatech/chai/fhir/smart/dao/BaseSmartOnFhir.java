package edu.gatech.chai.fhir.smart.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSmartOnFhir {
	final static Logger logger = LoggerFactory.getLogger(BaseSmartOnFhir.class);
	
	public Connection connect() {
		String url = "jdbc:sqlite::resource:smartonfhir.db";
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(url);
			logger.debug("Connected to database");
		} catch (SQLException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}

		return conn;
	}
}
