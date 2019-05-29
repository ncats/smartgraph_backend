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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.ncats.smrtgraph4j.structure.C2P;
import gov.nih.ncats.smrtgraph4j.structure.Compound;
import gov.nih.ncats.smrtgraph4j.structure.Pattern;

public class DBOperations {

	private DBConnector DBC = null;

	
	
	private String limit = "";
	
	public DBOperations (DBConnector connection, boolean isTest) {
	    
	    if (isTest) {
	        limit = " limit 1000";
	    }
	    
	    
	    
		assignDBConnetcion(connection);
	}
	
	
	public void assignDBConnetcion(DBConnector connection) {
		// TODO Auto-generated method stub
		this.DBC = connection;
	}
	
	
	public double getTargetActivityCutoff (String uniprot_id) throws SQLException {
		
		double cutoff = 0.0;
		
		List<String> columns = new ArrayList<String> ();
		columns.add("actual_cutoff");
		
	    String query = "select actual_cutoff from target_activity_cutoff where uniprot_id='" + uniprot_id + "'";
	       		
		
	    List<String> ACS = DBC.runQuery(query, columns);
	    
	    if (ACS.size() > 1) {
	    	throw new IllegalStateException ("[ERROR]: target " + uniprot_id + " has more "
	    			+ "than one activity cutoff values.");
	    }
	    
	    cutoff = Double.parseDouble(ACS.get(0));
	    
		return cutoff;
	}
	
	
    public List<String> acquireC2Ts () throws SQLException {
        
        List<String> C2Ts = new ArrayList<String> ();
        
        List<String> columns = new ArrayList<String> ();
        
     
        columns.add("compound_id");
        columns.add("uniprot_id");
        columns.add("can_activity_pm");

        
        String query = "select c.compound_id, a.uniprot_id, a.can_activity_pm from compound c, canonical_activity a where a.inchi_key=c.inchi_key and a.can_activity_pm is not null order by compound_id asc, uniprot_id asc" + limit;
        
        //System.out.println(query);
        
        C2Ts = DBC.runQuery(query, columns);
     
        
     
        return C2Ts;
        
     }   
	
	
	
    public List<String> acquireP2Ts () throws SQLException {
        
 /*     ###### ASSOCIATING TARGETS WITH POTENT SCAFFOLDS  - START ##################

        Logic :

        1. Find active potent interaction.
        2. Find the scaffolds in C2P table (compounds-all scaffold associations) that are associated to the compound
           of the potent interaction.
        3. The identified scaffolds are potent scaffolds of the target in question.
        4. List the unique target_id - scaffold_id associations.



        ###### ASSOCIATING TARGETS WITH POTENT SCAFFOLDS  - END ##################
        


        
        
        */
        
        List<String> P2Ts = new ArrayList<String> ();
        
        List<String> columns = new ArrayList<String> ();
        
     
        columns.add("pattern_id");
        columns.add("uniprot_id");

        
        
        //String query = "select c.pattern_id, a.uniprot_id from compound u, canonical_activity a, c2p c where a.can_activity_pm>=" + cutoff + " and a.inchi_key=u.inchi_key and u.compound_id=c.compound_id group by c.pattern_id, a.uniprot_id" + limit;
        String query = "select c.pattern_id, a.uniprot_id from compound u, canonical_activity a, c2p c, target_activity_cutoff tac where a.can_activity_pm>=tac.actual_cutoff and tac.uniprot_id=a.uniprot_id and a.inchi_key=u.inchi_key and u.compound_id=c.component_id group by c.pattern_id, a.uniprot_id" + limit;
        
        
        //System.out.println(query);
        
        P2Ts = DBC.runQuery(query, columns);
     
        
     
        return P2Ts;
        
        
     }   
	
    public List<String> acquireC2Ps () throws SQLException {
        
        List<String> C2Ps = new ArrayList<String> ();
        
        List<String> columns = new ArrayList<String> ();
        
     
        columns.add("compound_id");
        columns.add("pattern_id");
        columns.add("islargest");
        columns.add("pattern_overlap_ratio");

     
        
        
        
        
        
        String query = "select component_id as compound_id, pattern_id, islargest, pattern_overlap_ratio from c2p order by compound_id asc, pattern_id asc" + limit;
        
        //System.out.println(query);
        
        C2Ps = DBC.runQuery(query, columns);
     
        
     
        return C2Ps;
        
        
     }   

	
    public List<String> acquirePatternsOfChembl () throws SQLException {
        
        List<String> patterns = null;
        
        List<String> columns = new ArrayList<String> ();
        
        
        columns.add("pattern_id");
        columns.add("structure");
        columns.add("hash");
        columns.add("ptype");
        
        
        String query = "select pattern_id, structure, hash, ptype from pattern where hash is not null order by pattern_id asc" + limit;
        
        patterns = DBC.runQuery(query, columns);
        
        return patterns;
    }   
	

   public List<String> acquireCompoundsOfChembl () throws SQLException {
        
        List<String> compounds = null;
        
        List<String> columns = new ArrayList<String> ();
        
        
        columns.add("compound_id");
        columns.add("smiles");
        columns.add("inchi_key");
      
        
        String query = "select compound_id, smiles, inchi_key from compound where inchi_key is not null order by compound_id asc" + limit;
        
        compounds = DBC.runQuery(query, columns);
        
        return compounds;
    }

	public List<String> acquireTargetsOfPPIs () throws SQLException {
		
		List<String> targets = null;
		
		List<String> columns = new ArrayList<String> ();
		
		
		columns.add("uniprot_id");
		columns.add("name");
		columns.add("organism");
		
		
		String query = "select u.entry as uniprot_id, u.protein_names as name, u.organism from uniprot u, (select uniprot_ida "
				+ "as uniprot_id from ppi where uniprot_ida like '%uniprotkb:%' and type_a='psi-mi:MI:0326(protein)' "
				+ "union select uniprot_idb as uniprot_id from ppi where uniprot_idb like '%uniprotkb:%' "
				+ "and type_b='psi-mi:MI:0326(protein)') as t where replace(t.uniprot_id,'uniprotkb:','')=u.entry" + limit;
		
		targets = DBC.runQuery(query, columns);
		
		return targets;
	}
	
	
	
    

	
	
	public List<String> acquireTargetsOfChembl (DBConnector dbcPPI) throws SQLException {
		
		List<String> chembl_targets = null;
		List<String> targets = new ArrayList<String> ();
		List<String> tmp = null;
		
		List<String> columns = new ArrayList<String> ();
		

		columns.add("uniprot_id");
		
		String query = "select distinct uniprot_id from canonical_activity" + limit;
		
		chembl_targets = DBC.runQuery(query, columns);

		
		for (String target: chembl_targets ) {
			
			// Here recycling the column uniprot_id
			columns.add("name");
			columns.add("organism");


			
			query = "select entry as uniprot_id, protein_names as name, organism from uniprot where entry='" + target + "'";
			//System.out.println(query);
			
			tmp = dbcPPI.runQuery(query, columns);
			//System.out.println(tmp);

			targets.addAll(tmp);
		}
		
		return targets;
		
		
	}
	
	public List<String> acquirePPIs () throws SQLException {
		
		List<String> PPIs = new ArrayList<String> ();
		
		List<String> columns = new ArrayList<String> ();
		

		columns.add("uniprot_ida");
		columns.add("uniprot_idb");
		columns.add("causal_regulatory_mechanism");
		columns.add("intrxn_ids");
		columns.add("publication_id");
		columns.add("confidence_value");
		columns.add("causal_statement");
		
		
		
		
		
		String query = "select replace(uniprot_ida, 'uniprotkb:','') as uniprot_ida, "
				+ "replace(uniprot_idb, 'uniprotkb:','') as uniprot_idb, "
				+ "causal_regulatory_mechanism, intrxn_ids,"
				+ " publication_id, replace(confidence_values, 'SIGNOR-miscore:','') as confidence_value, causal_statement from ppi"
				+ " where"
				+ " uniprot_ida like '%uniprotkb:%' and type_a='psi-mi:MI:0326(protein)' and"
				+ " uniprot_idb like '%uniprotkb:%' and type_b='psi-mi:MI:0326(protein)'"
				+ " and uniprot_ida!=uniprot_idb"
				+ " and length(replace(confidence_values, 'SIGNOR-miscore:',''))>0" 
				+ " and replace(confidence_values, 'SIGNOR-miscore:','')!=''" 
				+ " and replace(confidence_values, 'SIGNOR-miscore:','')!='-'"
				+ " and confidence_values is not null" + limit;
		
		//System.out.println(query);
		
		PPIs = DBC.runQuery(query, columns);

		

		return PPIs;
		
		
	}
	
	
	
	private String indicateProgress(String message) {
		message = message + "|";
		
		System.out.print(message + "\r");
		
		return message;
	}
	
}
