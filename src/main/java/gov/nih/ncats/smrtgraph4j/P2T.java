/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j;

import java.util.Set;
import java.util.UUID;

public class P2T {



    private String uniqueLabel = null;
    private String uuid = null;

    private String pid = null;  
    private String tid = null;
    
	private String edgeType = "undefined";

   public P2T (Set<UUID> allUUIDs, String pid, String tid) {
        
       setPid(pid);
       setTid(tid);

       

       
        UUID uuid = UUID.randomUUID();
        
        while (allUUIDs.contains(uuid)) {
            uuid = UUID.randomUUID();
        }
        
        allUUIDs.add(uuid);
        setUuid(uuid.toString());
    }


   
    public String toNeo4jInsertStatement() {
        
        


        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
        // Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
        // Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
        // Ref: https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j
        String query = "MATCH (p:Pattern { pattern_id: '" + getPid() + "'}) "
                + "MATCH (t:Target { uniprot_id: '" + getTid() + "'}) MERGE (p)-[:POTENT_PATTERN_OF "
                + "{ unique_label: '" + getPid() + "_" + getTid() + "', uuid: '" + getUuid() + "', edgeType: '" + getEdgeType() + "'}]->(t)";
    
        
        
        //System.out.println(query);
        return query;
        
    }
  
     public String toCSV() {
        
        


        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
        // Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
        // Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
        // Ref: https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j
        
        
        
        String CSV = getPid() + "\t" + getTid() + "\t" + getPid() + "_" + getTid() + "\t" + getUuid();
    
        
        
        //System.out.println(query);
        return CSV;
        
    }
    
    public String getUuid() {
        return uuid;
    }

    private void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private void setUniqueLabel(String unique_label) {
        this.uniqueLabel = unique_label;
    }

    public String getUniqueLabel() {
        return uniqueLabel;
    }
    
    public String getTid () {
        return tid;
    }   
    
    protected void setTid (String s) {
        this.tid = s;
    }
    
    private void setPid(String pid) {
        this.pid = pid;
    }
    
    public String getPid() {
        return pid;
    }

    public String getEdgeType() {
        return edgeType;
    } 
    
    private void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    } 

    
}
