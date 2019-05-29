/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j;



import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gov.nih.ncats.smrtgraph4j.signorloader.SignorLoader;
import gov.nih.ncats.smrtgraph4j.structure.C2P;
import gov.nih.ncats.smrtgraph4j.structure.Compound;
import gov.nih.ncats.smrtgraph4j.structure.Pattern;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class SmrtGraph4j {


    private final static String NEWLINE = System.getProperty("line.separator");
    private Set<UUID> UUIDs = new LinkedHashSet<UUID> ();
 
    private String Neo4JURL = null;
    private boolean test = false;

    private UniProt2Gene uniProt2Gene = null;
    
    
    public SmrtGraph4j () {
    	;
    }

 
   public static void main(String[] args) throws SQLException, IOException {
		
    	SmrtGraph4j smrtgraph4j = new SmrtGraph4j();
		
    	CommandLineParser CLIParser = new DefaultParser();

    	Options options = new Options();
		  

		  
		  
		  
		options.addOption( OptionBuilder.withLongOpt( "pathwaydbconfig" )
	              .withDescription( "Pathway Database config file. Lines in order: hostname, port, database, user, password ." )
	              .hasArg(true)
	              .withArgName("FILE")
	              .isRequired(true)
	              .create("p") );
		options.addOption( OptionBuilder.withLongOpt( "chembldbconfig" )
                  .withDescription( "ChEMBL Database config file. Lines in order: hostname, port, database, user, password ." )
                  .hasArg(true)
                  .withArgName("FILE")
                  .isRequired(true)
                  .create("c") );
		options.addOption( OptionBuilder.withLongOpt( "neo4jdbconfig" )
                .withDescription( "Neo4j Database config file. Lines in order: hostname, port, user, password ." )
                .hasArg(true)
                .withArgName("FILE")
                .isRequired(true)
                .create("n") );		
        options.addOption( OptionBuilder.withLongOpt( "genesymbols")
                .withDescription( "TAB-separated file of GENE_SYMBOL - UniProtID pairs." )
                .hasArg(true)
                .withArgName("FILE")
                .isRequired(true)
                .create("g") ); 		
      options.addOption( OptionBuilder.withLongOpt( "testmode" )
                .withDescription( "if defined, only 1000 records will be pulled for each node/edge type." )
                .hasArg(false)
                .isRequired(false)
                .create("t") ); 
		
		  		  
		DBConnector dbcChembl = null;
		DBConnector dbcPPI = null;
		DBConnector dbneo4j = null;
		
		try {
			// parse the command line arguments
			CommandLine cli = CLIParser.parse( options, args );
		
			// Ref: https://stackoverflow.com/questions/27645951/how-to-configure-user-and-password-for-neo4j-cluster-without-rest-api
			dbneo4j = new Neo4jDBConnector (cli.getOptionValue("n"));
			dbneo4j.assembleURL ();
			dbneo4j.connect();
			dbneo4j.clearAllData();
			
			
			dbcChembl = new PostgreSQLDBConnector(cli.getOptionValue("c"));
			dbcChembl.assembleURL();
			dbcChembl.connect();

			
			smrtgraph4j.uniProt2Gene = new UniProt2Gene (cli.getOptionValue("g"));
			smrtgraph4j.uniProt2Gene.parseMapping();
			
			if (cli.hasOption("t")) {
			    smrtgraph4j.setTest(true);
			}
			
			DBOperations ChemblDBO = new DBOperations (dbcChembl, smrtgraph4j.isTest());
			
			

		    
		    
		    
			
			
			dbcPPI = new PostgreSQLDBConnector(cli.getOptionValue("p"));
			dbcPPI.assembleURL();
			dbcPPI.connect();

			DBOperations PPIDBO = new DBOperations (dbcPPI, smrtgraph4j.isTest());

			    

			// Ref: https://neo4j.com/docs/operations-manual/current/upgrade/deployment-upgrading/


			
			
			Set<Target> ChemblTargets = new LinkedHashSet<Target> ();
			
	    	System.out.println("[i] Getting targets from ChEMBL DB.");
			
	    	ChemblTargets = smrtgraph4j.getTargetsofChembl(ChemblDBO, dbcPPI);


	    	Set<Target> PPITargets = new LinkedHashSet<Target> ();

	    	System.out.println("[i] Getting targets from SIGNOR PPI DB.");

	    	PPITargets = smrtgraph4j.getTargetsofPPIs(PPIDBO);

	    	
	    	System.out.println("[i] Get ChEMBL Target activity cutoffs (precomputed 80th percentiles).");
	    	smrtgraph4j.getActivityCutoffs (ChemblTargets, ChemblDBO);
	    	
	    	System.out.println("[i] Loading targets to Neo4j.");
	    	smrtgraph4j.loadTargetsToNeo4j(ChemblTargets, PPITargets, dbneo4j);
            
	    	//System.out.println("[i] Writing targets to CSV.");
            //smrtgraph4j.writeTargetsToCSV(ChemblTargets, PPITargets);
	    	
	        
	        


	    	
	        System.out.println("[i] Getting PPIs from PPI DB.");
	    	Map<String, PathwayInteraction> PPIs = smrtgraph4j.getPPIs(PPIDBO, "SIGNOR2.0");
	    	
	    	
	    	
	        System.out.println("[i] Loading PPIs to Neo4j.");
	    	smrtgraph4j.loadPPIsToNeo4j (PPIs, dbneo4j);
	    	
	    	
	        //System.out.println("[i] Writing PPIs to CSV.");
            //smrtgraph4j.writePPIsToCSV(PPIs);
            
            
	    	dbcPPI.disconnect();
  	
	    	
	    	
	    	
	    	
	    	
	    	
	    	
	    	System.out.println("[i] Getting compounds from ChEMBL DB.");

	        Set<Compound> ChemblCompounds = new LinkedHashSet<Compound> ();
	    	
	    	ChemblCompounds = smrtgraph4j.getCompoundsofChembl(ChemblDBO);
	    	
	    	
	    	System.out.println("[i] Loading compounds to Neo4j.");
	    	smrtgraph4j.loadCompoundsToNeo4j (ChemblCompounds, dbneo4j);
	        
	    	//System.out.println("[i] Write compounds to CSV.");
	        //smrtgraph4j.writeCompoundsToCSV(ChemblCompounds);    	
	    	
	        
	        
	        
	        
	        
	        
	        
	    	
	    	System.out.println("[i] Getting patterns from ChEMBL DB.");
	    	
            Set<Pattern> ChemblPatterns = new LinkedHashSet<Pattern> ();
            
            ChemblPatterns = smrtgraph4j.getPatternsofChembl(ChemblDBO);	    	
	    	
	    	
	    	
	    	System.out.println("[i] Loading patterns to Neo4j.");    	
	        smrtgraph4j.loadPatternsToNeo4j(ChemblPatterns, dbneo4j);
	        
	        //System.out.println("[i] Writing patterns to CSV.");       
	        //smrtgraph4j.writePatternsToCSV(ChemblPatterns);
	        
	        
	        
	        
	        
	    	

	    	System.out.println("[i] Getting Compound-to-Pattern associations from ChEMBL DB.");
	    	
            Set<C2P> C2Ps = new LinkedHashSet<C2P> ();
            C2Ps = smrtgraph4j.getC2Ps (ChemblDBO);
	    	
	    	System.out.println("[i] Loading Compound-to-Pattern associations to Neo4j.");
	    	smrtgraph4j.loadC2PsToNeo4j (C2Ps, dbneo4j); 

            //System.out.println("[i] Writing Compound-to-Pattern associations to CSV.");
            //smrtgraph4j.writeC2PsToCSV(C2Ps); 
	    
	    	
            
            
            
            
            
            
	    	
            System.out.println("[i] Getting Compound-to-Target associations from ChEMBL DB.");
            Set<C2T> C2Ts = new LinkedHashSet<C2T> ();
            C2Ts = smrtgraph4j.getC2Ts (ChemblDBO);
            
            
            
            System.out.println("[i] Loading Compound-to-Target associations to Neo4j.");
            smrtgraph4j.loadC2TsToNeo4j (C2Ts, dbneo4j);

            //System.out.println("[i] Writing Compound-to-Target associations to CSV.");
            //smrtgraph4j.writeC2TsToCSV(C2Ts);
            
            
            
            
	    	
            System.out.println("[i] Getting Pattern-to-Target associations from ChEMBL DB.");
            Set<P2T> P2Ts = new LinkedHashSet<P2T> ();
            P2Ts = smrtgraph4j.getP2Ts (ChemblDBO);
            
            
            
            System.out.println("[i] Loading Pattern-to-Target associations to Neo4j.");
            smrtgraph4j.loadP2TsToNeo4j (P2Ts, dbneo4j);

            //System.out.println("[i] Writing Pattern-to-Target associations to CSV.");
            //smrtgraph4j.writeP2TsToCSV(P2Ts);
            
            
            

			dbcChembl.disconnect();
			dbneo4j.disconnect();
			
			
	    	System.out.println("[ Done ]");
			   
	    	
	    	
	    	/*
	    	CREATE CONSTRAINT ON (t:Target) ASSERT t.uuid IS UNIQUE
	    	CREATE CONSTRAINT ON (t:Target) ASSERT t.uniprot_id IS UNIQUE
	    	CREATE CONSTRAINT ON (c:Compound) ASSERT c.hash IS UNIQUE
	    	CREATE CONSTRAINT ON (c:Compound) ASSERT c.uuid IS UNIQUE
	    	CREATE CONSTRAINT ON (p:Pattern) ASSERT p.hash IS UNIQUE
	    	CREATE CONSTRAINT ON (p:Pattern) ASSERT p.uuid IS UNIQUE
	    	*/
			    
		  }
		  catch (org.apache.commons.cli.ParseException e) {
			// TODO Auto-generated catch block
			  
			  
			  HelpFormatter formatter = new HelpFormatter();
			  formatter.printHelp( "Smrtgraph4j", options );
			  
		  }
		 

    }
 
   public void getActivityCutoffs (Set <Target> targets, DBOperations DBOP) throws SQLException {
	   Iterator<Target> iT = targets.iterator();
	   
	   Target target = null;
	   
	   double cutoff = 0.0;
	   
	   while (iT.hasNext()) {
		   target = iT.next();
		   cutoff = DBOP.getTargetActivityCutoff (target.getUniProtID());
		   target.setActivityCutoff(cutoff);
	   }
	   
	   
   }
   
   
   public void writeTargetsToCSV (Set<Target> ChemblTargets, Set<Target> PPITargets) throws IOException {
       
       String fName = "smrtgraph_targets.csv";
       
       BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
       
       String header = "uniprot_id\tuuid\tname\tfullname" + NEWLINE;
       
       String csvEntry = null;
       
       bw.write(header);
       
       
       
       Set<String> uniprot_ids = new HashSet<String> ();
       String upid = null;
       
       
       
       for (Target target:ChemblTargets) {
           upid = target.getUniProtID();
           
           if (!uniprot_ids.contains(upid)) {
               uniprot_ids.add(upid);
               
               csvEntry = target.toCSV();
               
               bw.write(csvEntry + NEWLINE);
               
               
           }
       }
       
       
       for (Target target:PPITargets) {
           upid = target.getUniProtID();
           
           if (!uniprot_ids.contains(upid)) {
               uniprot_ids.add(upid);
               
               csvEntry = target.toCSV();
               
               bw.write(csvEntry + NEWLINE);

               
           }
       }
       
       bw.close ();
   
   }
 
   
   
   
   public void writePPIsToCSV (Map<String, PathwayInteraction> PPIs) throws IOException {
       
       
       String fName = "smrtgraph_ppi.csv";
       
       BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
       

       String header = "uniprot_ida\tuniprot_idb\tmechanisms\tsignor_intrxn_ids\treferences\t" + 
               "ppi_uid\tuuid\tmax_confidence_value\tcausal_statements\tconfidence_values" + NEWLINE;
       
       String csvEntry = null;
       
       bw.write(header);
 
        
        Iterator<PathwayInteraction> iT = PPIs.values().iterator();
        
        PathwayInteraction PPI = null;
        while (iT.hasNext()) {
            PPI = iT.next();
            csvEntry = PPI.toCSV();
 
        
            bw.write(csvEntry  + NEWLINE);             
            

            
        }
        
        
        bw.close ();
   }  
   

   public void writeCompoundsToCSV (Set<Compound> ChemblCompounds) throws IOException {
       
       String fName = "smrtgraph_compound.csv";
       
       BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
       

       
       String header = "hash\tuuid\tsmiles\tcompound_id\tnostereo_hash" + NEWLINE;
       
       String csvEntry = null;
       
       bw.write(header);
       
       
     
       Set<String> hash_strings = new HashSet<String> ();
       String hash_string = null;
       

       
       
       for (Compound compound: ChemblCompounds) {
           hash_string = compound.getHash();
           
           if (!hash_strings.contains(hash_string)) {
               hash_strings.add(hash_string);
               
               csvEntry = compound.toCSV();
               
               bw.write(csvEntry + NEWLINE);
               
           }
       }
       
       bw.close();
   
   }
   
   
 public void writePatternsToCSV (Set<Pattern> ChemblPatterns) throws IOException  {
       
       
       String fName = "smrtgraph_patterns.csv";
       
       BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
       

       
       String header = "hash\tuuid\tsmiles\tpattern_id\tpattern_type" + NEWLINE;

       
       String csvEntry = null;
       
       bw.write(header);
       
       Set<String> hash_strings = new HashSet<String> ();
       String hash_string = null;
       

       

       
       
       for (Pattern pattern: ChemblPatterns) {
           hash_string = pattern.getHash();
           
           if (!hash_strings.contains(hash_string)) {
               hash_strings.add(hash_string);
               
               csvEntry = pattern.toCSV();

                bw.write(csvEntry + NEWLINE);

               
           }
       }
       
     bw.close();
   
   }
     
   
 
 
 
 public void writeC2PsToCSV (Set<C2P> C2Ps) throws IOException {
     
     
     String fName = "smrtgraph_c2p.csv";
     
     BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
     

  
  
     String header = "compound_id\tpattern_id\tratio\tunique_label\tuuid\tislargest" + NEWLINE;
      
     
     String csvEntry = null;
     
     bw.write(header);
      

      
      Iterator<C2P> iT = C2Ps.iterator();
      
      C2P c2p = null;
      
      
      while (iT.hasNext()) {
          c2p = iT.next();
          
          
          
          csvEntry = c2p.toCSV();
             
          bw.write(csvEntry + NEWLINE);
          
      }
      
      
       bw.close();
}  
 
 
 

 public void writeC2TsToCSV (Set<C2T> C2Ts) throws IOException {
     
     
     String fName = "smrtgraph_c2t.csv";
     
     BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
     

     String header = "compound_id\tuniprot_id\tunique_label\tuuid\tactivity\tactivity_type" + NEWLINE;

     
     String csvEntry = null;
     
     bw.write(header);
      

      
      Iterator<C2T> iT = C2Ts.iterator();
      
      C2T c2t = null;

      
      while (iT.hasNext()) {
          c2t = iT.next();

          csvEntry = c2t.toCSV();

          bw.write(csvEntry + NEWLINE);
          

          
      }
      
      bw.close();
} 
 
  
 
 
   
 public void writeP2TsToCSV (Set<P2T> P2Ts) throws IOException  {
     String fName = "smrtgraph_p2t.csv";
     
     BufferedWriter bw = new BufferedWriter (new FileWriter (fName));
     

     String header = "pattern_id\tuniprot_id\tunique_label\tuuid" + NEWLINE;

    
        

     
     String csvEntry = null;
     
     bw.write(header);
        Iterator<P2T> iT = P2Ts.iterator();
        
        P2T p2t = null;
        
                
        while (iT.hasNext()) {
            p2t = iT.next();
        
        
        
          csvEntry = p2t.toCSV();

          bw.write(csvEntry + NEWLINE);
            
        
        
            
        }
        
        bw.close();
  }
  
   
   
   
   
   
   
   
   
   
   
   public void loadC2TsToNeo4j (Set<C2T> C2Ts, DBConnector DBC) throws SQLException {
        
        String query = null;
        
        Iterator<C2T> iT = C2Ts.iterator();
        
        C2T c2t = null;
        
        
        int counter = 0;
        
        while (iT.hasNext()) {
            c2t = iT.next();
            query = c2t.toNeo4jInsertStatement();
            DBC.runSimpleQuery(query);
            
            counter++;
            if (counter % 1000 == 0) {
                System.out.println("[p] Nr. of C2T edges uploaded: " + counter);
            }
            
        }
  }
   
   
   
   
   public Set<C2T> getC2Ts (DBOperations DBOP) throws SQLException {
       
       Set<C2T> C2Ts = new LinkedHashSet<C2T> ();
  
  
       List<String> c2tRecords = new ArrayList<String> ();
  

  
       c2tRecords = DBOP.acquireC2Ts();
  
  
  
       String tmp[] = null;
  
       C2T c2t = null;
       
       
       for (String c2tRecord: c2tRecords) {
           //System.out.println(targetRecord);
           tmp = c2tRecord.split("\t");
  
           c2t = new C2T(UUIDs, tmp[0], tmp[1], Double.parseDouble(tmp[2]));
  
           //System.out.println(t.getChEMBL_idL());
  
           C2Ts.add(c2t); 
       }
  
  
       return C2Ts;
  
   }    
   
   
   public void loadP2TsToNeo4j (Set<P2T> P2Ts, DBConnector DBC) throws SQLException {
        
        String query = null;
        
        Iterator<P2T> iT = P2Ts.iterator();
        
        P2T p2t = null;
        
        
        int counter = 0;
        
        while (iT.hasNext()) {
            p2t = iT.next();
            query = p2t.toNeo4jInsertStatement();
            DBC.runSimpleQuery(query);
            
            counter++;
            if (counter % 1000 == 0) {
                System.out.println("[p] Nr. of P2T edges uploaded: " + counter);
            }
            
        }
  }
   
   
   
   
   public Set<P2T> getP2Ts (DBOperations DBOP) throws SQLException {
       
       Set<P2T> P2Ts = new LinkedHashSet<P2T> ();
  
  
       List<String> p2tRecords = new ArrayList<String> ();
  

  
       //p2tRecords = DBOP.acquireP2Ts(7);
       p2tRecords = DBOP.acquireP2Ts();
       
  
  
       String tmp[] = null;
  
       P2T p2t = null;
       
       
       for (String p2tRecord: p2tRecords) {
           //System.out.println(targetRecord);
           tmp = p2tRecord.split("\t");
  
           p2t = new P2T(UUIDs, tmp[0], tmp[1]);
  
           //System.out.println(t.getChEMBL_idL());
  
           P2Ts.add(p2t); 
       }
  
  
       return P2Ts;
  
   }  
   
   
   
   public void loadC2PsToNeo4j (Set<C2P> C2Ps, DBConnector DBC) throws SQLException {
  		
  		String query = null;
 		
  		Iterator<C2P> iT = C2Ps.iterator();
  		
  		C2P c2p = null;
  		
        int counter = 0;
  		
  		while (iT.hasNext()) {
  			c2p = iT.next();
  			query = c2p.toNeo4jInsertStatement();
  			DBC.runSimpleQuery(query);
  			
            counter++;
            if (counter % 1000 == 0) {
                System.out.println("[p] Nr. of C2P edges uploaded: " + counter);
            }

  			
  		}
  }
   
   
   
  	public Set<C2P> getC2Ps (DBOperations DBOP) throws SQLException {
  		
		 Set<C2P> C2Ps = new LinkedHashSet<C2P> ();
	
	
		 List<String> c2pRecords = new ArrayList<String> ();
	

	
		 c2pRecords = DBOP.acquireC2Ps();
	
	
	
		 String tmp[] = null;
	
		 C2P c2p = null;
		 
		 
		 for (String c2pRecord: c2pRecords) {
			 //System.out.println(targetRecord);
			 tmp = c2pRecord.split("\t");
	
			 c2p = new C2P(UUIDs, tmp[0], tmp[1], tmp[2], Double.parseDouble(tmp[3]));
	
			 //System.out.println(t.getChEMBL_idL());
	
			 C2Ps.add(c2p); 
		 }
	
	
		 return C2Ps;
	
	 }    
  	
   
   
   public void loadPatternsToNeo4j (Set<Pattern> ChemblPatterns, DBConnector DBC) throws SQLException {
       
       Set<String> hash_strings = new HashSet<String> ();
       String hash_string = null;
       
       String query = null;
       
       int counter = 0;

       
       
       for (Pattern pattern: ChemblPatterns) {
           hash_string = pattern.getHash();
           
           if (!hash_strings.contains(hash_string)) {
               hash_strings.add(hash_string);
               
               query = pattern.toNeo4jInsertStatement();
               
               DBC.runSimpleQuery(query);
            
               
               counter++;
               if (counter % 1000 == 0) {
                   System.out.println("[p] Nr. of Pattern nodes uploaded: " + counter);
               }
               
           }
       }
       
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (p:Pattern) ASSERT p.pattern_id IS UNIQUE";
       DBC.runSimpleQuery(query);
       
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (p:Pattern) ASSERT p.hash IS UNIQUE";        
       DBC.runSimpleQuery(query);
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (p:Pattern) ASSERT p.uuid IS UNIQUE";        
       DBC.runSimpleQuery(query);
   
   }

  	
   	
   
	 public Set<Pattern> getPatternsofChembl(DBOperations DBOP) throws SQLException {
       
       Set<Pattern> patterns = new LinkedHashSet<Pattern> ();
       
       
       List<String> patternRecords = new ArrayList<String> ();
       
       
       patternRecords = DBOP.acquirePatternsOfChembl();
       

       
       String tmp[] = null;
       
       Pattern pattern = null;
       for (String patterndRecord: patternRecords) {
           //System.out.println(targetRecord);
           tmp = patterndRecord.split("\t");
           
           pattern = new Pattern(UUIDs, tmp[0], tmp[1], tmp[2], tmp[3]);
           
           //System.out.println(t.getChEMBL_idL());
           
           patterns.add(pattern); 
       }
       
       
       return patterns;
       
   } 
   
   public void loadCompoundsToNeo4j (Set<Compound> ChemblCompounds, DBConnector DBC) throws SQLException {
       
       Set<String> hash_strings = new HashSet<String> ();
       String hash_string = null;
       
       String query = null;
       
       int counter = 0;
      

       
       
       for (Compound compound: ChemblCompounds) {
           hash_string = compound.getHash();
           
           if (!hash_strings.contains(hash_string)) {
               hash_strings.add(hash_string);
               
               query = compound.toNeo4jInsertStatement();
               
               DBC.runSimpleQuery(query);
               
               
               counter++;
               if (counter % 1000 == 0) {
                   System.out.println("[p] Nr. of Compound nodes uploaded: " + counter);
               }
               
           }
       }
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (c:Compound) ASSERT c.compound_id IS UNIQUE";
       DBC.runSimpleQuery(query);
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (c:Compound) ASSERT c.hash IS UNIQUE";        
       DBC.runSimpleQuery(query);
       
       
       // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
       query = "CREATE CONSTRAINT ON (c:Compound) ASSERT c.uuid IS UNIQUE";        
       DBC.runSimpleQuery(query);
   
   }

   
   
   public Set<Compound> getCompoundsofChembl(DBOperations DBOP) throws SQLException {
       
       Set<Compound> compounds = new LinkedHashSet<Compound> ();
       
       
       List<String> compoundRecords = new ArrayList<String> ();
       
       
       // Load targets from signor, and chembl
       compoundRecords = DBOP.acquireCompoundsOfChembl();
       
       //System.out.println("[i] ChEMBL targets acquired: " + targetRecords.size());
       
       
       String tmp[] = null;
       
       Compound compound = null;
       for (String compoundRecord: compoundRecords) {
           //System.out.println(targetRecord);
           tmp = compoundRecord.split("\t");
           
           compound = new Compound(UUIDs, tmp[1], tmp[0], tmp[2]);
           
           //System.out.println(t.getChEMBL_idL());
           
           compounds.add(compound); 
       }
       
       
       return compounds;
       
   }       
   
   
   
   
   
   public void loadPPIsToNeo4j (Map<String, PathwayInteraction> PPIs, DBConnector DBC) throws SQLException {
   		
   		String query = null;
   		
        int counter = 0;
   		
        
   		Iterator<PathwayInteraction> iT = PPIs.values().iterator();
   		
   		PathwayInteraction PPI = null;
   		while (iT.hasNext()) {
   			PPI = iT.next();
   			query = PPI.toNeo4jInsertStatement();
   			DBC.runSimpleQuery(query);
   			
   			
            counter++;
            if (counter % 1000 == 0) {
                System.out.println("[p] Nr. of PPI edges uploaded: " + counter);
            }
   			
   		}
   }

   	
   	
   	
   public Map<String, PathwayInteraction> getPPIs(DBOperations DBOP, String sourceDataBase) throws SQLException {
   		
   		String record = null;
   		
   		String tmp[] = null;
   		
   		
   		String uniprot_ida = null;
   		String uniprot_idb = null;
   		String name = null;
   		String mechanism= null;
   		String intrxn_id = null;
   		String publication_id = null;
   		double confidence_value = 0.0;
   		String causal_statement = null;
   		
   		PathwayInteraction PPI = null;
   		PathwayInteraction existingPPI = null;
   		
   		Map<String, PathwayInteraction> PPIs = new HashMap<String, PathwayInteraction> ();
   		
   		List<String> PPIRecords = DBOP.acquirePPIs ();
   		
   		for (String PPIRecord: PPIRecords) {
   			record = PPIRecord.trim();
   			
   			//System.out.println(record);
   			
   			tmp = record.split("\t");
   			
   			uniprot_ida = tmp[0].trim();
   	   		uniprot_idb = tmp[1].trim();
   	   		mechanism = tmp[2].trim();
   	   		intrxn_id = tmp[3].trim();
   	   		publication_id = tmp[4].trim();	
   	   		confidence_value = Double.parseDouble(tmp[5].trim());
   	   		causal_statement = tmp[6].trim();
   			
   			
   			PPI = new PathwayInteraction(UUIDs, uniprot_ida, uniprot_idb, mechanism, //
   			                            intrxn_id, publication_id, confidence_value, //
   			                            causal_statement, sourceDataBase);
   			
   			
   			if (!PPIs.containsKey(PPI.getUnique_intrxn_label())) {
   				PPIs.put(PPI.getUnique_intrxn_label(), PPI);
   			}
   			else {
   				existingPPI = PPIs.get(PPI.getUnique_intrxn_label());
   				existingPPI.addMechanism(mechanism);
   				existingPPI.addCausalStatement(causal_statement);
   				existingPPI.addReference(publication_id);
   				existingPPI.addInteraction(intrxn_id);
   				existingPPI.addConfidenceValue(confidence_value);


   			}
   		}
   		
   		return PPIs;
   	}     	
   
     	
   	
    
    public Set<Target> getTargetsofChembl(DBOperations DBOP, DBConnector dbcPPI) throws SQLException {
    	
    	Set<Target> targets = new LinkedHashSet<Target> ();
    	Set<String> IDs_ChEMBL = null;
        Set<String> IDs_GENE_SYMBOL = null;
        
		List<String> targetRecords = new ArrayList<String> ();
		
		
		// Load targets from signor, and chembl
		targetRecords = DBOP.acquireTargetsOfChembl(dbcPPI);
    	
		//System.out.println("[i] ChEMBL targets acquired: " + targetRecords.size());
    	
		
		String tmp[] = null;
		
		Target t = null;
    	for (String targetRecord: targetRecords) {
    		//System.out.println(targetRecord);
    		tmp = targetRecord.split("\t");
    		
    		t = new Target(UUIDs, tmp[0], tmp[1], tmp[2]);
    		

    		t.setGENE_SYMBOLs (uniProt2Gene.getGeneSymbols(t.getUniProtID()));
    		
            
    		
    		
    		//System.out.println(IDs_GENE_SYMBOL);
    		
    		targets.add(t);	
    	}
    	
    	
    	return targets;
    	
    }

 
    
    public Set<Target> getTargetsofPPIs(DBOperations DBOP) throws SQLException {
    	
    	Set<Target> targets = new LinkedHashSet<Target> ();
    	
		List<String> targetRecords = new ArrayList<String> ();
		
		
		// Load targets from signor, and chembl
		targetRecords = DBOP.acquireTargetsOfPPIs();
    	
		//System.out.println("[i] PPI targets acquired: " + targetRecords.size());
    	
		
		String tmp[] = null;
		
		Target t = null;
    	for (String targetRecord: targetRecords) {
    		//System.out.println(targetRecord);
    		tmp = targetRecord.split("\t");
    		
    		t = new Target(UUIDs, tmp[0], tmp[1], tmp[2]);
    		
    		//System.out.println(t.getChEMBL_idL());
    		
            t.setGENE_SYMBOLs (uniProt2Gene.getGeneSymbols(t.getUniProtID()));
    		
    		targets.add(t);	
    	}
    	
    	return targets;
    }    
	
	
	
	public void loadTargetsToNeo4j (Set<Target> ChemblTargets, Set<Target> PPITargets, DBConnector DBC) throws SQLException {
		
		Set<String> uniprot_ids = new HashSet<String> ();
		String upid = null;
		
		String query = null;
		
        int counter = 0;
		

		
		for (Target target:ChemblTargets) {
			upid = target.getUniProtID();
			
			if (!uniprot_ids.contains(upid)) {
				uniprot_ids.add(upid);
				
				query = target.toNeo4jInsertStatement();
				
				DBC.runSimpleQuery(query);
				
                counter++;
                if (counter % 1000 == 0) {
                    System.out.println("[p] Nr. of Target nodes uploaded: " + counter);
                }
				
			}
		}
		
		
		for (Target target:PPITargets) {
			upid = target.getUniProtID();
			
			if (!uniprot_ids.contains(upid)) {
				uniprot_ids.add(upid);
				
				query = target.toNeo4jInsertStatement();
				
				DBC.runSimpleQuery(query);
				
                counter++;
                if (counter % 1000 == 0) {
                    System.out.println("[p] Nr. of Target nodes uploaded: " + counter);
                }
				
			}
		}
		
		

		
	    // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
        query = "CREATE CONSTRAINT ON (t:Target) ASSERT t.uniprot_id IS UNIQUE";        
        DBC.runSimpleQuery(query);
        
        // Ref: http://neo4j.com/docs/developer-manual/current/cypher/schema/constraints/
        query = "CREATE CONSTRAINT ON (t:Target) ASSERT t.uuid IS UNIQUE";        
        DBC.runSimpleQuery(query);
		
	
	}

	
	
 
	public String getNeo4JURL() {
		return Neo4JURL;
	}

	public void setNeo4JURL(String neo4jPath) {
		Neo4JURL = neo4jPath;
	}


    public boolean isTest() {
        return test;
    }


    private void setUUIDs(Set<UUID> uUIDs) {
        UUIDs = uUIDs;
    }


    private void setTest(boolean test) {
        this.test = test;
    }


}
