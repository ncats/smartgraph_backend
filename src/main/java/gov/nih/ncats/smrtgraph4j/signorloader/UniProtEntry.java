/* 
 * Author: Gergely Zahoranszky-Kohalmi, PhD
 * 
 * Organization: National Center for Advancing Translational Sciences
 * 
 * Email: zahoranszkykog2@mail.nih.gov
 * 
 */
package gov.nih.ncats.smrtgraph4j.signorloader;

public class UniProtEntry {

	
	private String entry = null;
	private String entry_name = null; 
	private String status = null;
	private String protein_names = null;
	private String gene_names = null;
	private String organism = null;
	private String length = null;
	
	
	
	public UniProtEntry (String entry, String entry_name, String status, //
			String protein_names, String gene_names, String organism, String length) {

		setEntry(entry);
		setEntry_name(entry_name);
		setStatus(status);
		setProtein_names(protein_names);
		setGene_names(gene_names);
		setOrganism(organism);
		setLength(length);
		
	}
	
	public String toSQLInsertStatement(String table) {
		String query = "INSERT INTO " + table + " (entry, entry_name, status, protein_names, gene_names, "
				+ "organism, length) VALUES ";
		
		query += "('" + entry + "', '" + entry_name + "', '" + status.replace("'", "") + "', '" +
		protein_names.replace("'", "") + "', '";
		
		query += gene_names.replace("'", "") + "', '" + organism.replace("'", "") +
				"', '" + length + "')"; 
		
		
		//System.out.println(query);
		return query;
		
	}
	
	
	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getEntry_name() {
		return entry_name;
	}

	public void setEntry_name(String entry_name) {
		this.entry_name = entry_name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProtein_names() {
		return protein_names;
	}

	public void setProtein_names(String protein_names) {
		this.protein_names = protein_names;
	}

	public String getGene_names() {
		return gene_names;
	}

	public void setGene_names(String gene_names) {
		this.gene_names = gene_names;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}
	
	
}
