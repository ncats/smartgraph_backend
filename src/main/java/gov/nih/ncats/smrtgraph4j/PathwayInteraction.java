/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PathwayInteraction {


	// Checked in chembl 23 chembl_id_lookup if chembl_id is unique: it is for targets, and compounds, so it's OK to use as
	// unique ID besides UUID. -- GZK 07/07/2017

	private String uniprot_ida = null;
	private String uniprot_idb = null;
	private List<String> mechanisms = null;
	private List<String> aggregatedInteractions = null;
	private List<String> publication_ids = null;
	private List<String> confidence_values = null;
	private String unique_intrxn_label = null;
	private double maxConfidenceValue = 0.0;
	private List<String> causal_statements = null;
	private String uuid = null;
	private List<String> edgeInfo = null;
	private String edgeType = null;
	private String sourceDB = null;
    
	
	public PathwayInteraction (Set<UUID> allUUIDs, String uniprot_ida, String uniprot_idb, //
			String mechanism, String intrxn_id, String publication_id, //
			double confidence_value, String causal_statement, String sourceDataBaseB) {
		
		// Ref: http://www.javapractices.com/topic/TopicAction.do?Id=56
		
		
		
		UUID uuid = UUID.randomUUID();
		
		while (allUUIDs.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		
		allUUIDs.add(uuid);
		setUuid(uuid.toString());
		
		this.mechanisms = new ArrayList<String> ();
        this.causal_statements = new ArrayList<String> ();
        this.aggregatedInteractions = new ArrayList<String> ();
        this.publication_ids = new ArrayList<String> ();
        this.confidence_values = new ArrayList<String> ();
        this.edgeInfo = new ArrayList<String> (); 
        
		setUniprot_ida(uniprot_ida);
		setUniprot_idb(uniprot_idb);
		registerMechanism(mechanism);
		registerReference(publication_id);
		setUnique_intrxn_label(uniprot_ida + "_" + uniprot_idb);
		setMaxConfidenceValue(confidence_value);
		registerCausalStatement (causal_statement);
	    registerInteraction(intrxn_id);
	    registerConfidenceValue(confidence_value);
	    assessEdgeType ();
	    setSourceDB(sourceDataBaseB);
	    
		
		// Average of confidence_values of signor's relevant PPIs:
//		signor20=# select avg(replace(confidence_values,'SIGNOR-miscore:','')::numeric) from ppi where uniprot_ida like '%uniprotkb:%' and type_a='psi-mi:MI:0326(protein)' and uniprot_idb like '%uniprotkb:%' and type_b='psi-mi:MI:0326(protein)' and uniprot_ida!=uniprot_idb and length(confidence_values)>0;
//        avg           
//        ------------------------
//		0.37974995334950550476
//	

		// STD deviation of confidence_values of signor's relevant PPIs:
//
//		signor20=# select stddev(replace(confidence_values,'SIGNOR-miscore:','')::numeric) from ppi where uniprot_ida like '%uniprotkb:%' and type_a='psi-mi:MI:0326(protein)' and uniprot_idb like '%uniprotkb:%' and type_b='psi-mi:MI:0326(protein)' and uniprot_ida!=uniprot_idb and length(confidence_values)>0;
//        stddev         
//        ------------------------
//        0.27749145045156343065
//        (1 row)

	}

	
	
	
//    public Node toNode(GraphDatabaseService gdb) {
//        Node node = gdb.createNode(NodeType.Target);
//        node.setProperty("created", new java.util.Date().getTime());
//        
//        if (getChEMBL_idL() != null) node.setProperty("chembl_id", getChEMBL_idL());
//        else node.setProperty("chembl_id", "-");
//        
//        node.setProperty("uniprot_id", getUniProtID());
//        node.setProperty("name", getName());
//        node.setProperty("uuid", getUuid());
//        return (node);
//    }
	
	
	public String getUuid() {
		return uuid;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}	
	
	public String toNeo4jInsertStatement() {
		
		
		//assembleEdgeInfo
		
		
		String inxn_id = getUniprot_ida() + "_" + getUniprot_idb();
		// Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
		// Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
		// Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
		// Ref:	https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j
//		String query = "MATCH (t1:Target { uniprot_id: '" + getUniprot_ida() + "'}) "
//				+ "MATCH (t2:Target { uniprot_id: '" + getUniprot_idb() + "'}) MERGE (t1)-[:REGULATES "
//				+ "{mechanisms: " + listToStringJDBC(getMechanisms()) + ", signor_intrxn_ids: " + listToStringJDBC(getInteractions()) + ", "
//				+ "references: " + listToStringJDBC(getReferences()) + ", ppi_uid: '" + inxn_id  + "', uuid: '" + getUuid()  + "', max_confidence_value: "
//				+ getMaxConfidenceValue() + ", causal_statements: " + listToStringJDBC (getCausalStatements())
//				+ ", confidence_values: " + listToStringJDBC (getConfidenceValues()) + "  }]->(t2)";
//		
//		
		
		String query = "MATCH (t1:Target { uniprot_id: '" + getUniprot_ida() + "'}) "
				+ "MATCH (t2:Target { uniprot_id: '" + getUniprot_idb() + "'}) MERGE (t1)-[:REGULATES "
				+ "{edgeType: '" + getEdgeType() + "', edgeInfo: " + assembleEdgeInfo() + ", ppi_uid: '" + inxn_id  + "', uuid: '" + getUuid()  + "', max_confidence_value: "
				+ getMaxConfidenceValue() + ", sourceDB: '" + getSourceDB() + "' }]->(t2)";
	
		
		
		//System.out.println(query);
		return query;
		
	}
	
	
    public String toCSV() {
        
        
        
        String inxn_id = getUniprot_ida() + "_" + getUniprot_idb();
        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
        // Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
        // Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
        // Ref: https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j

        


        String CSV = getUniprot_ida() + "\t" + getUniprot_idb() + "\t" + listToString(getMechanisms()) + "\t" +
                    listToString(getInteractions()) + "\t" + 
                    listToString(getReferences()) + "\t" + inxn_id  + "\t" + getUuid()  + "\t" +
                    getMaxConfidenceValue() + "\t" + listToString (getCausalStatements()) + "\t" +
                    listToString (getConfidenceValues());
    
        

        return CSV;
        
    }	
	
   public List<String> getMechanisms() {
        return mechanisms;
    }

    private void registerMechanism(String mechanism) {
    	if ((mechanism == null) || (mechanism.equals("null")) || (mechanism.equals("")) || (mechanism.equals("non exportable"))) {
    		mechanism = "Mech_NA";
    	}
    	
        this.mechanisms.add(mechanism);
    }
    
    public void addMechanism(String mechanism) {
        registerMechanism(mechanism);
    }
    


	
	private void registerCausalStatement(String causal_statement) {
    	if ((causal_statement == null) || (causal_statement.equals("null")) || (causal_statement.equals("")) || (causal_statement.equals("non exportable"))) {
    		causal_statement = "CS_NA";
    	}
		
    	this.causal_statements.add(causal_statement);
	}

    public void addCausalStatement(String causal_statement) {
        registerCausalStatement(causal_statement);
    }

	   
	public List<String> getCausalStatements() {
        return causal_statements;
    }
    
    
    
    private void registerReference (String pub_id) {
    	if ((pub_id == null) || (pub_id.equals("null")) || (pub_id.equals("")) || (pub_id.equals("non exportable"))) {
    		pub_id = "ref_NA";
    	}
    	
    	publication_ids.add(pub_id);
    } 
    
    public void addReference(String pub_id) {
        registerReference(pub_id);
    }
   

    public List<String> getReferences() {
        return publication_ids;
    }
    
 
    private void registerConfidenceValue (double CV) {
    	
    	confidence_values.add(Double.toString(CV));
    } 
    
    public void addConfidenceValue(double CV) {
        registerConfidenceValue(CV);
    }
   

    public List<String> getConfidenceValues() {
        return confidence_values;
    }
	

   
   private void registerInteraction (String intrxn_id) {
	   
	   if ((intrxn_id == null) || (intrxn_id.equals("null")) || (intrxn_id.equals("") || (intrxn_id.equals("non exportable")))) {
		   intrxn_id = "INX_NA";
	   }

	   
	   aggregatedInteractions.add(intrxn_id);
   }   

   public void addInteraction(String intrxn_id) {
       registerInteraction(intrxn_id);
   }  
   
   public List<String> getInteractions() {
       return aggregatedInteractions;
   }
   
 
   
   public String getEdgeType() {
       return edgeType;
   } 
   
   private void setEdgeType(String edgeType) {
       this.edgeType = edgeType;
   } 
   
   
   public String getUniprot_ida() {
       return uniprot_ida;
   }

   private void setUniprot_ida(String uniprot_ida) {
       this.uniprot_ida = uniprot_ida;
   }

   public String getUniprot_idb() {
       return uniprot_idb;
   }

   private void setUniprot_idb(String uniprot_idb) {
       this.uniprot_idb = uniprot_idb;
   }

   

   public String getUnique_intrxn_label() {
       return unique_intrxn_label;
   }

   private void setUnique_intrxn_label(String unique_intrxn_label) {
       this.unique_intrxn_label = unique_intrxn_label;
   }




   public double getMaxConfidenceValue() {
       return maxConfidenceValue;
   }




   private void setMaxConfidenceValue(double confidence) {
       if (confidence > this.maxConfidenceValue) {
           this.maxConfidenceValue = confidence;
       }
   }
     

  

   
   public String listToString (List<String> someList) {
       
       // Ref: https://docs.oracle.com/javase/tutorial/java/data/buffers.html
       StringBuilder result = null;
       boolean first = true;
	   
       result = new StringBuilder("");
  
       for (int i = 0; i < someList.size(); i++) {

           if (someList.get(i) == null) {
               someList.set(i, "NA");
           }
  
           if (!first) {
               result.append("|");
               result.append(someList.get(i));
           }
           else {
        	   result.append(someList.get(i));

               first = false;
           }

      
       }
      
       
       
       
       return result.toString();
   }     

   
   public String listToStringJDBC (List<String> someList) {
       
       // Ref: https://docs.oracle.com/javase/tutorial/java/data/buffers.html
       StringBuilder result = null;
       boolean first = true;
       
       result = new StringBuilder("[");
  
       for (int i = 0; i < someList.size(); i++) {
  
           if (!first) {
               result.append(", '");
               result.append(someList.get(i));
               result.append("'");
           }
           else {
               if (someList.get(i) == null) {
                   someList.set(i, "NA");
               }
               result.append("'");
               result.append(someList.get(i));
               result.append("'");

               first = false;
           }

      
       }
      
       
       result.append("]");
       
       
       return result.toString();
   }     
   
   
   
   public String assembleEdgeInfo () {
       // Ref: https://docs.oracle.com/javase/tutorial/java/data/buffers.html
       StringBuilder result = null;
       boolean first = true;
       
       Set<String> uniqueInfo = new HashSet<String>(); 
       
       String mechanism = null; 
       String causal_statement = null; 
       String publication_id = null; 
       String confidence_value = null; 
       
       result = new StringBuilder("[");
       StringBuilder record = null;
  
       for (int i = 0; i < mechanisms.size(); i++) {

    	   record  = new StringBuilder ("");
    	   
    	   mechanism = mechanisms.get(i);
    	   causal_statement = causal_statements.get(i);
    	   publication_id = publication_ids.get(i);
    	   confidence_value = confidence_values.get(i);
    	   
           if (mechanism == null) {
        	   mechanism = "";
           }
           else {
        	   
        	   if (!mechanism.equals("Mech_NA")) {

        		   // Ref: https://stackoverflow.com/questions/13948751/string-parse-error
	        	   //System.out.println(mechanism );
	        	   mechanism = mechanism
	        			   		.split("\\(")[1]
	        			   		.split("\\)")[0];
        	   }
        	   else 
        	   {
        	       mechanism = "";
        	   }

           }
    	   

           
           
           
           if (causal_statement == null) {
        	   causal_statement = "";
           }
           else {
        	   
        	   if (!causal_statement.equals("CS_NA")) {
	        	   // Ref: https://stackoverflow.com/questions/13948751/string-parse-error
	        		   
	        	   //System.out.println(causal_statement);
	
	        	   causal_statement = causal_statement
	        			   		.split("\\(")[1]
	        			   		.split("\\)")[0];
        	   }
        	   else {
        	       causal_statement = "";
        	   }

           }
    	   
           
           if (publication_id == null) {
        	   publication_id = "";
           }
           
    	   
           if (confidence_value == null) {
        	   confidence_value = "";
           }
           
           
           
    	   record.append(mechanism)
    	   		.append("|")
    	   		.append(causal_statement)
    	   		.append("|")
    	   		.append(publication_id)
    	   		.append("|")
    	   		.append(confidence_value);
    	   		

           
    	   
           if (!first) {
        	   
        	   if (!uniqueInfo.contains(record.toString())) { 

	               result.append(", '");
	               result.append(record.toString());
	               result.append("'");
	               uniqueInfo.add(record.toString());
        	   }
           }
           else {

        	   
               result.append("'");
               result.append(record.toString());
               result.append("'");
               uniqueInfo.add(record.toString());
               first = false;
           }

      
       }
      
       
       result.append("]");	
       
       return result.toString();
   }
   
   private void assessEdgeType () {
	   
	   boolean up = false;
	   boolean down = false;
	   
	   for (String cs: causal_statements) {
		   
		   if (cs.toLowerCase().contains("up")) up = true;

		   if (cs.toLowerCase().contains("down")) down = true;

	   }
	   
	   if (up & down) setEdgeType("conflict");
	   else if (up) setEdgeType("up");
	   else if (down) setEdgeType("down");
	   else setEdgeType("undefined");
   }




    public String getSourceDB() {
        return sourceDB;
    }
    
    
    
    
    private void setSourceDB(String sourceDB) {
        this.sourceDB = sourceDB;
    }
   
}
