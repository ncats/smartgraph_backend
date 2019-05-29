/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.structure;

import java.util.Set;
import java.util.UUID;

public class C2P {

	private Compound compound = null;
	private Pattern pattern = null;
    private boolean maximal = false;
    private String uniqueLabel = null;
	private String uuid = null;

    private String cid = null;	
    private String pid = null;
    
	private String edgeType = "undefined";
    
    private double pattern_overlap_ratio = 0.0;
	
	public C2P (Compound compound, Pattern pattern, boolean maximal) {
		
		double POR = 0.0;
		
		setCompound(compound);
		setPattern(pattern);
		setUniqueLabel(compound.getCid() + "_" + pattern.getPid());
		setMaximal(maximal);
		
		
		
		if (compound.getHeavyAtomNumber() > 0) {
			POR  = (double) pattern.getHeavyAtomNumber() / compound.getHeavyAtomNumber(); 
		}
		else {
			throw new IllegalStateException ("[ERROR] Invalid number of heavyatoms in compound: " + compound.getCid() + 
					" . Terminating...");
		}
		
		setPattern_overlap_ratio(POR);
		
	}	
	
	public C2P (Set<UUID> allUUIDs, Compound compound, Pattern pattern) {
		
		UUID uuid = UUID.randomUUID();
		
		while (allUUIDs.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		
		allUUIDs.add(uuid);
		setUuid(uuid.toString());
	}
	
   public C2P (Set<UUID> allUUIDs, String cid, String pid, String isMaximal, double ratio) {
        
       setCid(cid);
       setPid(pid);
       setPattern_overlap_ratio(ratio);
       
       if (isMaximal.equals("t")) {
    	   setMaximal(true);
       }
       
       else if (isMaximal.equals("f")) {
    	   setMaximal(false);
       }
       
       else {
       
    	   throw new IllegalStateException ("[ERROR]: Invalid logical statement for C2P (compound_id - pattern_id): " +
                                           getCid() + " - " + getPid() + " . Terminating...");
       }
       
        UUID uuid = UUID.randomUUID();
        
        while (allUUIDs.contains(uuid)) {
            uuid = UUID.randomUUID();
        }
        
        allUUIDs.add(uuid);
        setUuid(uuid.toString());
    }


   
	public String toNeo4jInsertStatement() {
		
		
		
	    String logicStatement = null;
	    
	    
        if (isMaximal()) {
            logicStatement = "true";
        }
        else {
            logicStatement = "false";
        }
        
		

		// Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
		// Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
		// Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
		// Ref:	https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j
		String query = "MATCH (c:Compound { compound_id: " + getCid() + "}) "
				+ "MATCH (p:Pattern { pattern_id: '" + getPid() + "'}) MERGE (p)-[:PATTERN_OF "
				+ "{ ratio: " + getPattern_overlap_ratio() + ", "
				+ "unique_label: '" + getCid() + "_" + getPid() + "', uuid: '" + getUuid() + "', islargest: '" + logicStatement 
				+ "', edgeType: '" + getEdgeType() + "'}]->(c)";
	
		
		
		//System.out.println(query);
		return query;
		
	}
   
	
	
	   public String toCSV() {
	        
	        
	        
	        String logicStatement = null;
	        
	        
	        if (isMaximal()) {
	            logicStatement = "true";
	        }
	        else {
	            logicStatement = "false";
	        }
	        
	        

	        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
	        // Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
	        // Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
	        // Ref: https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j
	    
	    
	    
	        String CSV = getCid() + "\t" + getPid() + "\t" + getPattern_overlap_ratio() + "\t" +
	                        getCid() + "_" + getPid() + "\t" + getUuid() + logicStatement;
	                        

	        return CSV;
	        
	    }
	    
	
	public String toSQLInsertStatement (String tableName) {
	    String logicStatement = null;
	    
	    
        if (isMaximal()) {
            logicStatement = "true";
        }
        else {
            logicStatement = "false";
        }
        

	    
	    StringBuilder sb = new StringBuilder ("INSERT into ");
	    
	    sb.append (tableName)
	                    .append(" (component_id, pattern_id, islargest, pattern_overlap_ratio) VALUES (")
	                    .append (getCompound().getCid())
                        .append (", ")
                        .append("'")
                        .append(getPattern().getPattern_type())
                        .append(".")
                        .append (getPattern().getPid())
                        .append ("', '")
                        .append (logicStatement)
                        .append ("', ")
                        .append (getPattern_overlap_ratio())
                        .append (")");
        
        
        
	    return sb.toString();
	}
	
	public String getUuid() {
		return uuid;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}

    private void setCompound(Compound compound) {
        this.compound = compound;
    }

    private void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    private void setUniqueLabel(String unique_label) {
        this.uniqueLabel = unique_label;
    }

    public Compound getCompound() {
        return compound;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getUniqueLabel() {
        return uniqueLabel;
    }

    public boolean isMaximal() {
        return maximal;
    }

    private void setMaximal(boolean maximal) {
        this.maximal = maximal;
    }	
    
    public String getCid () {
        return cid;
    }	
    
    protected void setCid (String s) {
        this.cid = s;
    }
    
    private void setPid(String pid) {
        this.pid = pid;
    }
    
    public String getPid() {
        return pid;
    }

	public double getPattern_overlap_ratio() {
		return pattern_overlap_ratio;
	}

	private void setPattern_overlap_ratio(double pattern_overlap_ratio) {
		this.pattern_overlap_ratio = pattern_overlap_ratio;
	}
    
    public String getEdgeType() {
        return edgeType;
    } 
    
    private void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    } 
}
