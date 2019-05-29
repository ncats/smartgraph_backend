/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.querybuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gov.nih.ncats.smrtgraph4j.DBConnector;
import gov.nih.ncats.smrtgraph4j.DBOperations;
import gov.nih.ncats.smrtgraph4j.Neo4jDBConnector;
import gov.nih.ncats.smrtgraph4j.PathwayInteraction;
import gov.nih.ncats.smrtgraph4j.PostgreSQLDBConnector;
import gov.nih.ncats.smrtgraph4j.SmrtGraph4j;
import gov.nih.ncats.smrtgraph4j.Target;

public class SPTwoSetsOfTargets {

	private String startTargetsFile = null;
	private String endTargetsFile = null;
	private String outputFile = null;
	
	
	private Set<String> startNodes = null;
	private Set<String> endNodes = null;
	private Set<String> queries = null;
	private static final String NEWLINE = System.getProperty("line.separator");
	
	
	public SPTwoSetsOfTargets () {
		startNodes = new HashSet<String> ();
		endNodes = new HashSet<String> ();
		queries = new HashSet<String> ();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		
		SPTwoSetsOfTargets spTwoSetsOfTargets = new SPTwoSetsOfTargets();
		
    	CommandLineParser CLIParser = new DefaultParser();

    	Options options = new Options();
		  

		  
		  
		  
		options.addOption( OptionBuilder.withLongOpt("start_targets")
	              .withDescription("File containing the uniprot_ids of targets that will be the starting point of shortest paths. Comment lines"
		              		+ " starting with '#' are supported.") 
	              .hasArg(true)
	              .withArgName("FILE")
	              .isRequired(true)
	              .create("s") );
		options.addOption( OptionBuilder.withLongOpt("end_targets")
	              .withDescription("File containing the uniprot_ids of targets that will be the end point of shortest paths. Comment lines"
	              		+ " starting with '#' are supported.") 
                  .hasArg(true)
                  .withArgName("FILE")
                  .isRequired(true)
                  .create("e") );
		options.addOption( OptionBuilder.withLongOpt("output")
                .withDescription("Output: Neo4j (CYPHER) query file." )
                .hasArg(true)
                .withArgName("FILE")
                .isRequired(true)
                .create("o") );		  
		  		  
		DBConnector dbcChembl = null;
		DBConnector dbcPPI = null;
		DBConnector dbneo4j = null;
		
		try {
			// parse the command line arguments
			CommandLine cli = CLIParser.parse( options, args );
		
			spTwoSetsOfTargets.setStartTargetsFile(cli.getOptionValue("s"));
			spTwoSetsOfTargets.setEndTargetsFile(cli.getOptionValue("e"));
			spTwoSetsOfTargets.setOutputFile(cli.getOptionValue("o"));
			
			
			spTwoSetsOfTargets.parseStartTargetsFile ();
			spTwoSetsOfTargets.parseEndTargetsFile ();
			
			spTwoSetsOfTargets.assembleQueries ();	
			
			spTwoSetsOfTargets.writeQueries ();	
			

	    	System.out.println("[ Done ]");
			    	
			    
		  }
		  catch (org.apache.commons.cli.ParseException e) {
			// TODO Auto-generated catch block
			  
			  
			  HelpFormatter formatter = new HelpFormatter();
			  formatter.printHelp( "SPTwoSetsOfTargets", options );
			  
		  }
			
		
	}
	
	
	private void writeQueries () throws IOException {
		BufferedWriter bw = new BufferedWriter (new FileWriter (getOutputFile()));
		
		boolean first = true;
		
		for (String q: queries) {
			if (!first) {
				bw.write("UNION" + NEWLINE + q + NEWLINE);
			}
			else {
				bw.write(q + NEWLINE);
				first = false;
			}
		}
		
		bw.close();
	}
	
	private void assembleQueries () {
		
		
		// Ref: http://neo4j.com/docs/developer-manual/current/cypher/functions/list/
		// Ref: https://neo4j.com/developer/kb/all-shortest-paths-between-set-of-nodes/
		// Ref: https://neo4j.com/docs/developer-manual/current/cypher/clauses/where/#where-in-operator
		// Ref: https://neo4j.com/docs/developer-manual/current/cypher/clauses/union/
		
		String query = null;
		String endNodesClause = null;
		boolean first = true;
		
		for (String e: endNodes) {

			if (!first) {
				endNodesClause += ", '" + e + "'";
			}
			else {
				endNodesClause = "['" + e + "'";
				first = false;
			}
		}

		endNodesClause += "]";
		
		
		for (String s: startNodes) {
			query = "MATCH p=shortestPath((t:Target {uniprot_id: '" + s + "'})-[r:REGULATES*..]->(q:Target)) where q.uniprot_id IN " + endNodesClause + 
					" AND q.uniprot_id <> t.uniprot_id return p";
			
			this.queries.add(query);
			
		}
	}
	
	private void parseEndTargetsFile () throws IOException {
		BufferedReader br = new BufferedReader (new FileReader (getEndTargetsFile()));
		
		
		String line = null;
		
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				line = line.trim();
				this.endNodes.add(line);
			}
		}
		
		br.close();
	}
	
	
	private void parseStartTargetsFile () throws IOException {
		BufferedReader br = new BufferedReader (new FileReader (getStartTargetsFile()));
		
		
		String line = null;
		
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				line = line.trim();
				this.startNodes.add(line);
			}
		}
		
		br.close();
	}

	public String getStartTargetsFile() {
		return startTargetsFile;
	}

	public String getEndTargetsFile() {
		return endTargetsFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	private void setStartTargetsFile(String startTargetsFile) {
		this.startTargetsFile = startTargetsFile;
	}

	private void setEndTargetsFile(String endTargetsFile) {
		this.endTargetsFile = endTargetsFile;
	}

	private void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
