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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;



public class Target {



	// Checked in chembl 23 chembl_id_lookup if chembl_id is unique: it is for targets, and compounds, so it's OK to use as
	// unique ID besides UUID. -- GZK 07/07/2017

	private String ChEMBL_id = null;
	private String name = null;
	private Set<String> Synonyms = null;
	private String shortName = null;
	private String UniProtID = null;
	private String uuid = null;
	private String organism = null;
	private Set<String> IDs_ChEMBL = null;
	private Set<String> IDs_GENE_SYMBOL = null;
	private double activityCutoff = 0.0;
	
	private final int shortNameLength = 50;
	
	public Target (Set<UUID> allUUIDs, String UniProtID_id, String name, String organism) {
		
		// Ref: http://www.javapractices.com/topic/TopicAction.do?Id=56
		
		UUID uuid = UUID.randomUUID();
		
		while (allUUIDs.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		
		allUUIDs.add(uuid);
		setUuid(uuid.toString());


		setName(name);
		
		Synonyms = new HashSet<String> ();
		Synonyms = extractSynonyms();
		setUniProtID(UniProtID_id);
		setOrganism(organism);
		

		
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
	
	public String toNeo4jInsertStatement() {
		
		// Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
		
		String ch_id = null;
		
		if (getChEMBL_id() != null) ch_id = getChEMBL_id();
		else ch_id = "-";
		
		String query = "CREATE (t:Target { uniprot_id: '" + getUniProtID() + "', uuid: '" + getUuid() + "'," + 
		" synonyms: " + aSetToStringJDBC(getSynonyms()) + ", activity_cutoff: " + getActivityCutoff() + 
		", fullname: '" + extractMostInformativeName(getName()) + "'" +
		//", chembl_ids: " + aSetToStringJDBC(IDs_ChEMBL) +
		", gene_symbols: " + aSetToStringJDBC(IDs_GENE_SYMBOL) +
		" })";
		//				+ ", chembl_id: '" + ch_id + "' })";
		
		//System.out.println(IDs_ChEMBL);
		return query;
		
	}
	
	
   public String toCSV() {
        
        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/
        
        String ch_id = null;
        
        if (getChEMBL_id() != null) ch_id = getChEMBL_id();
        else ch_id = "-";
        
        String CSV = getUniProtID() + "\t" + getUuid() + "\t" + getShortName()  + "\t" + getName();
        //              + ", chembl_id: '" + ch_id + "' })";
        

        return CSV;
        
    }
	
	
	public String getChEMBL_id() {
		return ChEMBL_id;
	}

	public void setChEMBL_id(String chEMBL) {
		ChEMBL_id = chEMBL;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
		
		setShortName(name);
		
	}

	public String getUniProtID() {
		return UniProtID;
	}

	public void setUniProtID(String uniProtID) {
		UniProtID = uniProtID;
	}

	public String getUuid() {
		return uuid;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOrganism() {
		return organism;
	}

	private void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getShortName() {
		return shortName;
	}

	private void setShortName(String name) {

		String shortName = null;
				
		if (name.length() < this.shortNameLength) {
			this.shortName = name;
		}
		else {
			shortName = name.substring(0, this.shortNameLength) + "..."; 
			this.shortName = shortName;
		}

	}
	
	
	private Set<String> extractSynonyms () {
		Set<String> synonyms = new HashSet<String> ();
		
		String s = null;
		String tmp[] = name.split("\\(");
		
		
		for (int i = 0; i < tmp.length; i++) {
			s = tmp[i].split("\\)")[0];
		}
		
		synonyms.add(s);
		
		return synonyms;
	}
	
	
	
	public Set<String> getSynonyms () {
		return this.Synonyms;
	}
	
	
	
		   
   public String extractMostInformativeName (String name) {
	   String res  = null;
	   
	   return res = name.split("\\(")[0];
   }

   
   
   public void setChEMBLIDs (Set<String> ids) {
       this.IDs_ChEMBL = ids;
   }
   
   
   
   public void setGENE_SYMBOLs (Set<String> ids) {
       //System.out.println(ids);
       this.IDs_GENE_SYMBOL = ids;
   }
	   
   
   public String aSetToStringJDBC (Set<String> someSet) {
       
       // Ref: https://docs.oracle.com/javase/tutorial/java/data/buffers.html
       StringBuilder result = null;
       boolean first = true;
       
       result = new StringBuilder("[");
  
       Iterator<String> iT = null;
 
       
       String member = null;
       
       if (someSet.isEmpty()) {
           result.append("");
          
       }
       
       else {
           
           iT = someSet.iterator();   
           
           while (iT.hasNext()) {
               member = iT.next();
    
               if (!first) {
                   result.append(", '");
                   result.append(member);
                   result.append("'");
               }
               else {
                   if (member == null) {
                       member = "";
                   }
                   result.append("'");
                   result.append(member);
                   result.append("'");
    
                   first = false;
               }
           }
       }
       


       
       result.append("]");
       
       
       return result.toString();
   }

public double getActivityCutoff() {
	return activityCutoff;
}

public void setActivityCutoff(double activityCutoff) {
	this.activityCutoff = activityCutoff;
}     
   
    
   
   
}
