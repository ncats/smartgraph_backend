/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.signorloader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import gov.nih.ncats.smrtgraph4j.DBConnector;
import gov.nih.ncats.smrtgraph4j.PostgreSQLDBConnector;

public class SignorLoader {

	private String signorFileName = null;
	private String uniprotFileName = null;
	
	private Set<PPI> PPIs = new LinkedHashSet<PPI> ();
	private Set<UniProtEntry> UPEs = new LinkedHashSet<UniProtEntry> ();
	
	private Map<String, String> columnMapping = new HashMap<String, String> ();
	
	  // Ref: https://stackoverflow.com/questions/13395114/how-to-initialize-liststring-object-in-java
	private final List<String> importantColumnNames = Arrays.asList("ID(s) interactor A",
			  											"ID(s) interactor B",
			  											"Interaction detection method(s)",
			  											"Publication Identifier(s)",
			  											"Causal regulatory mechanism",
			  											"Source database(s)",
			  											"Taxid interactor A",
			  											"Taxid interactor B",
			  											"Interaction identifier(s)",
			  											"Confidence value(s)",
			  											"Biological role(s) interactor A",
			  											"Biological role(s) interactor B",
			  											"Experimental role(s) interactor A",
			  											"Experimental role(s) interactor B",
			  											"Type(s) interactor A",
			  											"Type(s) interactor B",
			  											"Host organism(s)",
			  											"Creation date",
			  											"Update date",
			  											"Stoichiometry(s) interactor A",
			  											"Stoichiometry(s) interactor B",
			  											"Identification METHOD participant A",
			  											"Identification METHOD participant B",
			  											"Causal statement");
	

	
	
	
	
	  private Map<Integer, String> importantColumnIndices = new HashMap<Integer, String> ();
	  
	  
	  
	  public SignorLoader () {
		  columnMapping.put("ID(s) interactor A", "uniprot_ida");
		  columnMapping.put("ID(s) interactor B", "uniprot_idb");
		  columnMapping.put("Interaction detection method(s)", "intrxn_detection_methods");
		  columnMapping.put("Publication Identifier(s)", "publication_id");
		  columnMapping.put("Causal regulatory mechanism", "causal_regulatory_mechanism");
		  columnMapping.put("Source database(s)", "source_databases");
		  columnMapping.put("Taxid interactor A", "taxid_a");
		  columnMapping.put("Taxid interactor B", "taxid_b");
		  columnMapping.put("Interaction identifier(s)", "intrxn_ids");
		  columnMapping.put("Confidence value(s)", "confidence_values");
		  columnMapping.put("Biological role(s) interactor A", "biological_roles_a");
		  columnMapping.put("Biological role(s) interactor B", "biological_roles_b");
		  columnMapping.put("Experimental role(s) interactor A", "experimental_roles_a");
		  columnMapping.put("Experimental role(s) interactor B", "experimental_roles_b");
		  columnMapping.put("Type(s) interactor A", "type_a");
		  columnMapping.put("Type(s) interactor B", "type_b");
		  columnMapping.put("Host organism(s)", "host_organisms");
		  columnMapping.put("Creation date", "creation_date");
		  columnMapping.put("Update date", "update_date");
		  columnMapping.put("Stoichiometry(s) interactor A", "stoichiometry_a");
		  columnMapping.put("Stoichiometry(s) interactor B", "stoichiometry_b");
		  columnMapping.put("Identification METHOD participant A", "identification_method_a");
		  columnMapping.put("Identification METHOD participant B", "identification_method_b");
		  columnMapping.put("Causal statement", "causal_statement");

	  }
	  
	  public static void main(String[] args) throws IOException, SQLException {
	
		  // Ref: https://commons.apache.org/proper/commons-cli/usage.html
		  
		  CommandLineParser CLIParser = new DefaultParser();

		  Options options = new Options();
		  
		  SignorLoader signorLoader = new SignorLoader ();
		  
		  
		  
		  options.addOption( OptionBuilder.withLongOpt( "dbconfig" )
                  .withDescription( "Database config file. Lines in order: hostname, port, database, user, password ." )
                  .hasArg(true)
                  .withArgName("FILE")
                  .isRequired(true)
                  .create("c") );
		  options.addOption( OptionBuilder.withLongOpt( "uniprot" )
                  .withDescription("Uniprot DB Human proteins export file. Needs tocontain these fields:"
                  		+ " \"Entry, Entry name, Status, Protein names, Gene names, Organism, Length\"" )
                  .hasArg(true)
                  .withArgName("FILE")
                  .isRequired(true)
                  .create("u") );
		  options.addOption( OptionBuilder.withLongOpt( "pathway" )
                  .withDescription( "SIGNOR 2.0 causal tab format file. The quotes need to be stripped"
                  		+ " before providing it as an input. Especially the first couple characters"
                  		+ " might be invisible non-ASCII characters that need to be stripped." )
                  .hasArg(true)
                  .withArgName("FILE")
                  .isRequired(true)
                  .create("p") );		  
		  
		  DBConnector dbc = null;
	  
		  try {
			    // parse the command line arguments
			    CommandLine cli = CLIParser.parse( options, args );

		    	
		    	dbc = new PostgreSQLDBConnector(cli.getOptionValue("c"));
		    	dbc.assembleURL();
		    	dbc.connect();
		    	signorLoader.setSignorFileName(cli.getOptionValue("p"));
		    	signorLoader.setUniprotFileName(cli.getOptionValue("u"));

		    	System.out.println("[i] Parsing SIGNOR file..");
		    	signorLoader.parsePathwayData ();

		    	
		    	
		    	System.out.println("[i] Uploading SIGNOR PPI data to database.. (this can take a while)");
		    	signorLoader.uploadPPIs (dbc);

		    	
		    	
		    	
		    	System.out.println("[i] Parsing UniProt Human DB export file..");
		    	signorLoader.parseUniprotData();
		    	
		    	
		    	
		    	System.out.println("[i] Uploading UniProt data to database.. (this can take a while)");
		    	signorLoader.uploadUniprotEntries(dbc);
		    	
		    	dbc.disconnect();
		    	
		    	System.out.println("[ Done ]");
		    	
	
			    
		  }
		  catch (org.apache.commons.cli.ParseException e) {
			// TODO Auto-generated catch block
			  
			  
			  HelpFormatter formatter = new HelpFormatter();
			  formatter.printHelp( "SignorLoader", options );
			  
		  }
		 

		  
	  }
	  
	  
	  private void uploadPPIs (DBConnector dbc) throws SQLException {
		  
		  String query = "DROP TABLE IF EXISTS ppi";
		  dbc.runSimpleQuery(query);
		  
		  query = "CREATE TABLE ppi (" +
									  "uniprot_ida varchar(1000), " +
									  "uniprot_idb varchar(1000), " +
									  "intrxn_detection_methods text, " +
									  "publication_id text, " +
									  "causal_regulatory_mechanism text, " +
									  "source_databases text, " +
									  "taxid_a varchar(1000), " +
									  "taxid_b varchar(1000), " +
									  "intrxn_ids varchar(1000), " +
									  "confidence_values varchar(1000), " +
									  "biological_roles_a text, " +
									  "biological_roles_b text, " +
									  "experimental_roles_a text, " +
									  "experimental_roles_b text, " +
									  "type_a varchar(1000), " +
									  "type_b varchar(1000), " +
									  "host_organisms varchar(1000), " +
									  "creation_date varchar(1000), " +
									  "update_date varchar(1000), " +
									  "stoichiometry_a text, " +
									  "stoichiometry_b text, " +
									  "identification_method_a text, " +
									  "identification_method_b text, " +
									  "causal_statement text" +
									  ")";
		  
		  dbc.runSimpleQuery(query);
		  
		  for (PPI ppi: PPIs) {
			  //System.out.println(ppi.toSQLInsertStatement("ppi"));
			  dbc.runSimpleQuery(ppi.toSQLInsertStatement("ppi"));
		  }
	  }
	  
	  private void uploadUniprotEntries (DBConnector dbc) throws SQLException {
		  
		  String query = "DROP TABLE IF EXISTS uniprot";
		  dbc.runSimpleQuery(query);
		  
		  query = "CREATE TABLE uniprot (" +
									  "entry varchar(100), " +
									  "entry_name varchar(100), " +
									  "status text, " +
									  "protein_names text, " +
									  "gene_names text, " +
									  "organism varchar(1000), " +
									  "length integer" +
									  ")";
		  
		  dbc.runSimpleQuery(query);
		  
		  for (UniProtEntry upe: UPEs) {

			  dbc.runSimpleQuery(upe.toSQLInsertStatement("uniprot"));
		  }
	  }
	   

	  	  
	  
	  private void parsePathwayData () throws IOException {
		  
		  String line = null;
		  
		  String tmp[] = null;
		  boolean firstLine = true;
		  
		  
		  BufferedReader br = new BufferedReader (new FileReader (getSignorFileName()));
		  

		  PPI ppi = null;
		  
		  
		  while ((line = br.readLine()) != null) {
			  if (!line.startsWith("#")) {
				  line = line.replace("\"", "").trim();
				  //System.out.println (line);
				  
				  tmp = line.split("\t");
				  
				  if (!firstLine) {
					  
					  ppi = new PPI ();
					  
					  for (int i = 0; i < tmp.length; i++) {
						  
						  if (importantColumnIndices.keySet().contains(i)) {
							  
							  //System.out.print(tmp[i].trim());
							  //System.out.println (":::" + columnMapping.get(importantColumnIndices.get(i)) + " - " + tmp[i]);
							  ppi.setProperty(columnMapping.get(importantColumnIndices.get(i)), tmp[i]);


							  
							  
						  }
						  

						  
						  
					  }		

					  
					  PPIs.add(ppi);
					  //System.out.println ("");
					  
				  }	
				 
				  else {
					  
					  for (int i = 0; i < tmp.length; i++) {
						  
						  if (importantColumnNames.contains(tmp[i].trim())) {
							  
							  
							  //System.out.println(tmp[i].trim());
							  importantColumnIndices.put(i, tmp[i].trim());
							  
							  
							  
							  
							  
						  }
						  
					  }
					  
					  firstLine = false;
				  }
			  }
		  }
		  
		  
		  br.close();
	  }

	  
  	  
	  
	  private void parseUniprotData () throws IOException {
		  
		  String line = null;
		  
		  String tmp[] = null;
		  boolean firstLine = true;
		  
		  
		  BufferedReader br = new BufferedReader (new FileReader (getUniprotFileName()));
		  
		  UniProtEntry upe = null;
		  
		  
		  while ((line = br.readLine()) != null) {
			  if (!line.startsWith("#")) {
				  line = line.replace("\"", "").trim();
				  //System.out.println (line);
				  
				  tmp = line.split("\t");
				  
				  if (!firstLine) {
					  
					  upe = new UniProtEntry(tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5], tmp[6]);
					  
					  UPEs.add(upe);
					  
				  }	
				 
				  else {
					  firstLine = false;
				  }
			  }
		  }
		  
		  
		  br.close();
	  }

	  
	  
	  private void setSignorFileName (String fileName) {
		  this.signorFileName = fileName;
	  }
	  

	  public String getSignorFileName () {
		  return signorFileName;
	  }
	  
	  public String getUniprotFileName() {
		return uniprotFileName;
	  }

	  public void setUniprotFileName(String uniprotFileName) {
		  this.uniprotFileName = uniprotFileName;
	  }
	  
	  
	  
}
