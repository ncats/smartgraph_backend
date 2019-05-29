/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLDBConnector extends DBConnector{

		// Ref: https://jdbc.postgresql.org/documentation/head/connect.html
		// Ref: https://arstechnica.com/civis/viewtopic.php?t=751834
		// Ref: https://serverfault.com/questions/106168/cant-connect-to-postgresql-on-virtualbox-guest
		// Ref: https://networkengineering.stackexchange.com/questions/7106/how-do-you-calculate-the-prefix-network-subnet-and-host-numbers
		// Ref: http://community.jaspersoft.com/blog/configure-postgresql-remote-connections
		// Ref: https://devops.profitbricks.com/tutorials/install-postgresql-on-centos-7/
	

		public PostgreSQLDBConnector (String fName) {

			super(fName);
			
			// Ref: Discussion with Trung
			// Ref: https://jdbc.postgresql.org/documentation/84/load.html
			
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		
		public void connect () throws SQLException {
			 conn = DriverManager.getConnection(getURL());
		}
		

		
		

			
		
	
		@Override
		public void assembleURL () {
			String u = "jdbc:postgresql://";

			u += host + ":";
			u += port + "/";
			u += database + "?";
			u += "user=" + user + "&";
			u += "password=" + password; 
			u += "&ssl=false";

			setURL (u);
			
		}


		@Override
		public void clearAllData() throws SQLException {
			// TODO Auto-generated method stub
			
		}
		
}
