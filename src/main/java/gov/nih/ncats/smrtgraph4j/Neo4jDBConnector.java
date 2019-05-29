/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Neo4jDBConnector extends DBConnector{

	// Ref: http://dyanarose.github.io/blog/2014/07/08/preventing-duplication-when-creating-relationships-in-neo4j/
	
	private String url = null;
	
	public Neo4jDBConnector (String fName) {
		super (fName);
		// Ref: Discussion with Trung
		// Ref: https://github.com/neo4j-contrib/neo4j-jdbc/issues/79
		
		try {
			Class.forName("org.neo4j.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		
	}
	
	@Override
	public void clearAllData () throws SQLException {
		// Ref: https://neo4j.com/docs/developer-manual/current/cypher/clauses/delete/#delete-delete-a-node-with-all-its-relationships
		// Ref: http://neo4j.com/docs/developer-manual/current/cypher/clauses/remove/
		String query = "MATCH (n) DETACH DELETE n";
	
		runSimpleQuery(query);
		
		query = "MATCH (n)-[r]-(m) DETACH DELETE r";
		
		runSimpleQuery(query);
		
	
	}
	
	@Override
	public void assembleURL () {
		
		// Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
		// Ref: http://neo4j-contrib.github.io/neo4j-jdbc/
		String u = "jdbc:neo4j:http://";

		u += host + ":";
		u += port;
		//u += "/?" + "user=" + user + ",";
		//u += "password=" + password; 


		//System.out.println(host + " " + port + " " + user + " " + password + " full: " + u);
		
		setURL (u);
		
	}



	
	@Override
	public void connect() throws SQLException {
		// TODO Auto-generated method stub
		// Ref: http://neo4j-contrib.github.io/neo4j-jdbc/
		//conn = DriverManager.getConnection(getURL());
		conn = DriverManager.getConnection(getURL(), user, password);
	}
	
	
}
