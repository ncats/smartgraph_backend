/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

public class Compound {

	

	private String cid = null;
	
	private String structure = null;
	private String uuid = null;
	private String hash = null;
	
	private Set<Pattern> patterns = null;
	private Pattern largestPattern = null;
	
	
	private Indigo indigoMolManager = new Indigo();

    private IndigoObject indigoCompound = null;
	
	
	public Compound (Set<UUID> allUUIDs, String structure, String cid, String hash) {
		setStructure (structure);
		setCid (cid);
		setHash(hash);
		
		
		UUID uuid = UUID.randomUUID();
		
		while (allUUIDs.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		
		allUUIDs.add(uuid);
		setUuid(uuid.toString());
		
        this.indigoCompound = indigoMolManager.loadMolecule(structure);

	}
	
	
	public Compound (String structure, String cid) {
        setStructure (structure);
        setCid (cid);
        
        this.indigoCompound = indigoMolManager.loadMolecule(structure);
        
    }
	
	public Compound (String cid) {

		this.patterns = new LinkedHashSet<Pattern> ();
		
		setCid (cid);

	}
	
	
	public int getHeavyAtomNumber () {
        return(indigoCompound.countHeavyAtoms());
    }
	
	public String toNeo4jInsertStatement() {
            
        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/

		String noStereoHash = getHash().split("-")[0];
		
	    String query = "CREATE (c:Compound { hash: '" + getHash() + "', uuid: '" + getUuid() + "'," + 
        " smiles: '" + getStructure().replace("\\", "\\\\") + "', compound_id: " + getCid() + ", "
        		+ "nostereo_hash: '" + noStereoHash +"' })";

        return query;
        
    }
	
	
   public String toCSV() {
       
        // Ref: http://www.markhneedham.com/blog/2013/06/20/neo4j-a-simple-example-using-the-jdbc-driver/

        String noStereoHash = getHash().split("-")[0];
        
        //String CSV = getHash() + "\t" + getUuid() + "\t" + getStructure().replace("\\", "\\\\") + "\t" +
        //                getCid() + "\t" + noStereoHash;

        
        String CSV = getHash() + "\t" + getUuid() + "\t" + getStructure() + "\t" +
                        getCid() + "\t" + noStereoHash;        
        
        return CSV;
        
    }
	
		
	public void registerPattern (Pattern pattern) {
		if (!patterns.contains(pattern.getHash())) {
			patterns.add(pattern);
		}
	}
	
	
	public void showC2P () {
		System.out.print(getCid() + ":");
		
		Iterator<Pattern> iT = patterns.iterator();
		Pattern p = null;
		
		while (iT.hasNext()) {
			p = iT.next();
			
			System.out.print("\t" + p.getPid());
		
			if (p.getHash().equals(largestPattern.getHash())) {
				System.out.print("*");
				
			}
			
		}
		
		System.out.println("");
		
	}
	

	
	public void identifyLargestPattern () {
		

		Pattern p = null;

		int maxHeavyAtomNum = 0;
		int heavyAtomNumOfPattern = 0;
		boolean first = true;
		
		Iterator<Pattern> iT = patterns.iterator();
		
		while (iT.hasNext()) {
			p = iT.next();
			
			heavyAtomNumOfPattern = p.getHeavyAtomNumber();
			
			if (heavyAtomNumOfPattern > maxHeavyAtomNum) {
				maxHeavyAtomNum = heavyAtomNumOfPattern;
			}
		}
		
		iT = patterns.iterator();
		
		while (iT.hasNext()) {
			p = iT.next();
			
			if ((maxHeavyAtomNum == p.getHeavyAtomNumber()) && first) {
				this.largestPattern = p;
				first = false;
			}
		}	
		
		

	}
	
	protected void setCid (String s) {
		this.cid = s;
	}

	protected void setStructure (String s) {
		this.structure = s;
	}
	
	

	public String getCid () {
		return cid;
	}

	public String getStructure () {
		return structure;
	}
	
	private void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getUuid() {
		return uuid;
	}

	private void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
