/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

public class Pattern {

	
	private String pid = null;
	private String hash = null;
	private String structure = null;
	private int idx = 0;
	
	private String uuid = null;
	
	private String pattern_type = null;
	
	private Map<String, Pattern> neighbors = null;	
	private Map<String, Pattern> subStructures = null;	
	private Map<String, Pattern> superStructures = null;	
	
	private Indigo indigoMolManager = new Indigo();
	private IndigoInchi indigoInchi = new IndigoInchi (indigoMolManager);
	
	private IndigoObject indigoPattern = null;
	
	
    public Pattern (Set<UUID> allUUIDs, String pid, String structure, String hash, String pattern_type) {
        
        neighbors = new HashMap<String, Pattern> ();
        subStructures = new HashMap<String, Pattern> ();
        superStructures = new HashMap<String, Pattern> ();
        
        
        UUID uuid = UUID.randomUUID();
        
        while (allUUIDs.contains(uuid)) {
            uuid = UUID.randomUUID();
        }
        
        allUUIDs.add(uuid);
        setUuid(uuid.toString());

        
        setPid(pid);
        setStructure(structure);
  
        setHash(hash);
        setPattern_type(pattern_type);
        
        

    
    }	
	
	public Pattern (Set<UUID> allUUIDs, String pid, String structure, int idx, String hash, String pattern_type) {
		
		neighbors = new HashMap<String, Pattern> ();
		subStructures = new HashMap<String, Pattern> ();
		superStructures = new HashMap<String, Pattern> ();
		
		
		UUID uuid = UUID.randomUUID();
		
		while (allUUIDs.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		
		allUUIDs.add(uuid);
		setUuid(uuid.toString());

		
		setPid(pid);
		setStructure(structure);
		setIdx(idx);
		setHash(hash);
		setPattern_type(pattern_type);
		
		

	
	}

	public Pattern (String structure, String pid, String pattern_type) {
		
		neighbors = new HashMap<String, Pattern> ();
		subStructures = new HashMap<String, Pattern> ();
		superStructures = new HashMap<String, Pattern> ();

		setStructure(structure);
		setPid(pid);
		setPattern_type(pattern_type);
		
		// Ref: http://lifescience.opensource.epam.com/indigo/api/

		this.indigoPattern = indigoMolManager.loadMolecule(structure);
		
		setHash(indigoInchi.getInchiKey(indigoInchi.getInchi(indigoPattern)));
		
	}

   public String toNeo4jInsertStatement() {
       
        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/

        String query = "CREATE (p:Pattern { hash: '" + getHash() + "', uuid: '" + getUuid() + "'," + 
        " smiles: '" + getStructure().replace("\\", "\\\\") + "', pattern_id: '" + getPid() + "', pattern_type: '" + getPattern_type()+ "'})";

        return query;
        
    }

   public String toCSV() {
       
       // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/

       //String CSV = getHash() + "\t" + getUuid() + "\t" + getStructure().replace("\\", "\\\\") + "\t" + getPid() +
       //          getPattern_type();
                   
                   
       String CSV = getHash() + "\t" + getUuid() + "\t" + getStructure() + "\t" + getPid() +
                   getPattern_type();                  

       return CSV;
       
   }	
	
	public int getHeavyAtomNumber () {
		return(indigoPattern.countHeavyAtoms());
	}
	
	public void addNeighbor (Pattern pattern, String id) {
		registerNeighbor (pattern, id);
	}
	
	public void addSubStructure (Pattern pattern, String id) {
		registerSubStructure (pattern, id);
	}
	
	public void addSuperStructure (Pattern pattern, String id) {
		registerSuperStructure (pattern, id);
	}
	
	
	private void registerNeighbor (Pattern pattern, String id)
	{
		if (!this.neighbors.containsKey(id)) {
			this.neighbors.put(id, pattern);
		}
	}
	

	
	private void registerSubStructure (Pattern pattern, String id)
	{
		if (!this.subStructures.containsKey(id)) {
			this.subStructures.put(id, pattern);
		}
	}	
	
	private void registerSuperStructure (Pattern pattern, String id)
	{
		if (!this.superStructures.containsKey(id)) {
			this.superStructures.put(id, pattern);
		}
	}

	public String getPid() {
		return pid;
	}

	public String getHash() {
		return hash;
	}

	public String getStructure() {
		return structure;
	}

	public int getIdx() {
		return idx;
	}

	public Map<String, Pattern> getNeighbors() {
		return neighbors;
	}

	public Map<String, Pattern> getSubStructures() {
		return subStructures;
	}

	public Map<String, Pattern> getSuperStructures() {
		return superStructures;
	}

	private void setPid(String pid) {
		this.pid = pid;
	}

	private void setHash(String hash) {
		this.hash = hash;
	}

	private void setStructure(String structure) {
		this.structure = structure;
	}

	private void setIdx(int idx) {
		this.idx = idx;
	}



	public String getPattern_type() {
		return pattern_type;
	}



	private void setPattern_type(String pattern_type) {
		this.pattern_type = pattern_type;
	}




	public String getUuid() {
		return uuid;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public void show () {
		System.out.println (getStructure() + "\t" + getPid() + "\t" + getHash() + "\t" + getPattern_type());
	}
	
	public String toSQLInsertStatement (String table) {
		
		StringBuilder sb = new StringBuilder ("INSERT into ");
		
		sb.append(table)
			.append(" (structure, pattern_id, ptype, hash) VALUES (")
			.append("'")
			.append(getStructure())
			.append("', '")
	        .append(getPattern_type())
            .append(".")
			.append(getPid())
			.append("', '")
			.append(getPattern_type())
			.append("', '")
			.append(getHash())
			.append("')");

		return sb.toString();
	}
	
}
