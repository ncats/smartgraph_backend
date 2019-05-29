/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.signorloader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PPI {
	
	private Map<String, String> properties = null;
	
	public PPI () {
		properties = new HashMap<String, String> ();
	}
	
	public void setProperty (String key, String value) {
		this.properties.put(key, value);
	}
	
	public String toSQLInsertStatement (String table) {
		
		String query = "INSERT INTO "+ table + " (";
		String fields = null;

		String values = null;
		
		Iterator<String> iT = properties.keySet().iterator(); 
		
		String key = null;
		String value = null;
		
		boolean first = true;
		while (iT.hasNext()) {
			key = iT.next();
			value = properties.get(key);
			
			if (!first) {
				fields += "," + key;
				values += ", '" + value + "'";
			}
			else {
				
				fields = key;
				values = "'" + value + "'";
				first = false;
				
			}
			
			
		}
		
		
		query += fields + ") VALUES (" + values + ")";
		
		return query;
	}
	
	
	
}
