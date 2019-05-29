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

public class C2T {



    private String uniqueLabel = null;
    private String uuid = null;

    private String cid = null;  
    private String tid = null;
    
    private double canonicalActivitypM = 0.0;
    
	private String edgeType = "undefined";

   public C2T (Set<UUID> allUUIDs, String cid, String tid, double activity) {
        
       setCid(cid);
       setTid(tid);
       setCanonicalActivitypM(activity);
       

       
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
        String query = "MATCH (c:Compound { compound_id: " + getCid() + "}) "
                + "MATCH (t:Target { uniprot_id: '" + getTid() + "'}) MERGE (c)-[:TESTED_ON "
                + "{ unique_label: '" + getCid() + "_" + getTid() + "', uuid: '" + getUuid() + "', "
                + "activity: " + 
                1000000 * (Math.pow(10, (-1) * getCanonicalActivitypM())) +
                ", activity_type: 'activity', edgeType: '" + getEdgeType() +"'}]->(t)";
    
        
        
        //System.out.println(query);
        return query;
        
    }
  
    
    
    
    public String toCSV() {
         
         


         // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
         // Ref: https://stackoverflow.com/questions/20456002/adding-relationship-to-existing-nodes-with-cypher
         // Ref: https://groups.google.com/forum/#!topic/neo4j/bY5GXWYpM1k
         // Ref: https://stackoverflow.com/questions/21979782/how-to-push-values-to-property-array-cypher-neo4j

         String CSV = getCid() + "\t" + getTid() + "\t" + getCid() + "_" + getTid() + "\t" + getUuid() +
                             "\t" + 1000000 * (Math.pow(10, (-1) * getCanonicalActivitypM())) + "\tactivity";
     

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
    
    private void setCid(String cid) {
        this.cid = cid;
    }
    
    public String getCid() {
        return cid;
    }


    public double getCanonicalActivitypM () {
        return canonicalActivitypM;
    }

    private void setCanonicalActivitypM (double canonicalActivitypM) {
        this.canonicalActivitypM = canonicalActivitypM;
    }
    
    public String getEdgeType() {
        return edgeType;
    } 
    
    private void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    } 
    
}
