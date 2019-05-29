package gov.nih.ncats.smrtgraph4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nih.ncats.smrtgraph4j.signorloader.PPI;

public class UniProt2Gene {
    private String GeneSymbolFile = null;
    
    private List<U2G> U2Gs = null;
    
    public UniProt2Gene (String fName) {
        setGeneSymbolFile (fName);
        U2Gs = new ArrayList<U2G> ();
        
    }
    
    
    public void parseMapping () throws IOException {

        U2G u2g = null;

            
        String line = null;
        
        String tmp[] = null;
        boolean firstLine = true;
        
        
        BufferedReader br = new BufferedReader (new FileReader (getGeneSymbolFile()));
        


        
        
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
                line = line.trim();


                
                if (!firstLine) {

                    tmp = line.split("\t");

                    
                    if (tmp.length > 1) {
                        
                        u2g = new U2G (tmp[1].trim(), tmp[0].trim());
                        
                        // No need to check for duplicates, as a many-to-many mapping is expected between UniProt and GeneSymbols.
                        // -- GZK 11/05/2017
                        
                        this.U2Gs.add(u2g);
                        //System.out.println (tmp[1].trim() + " " + tmp[0].trim());
                    }
                } 
               
                else {
                    firstLine = false;
                }
            }
        }
        
        
        br.close();


        
    }
    
    public Set<String> getGeneSymbols (String uniprot) {
        Set<String> GSs = new HashSet<String> ();
        
        U2G u2g = null;
        
        for (int i = 0; i < U2Gs.size(); i++) {
            u2g = U2Gs.get(i);
            
            if (uniprot.equals(u2g.getUniProtID())) {
                GSs.add(u2g.getGeneSymbol());
            }
                
        }
        
        return GSs;
        
    }
    

    private void setGeneSymbolFile(String geneSymbolFile) {
        GeneSymbolFile = geneSymbolFile;
    }


    public String getGeneSymbolFile() {
        return GeneSymbolFile;
    }
    
    
    
    private class U2G {
        
        private String UniProtID = null;
        private String GeneSymbol = null;
        
        
        
        private U2G (String U, String G) {
            setUniProtID (U);
            setGeneSymbol (G);
        }
        
        private String getUniProtID () {
            return this.UniProtID;
        }
        
        private String getGeneSymbol () {
            return this.GeneSymbol;
        }
     
        private void setUniProtID (String U) {
            this.UniProtID = U;
        }
        
        private void setGeneSymbol (String G) {
            this.GeneSymbol = G;
        }        
        
        
    }
    
}
