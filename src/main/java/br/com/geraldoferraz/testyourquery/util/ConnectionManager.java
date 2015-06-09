package br.com.geraldoferraz.testyourquery.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionManager {

	public void executeStatement(String statement) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:ctaTeste", "sa", "");
			pstmt = conn.prepareStatement(statement);
		} finally { 
			if(pstmt != null){
				pstmt.execute();
			}
			if(conn != null){
				conn.close();
			}
		}
	}

}
