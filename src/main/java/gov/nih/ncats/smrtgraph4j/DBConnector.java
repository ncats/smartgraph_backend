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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class DBConnector {

	private String dbConfigFileName = null;

	private String URL = null;
	
	protected String host = null;
	protected String port = null;
	protected String database = null;
	protected String user = null;
	protected String password = null;
	
	
	
    protected Connection conn = null;
	
    
    
	public DBConnector (String fName) {
		setDbConfigFileName(fName);
		parseDBConnectionParameters();
	}
	
	
	
	
	abstract public void connect () throws SQLException;
	
	public void disconnect () throws SQLException {
		 conn.close();
	}
	
	
	public void test () throws SQLException {
		
		
		// Ref: // Ref: https://docs.oracle.com/cd/B28359_01/java.111/b31224/getsta.htm
        DatabaseMetaData meta = conn.getMetaData();

        
        
        // gets driver info:
        System.out.println("JDBC driver version is " + meta.getDriverVersion());
        
	}
	
	
	private void parseDBConnectionParameters () {
		String line = null;
		int lineNr = 0;
		

		

		
		try {
			BufferedReader br = new BufferedReader (new FileReader (getDbConfigFileName()));
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (0 == lineNr) setHost(line);
				else if (1 == lineNr) setPort(line);
				else if (2 == lineNr) setDatabase(line);
				else if (3 == lineNr) setUser(line);
				else if (4 == lineNr) setPassword(line);
				else if (lineNr < 0) {
					System.err.println ("[ERROR] Something fishy with database config file : " + getDbConfigFileName() + " . Terminating ...");
					System.exit(-3);
				}
				else {
					System.err.println ("[ERROR] Database config file : " + getDbConfigFileName() + " has more lines than expected. Terminating ...");
					System.exit(-3);
				}
					
				lineNr++;
			}
			br.close();
			

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println ("[ERROR] Database config file " + getDbConfigFileName() + " not found. Terminating...");
			e.printStackTrace();
			System.exit(-2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println ("[ERROR] General I/O error concerning database config file " + getDbConfigFileName() + " . Terminating...");
			e.printStackTrace();
			System.exit(-2);
		}
	}
	
	abstract public void assembleURL ();
	
	
	public String getDbConfigFileName() {
		return dbConfigFileName;
	}
	
	
	private void setDbConfigFileName(String dbConfigFileName) {
		this.dbConfigFileName = dbConfigFileName;
	}
	
	
	public List<String> fetchQueryResults (String query, String fieldsToFetch[]) throws SQLException {

	    List<String> results = new ArrayList<String>();
	    
        // Ref: https://docs.oracle.com/cd/B28359_01/java.111/b31224/getsta.htm
        
        // Create a statement
        Statement stmt = conn.createStatement();
    

        //System.out.println(query);
        
        ResultSet rset = stmt.executeQuery(query);
        
        String result = null;
       
	String fieldVal = null;
 
        while (rset.next()) {
        	result = null;
               	for (int i = 0; i < fieldsToFetch.length; i++) {
  
     
			fieldVal = rset.getString(fieldsToFetch[i]);
			
			//System.out.println("FV:" + fieldVal);
			
			if (fieldVal == null) {
				fieldVal = "NA";
			}
			else {
        	        	// Ref: http://stackoverflow.com/questions/2163045/how-to-remove-line-breaks-from-a-file-in-java
                		// Ref: http://stackoverflow.com/questions/6870858/java-how-to-remove-carriage-return-hex-0a-from-string
                		fieldVal = fieldVal.replaceAll("\r\n", " ");
                		fieldVal = fieldVal.replaceAll("\r", " ");
                		fieldVal = fieldVal.replaceAll("\n", " ");
		
				//fieldVal = fieldVal.trim();
			}



	      		if (i == 0) {
        			result = fieldVal;
        		}
        		else {
        			result += "\t" + fieldVal;
        		}
        		 
        	}
        	results.add(result);
        }
        
        //result = result.trim();
        
        
        // close the result set, the statement and the connection

        rset.close();
        stmt.close();
    
        
        return results;		
	}
	
	
    public String fetchSingleQueryResult (String query, String fieldsToFetch[]) throws SQLException {

        
        // Ref: https://docs.oracle.com/cd/B28359_01/java.111/b31224/getsta.htm
        
        // Create a statement
        Statement stmt = conn.createStatement();
    

        //System.out.println(query);
        
        ResultSet rset = stmt.executeQuery(query);
        
        String result = null;
       
        String fieldVal = null;
 
        while (rset.next()) {
            result = null;
                for (int i = 0; i < fieldsToFetch.length; i++) {
  
     
            fieldVal = rset.getString(fieldsToFetch[i]);
            
            //System.out.println("FV:" + fieldVal);
            
            if (fieldVal == null) {
                fieldVal = "";
            }
            else {
                        // Ref: http://stackoverflow.com/questions/2163045/how-to-remove-line-breaks-from-a-file-in-java
                        // Ref: http://stackoverflow.com/questions/6870858/java-how-to-remove-carriage-return-hex-0a-from-string
                        fieldVal = fieldVal.replaceAll("\r\n", " ");
                        fieldVal = fieldVal.replaceAll("\r", " ");
                        fieldVal = fieldVal.replaceAll("\n", " ");
        
                fieldVal = fieldVal.trim();
            }



                if (i == 0) {
                    result = fieldVal;
                }
                else {
                    result += "\t" + fieldVal;
                }
                 
            }
        }
        
        //result = result.trim();
        
        
        // close the result set, the statement and the connection

        rset.close();
        stmt.close();
    
        
        return result;     
    }	
	

	
	public List<String> runQuery (String query, List<String> columnLabels) throws SQLException {
		// Ref: https://jdbc.postgresql.org/documentation/head/query.html
		
		//System.out.println(query);
		
		List<String> results = new ArrayList<String> ();
		
		String oneRow = null;
		
		Statement qStatement = conn.createStatement();
		ResultSet resultRows = qStatement.executeQuery(query);
		
		while (resultRows.next())
		{
			oneRow = null;
		    for (int i = 0; i < columnLabels.size(); i++) {
		    	
		    	if (i == 0) {
		    		oneRow = resultRows.getString(columnLabels.get(i));
		    	}
		    	else {
		    		oneRow += "\t" + resultRows.getString(columnLabels.get(i));
		    	}
		    	

		    	
		    }
		    
	    	results.add(oneRow);
		}
		
		
		
		
		resultRows.close();
		qStatement.close();
		
		return results;
		
	}

	
	abstract public void clearAllData () throws SQLException;

	
	public void runSimpleQuery (String query) throws SQLException {
		// Ref: https://jdbc.postgresql.org/documentation/head/query.html
		
		
		Statement qStatement = conn.createStatement();
		
		//System.out.println(query);
		
		qStatement.executeUpdate(query);
		qStatement.close();
		
		
	}   

	
	
	
	protected void setURL (String url) {
		this.URL = url;
	}
	
	protected void setUser (String user) {
		this.user = user;
	}
	
	protected void setPassword (String password) {
		this.password = password;
	}
	
	protected void setHost (String host) {
		this.host = host;
	}
	
	private void setDatabase (String database) {
		this.database = database;
	}
	
	protected void setPort (String port) {
		this.port = port;
	}
	
	
	public String getURL () {
		return URL;
	}
	

}