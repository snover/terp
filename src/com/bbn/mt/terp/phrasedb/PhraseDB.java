package com.bbn.mt.terp.phrasedb;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.PrimaryIndex; 
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.model.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.io.*;

public class PhraseDB {
    public PhraseDB(String base) {
	this.base = base;
	// Create the directory for the DB if it doesn't exist!
	File baseFH = new File(base);
	if (! baseFH.exists()) {
	    System.err.println("Warning, directory " + base + " does not exist.  Attempting to create it...");
	    if (baseFH.mkdirs()) {
		System.err.println("Phrase DB created phrase database: " + base);
	    } else {
		System.err.println("ERROR! Phrase DB failed to create phrase database: " + base);
		System.exit(-1);
	    }
	}
	
    }

    private boolean readonly = true;
    private String base = ""; 
    private Environment myEnv = null;
    private EntityStore store = null;

    // Join string[] using spaces
    private String sjoin(Object[] ar) {
	return sjoin(ar, 0, ar.length-1);
    }
    private String sjoin(Object[] ar, int s, int e) {
	String toret = "";
	for (int i = s; i <= e; i++) {
	    if (i != s) toret += " ";
	    toret += ar[i];
	}
	return toret;
    }

    public Set<PhraseEntry> get_phrases(String[][] refwdsAr, 
					String[][] hypwdsAr) {
	HashSet<PhraseEntry> toret = new HashSet<PhraseEntry>();
	HashSet<String> searched = new HashSet<String>();
	
	HashSet<Integer> hypset = new HashSet<Integer>();
	for (String[] hypwds : hypwdsAr) {
	    Integer[] hypids = phraseStrToInt(hypwds);
	    hypset.addAll(Arrays.asList(hypids));
	}
	for (String[] refwds : refwdsAr) {
	    Integer[] refids = phraseStrToInt(refwds);
	    try {		
		List<Integer> li = Arrays.asList(refids);		
		for (int i = 0; i < refids.length; i++) {
		    WordInfo wi = wordById.get(refids[i]);
		    int max_len = 0;
		    if (wi != null) {
			max_len = wi.maxlen;
			if ((i+max_len) > refids.length) 
			    max_len = refids.length - i;
		    }
		    
		    for (int j = i; j < (max_len + i); j++) {
			String searchkey = sjoin(refids, i, j);
			if (searched.contains(searchkey)) continue;
			searched.add(searchkey);
			
			EntityIndex<Integer,PhraseNumEntry> subIndex = 
			    phraseByRef.subIndex(sjoin(refids, i, j));
			EntityCursor<PhraseNumEntry> cursor = subIndex.entities();
			try {
			    for (PhraseNumEntry pe : cursor) {
				boolean ok = true;
				for (int hw : pe.getHypIds()) {
				    if (! hypset.contains(hw)) {
					ok = false;
					break;
				    }    
				}
				if (ok) {
				    toret.add(convertPhraseEntry(pe));
				}
			    }
			} finally {
			    cursor.close();
			}
		    }
		}
	    } catch(DatabaseException dbe) {
		closeDB();
		System.err.println("Error scanning database(" + base + 
				   "): " + dbe.toString());
		System.exit(-1);
	    }
	}
	return toret;
    }

    public Set<PhraseEntry> get_phrases(String[] refwds, String[] hypwds) {
	Integer[] refids = phraseStrToInt(refwds);
	Integer[] hypids = phraseStrToInt(hypwds);	

	HashSet<PhraseEntry> toret = new HashSet<PhraseEntry>();
	HashSet<Integer> hypset = new HashSet<Integer>(Arrays.asList(hypids));
	
	try {
	    List<Integer> li = Arrays.asList(refids);		
	    for (int i = 0; i < refids.length; i++) {
		WordInfo wi = wordById.get(refids[i]);
		int max_len = 0;
		if (wi != null) {
		    max_len = wi.maxlen;
		    if ((i+max_len) > refids.length) 
			max_len = refids.length - i;
		}

		for (int j = i; j < (max_len + i); j++) {
		    EntityIndex<Integer,PhraseNumEntry> subIndex = 
			phraseByRef.subIndex(sjoin(refids, i, j));
		    EntityCursor<PhraseNumEntry> cursor = subIndex.entities();
		    try {
			for (PhraseNumEntry pe : cursor) {
			    boolean ok = true;
			    for (int hw : pe.getHypIds()) {
				if (! hypset.contains(hw)) {
				    ok = false;
				    break;
				}    
			    }
			    if (ok) {
				toret.add(convertPhraseEntry(pe));
			    }
			}
		    } finally {
			cursor.close();
		    }
		}
	    }
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error scanning database(" + base + 
			       "): " + dbe.toString());
	    System.exit(-1);
	}
	return toret;
    }

    private String[] phraseIntToStr(Integer[] data) {
	String[] toret = new String[data.length];
	try {
	    for (int i = 0; i < data.length; i++) {
		WordInfo wi = wordById.get(data[i]);
		if (wi == null) {
		    closeDB();
		    System.err.println("Error:  Cannot find WordInfo for word id: " + data[i]);
		    System.exit(-1);
		}	    
		toret[i] = wi.word.intern();
	    }	
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error adding converting phrase Integer[] to String[]: " + dbe.toString());
	    System.exit(-1);
	}
	return toret;
    }
    private String[] phraseIntToStr(int[] data) {
	String[] toret = new String[data.length];
	try {
	    for (int i = 0; i < data.length; i++) {
		WordInfo wi = wordById.get(data[i]);
		if (wi == null) {
		    closeDB();
		    System.err.println("Error:  Cannot find WordInfo for word id: " + data[i]);
		    System.exit(-1);
		}	    
		toret[i] = wi.word.intern();
	    }	
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error adding converting phrase int[] to String[]: " + dbe.toString());
	    System.exit(-1);
	}
	return toret;
    }

    private Integer[] phraseStrToInt(String[] data) {
	Integer[] toret = new Integer[data.length];
	try {
	    for (int i = 0; i < data.length; i++) {
		WordInfo wi = wordByStr.get(data[i]);
		if (wi == null) {
		    wi = new WordInfo(data[i]);
		    if (this.readonly) {
			wi.id = 0;
		    } else {
			wordById.put(wi);		    
		    }
		}
		toret[i] = wi.id;
	    }	
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error adding converting phrase String[] to Integer[]: " + dbe.toString());
	    System.exit(-1);
	}
	return toret;
    }

    private PhraseEntry convertPhraseEntry(PhraseNumEntry pe) {
	return new PhraseEntry(phraseIntToStr(pe.getRefIds()),
			       phraseIntToStr(pe.getHypIds()),
			       pe.getProb(),
			       pe.getSource());
    }
    private PhraseNumEntry convertPhraseEntry(PhraseEntry pe) {
	return new PhraseNumEntry(phraseStrToInt(pe.getRefWds()),
				  phraseStrToInt(pe.getHypWds()),
				  pe.getProb(),
				  pe.getSource());
    }

    private String PhraseNumEntryToString(PhraseNumEntry pe) {
	return "" + pe.getProb() + " <p>" + sjoin(phraseIntToStr(pe.getRefIds())) + 
	    "</p> <p>" + sjoin(phraseIntToStr(pe.getHypIds())) + "</p>";
    }

    public void dump_pt_file(PrintStream out) {
	openDB();	
	try {
	    EntityCursor<PhraseNumEntry> pi_cursor = 
		phraseById.entities();
	    for (PhraseNumEntry pe : pi_cursor)
		out.println(PhraseNumEntryToString(pe));
	    pi_cursor.close();
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error reading from phrase database (" + base + "): " + dbe.toString());
	    System.exit(-1);
	}
	closeDB();
    }
    
    public void add(String[] refwds, String[] hypwds, double pr, int srcid) {
	add(phraseStrToInt(refwds),phraseStrToInt(hypwds),pr,srcid);
    }
    
    private void add(Integer[] refwds, Integer[] hypwds, double pr, int srcid) {
	if ((refwds == null) || (hypwds == null)) return;
	PhraseNumEntry pe = new PhraseNumEntry(refwds, hypwds,pr,srcid);	
	try {
	    phraseById.put(pe);
	    WordInfo wi = wordById.get(refwds[0]);
	    if (wi == null) {
		closeDB();
		System.err.println("Error.  Cannot find WordInfo for ref id: " + refwds[0]);
		System.exit(-1);
	    } else if (wi.maxlen < refwds.length) {
		wi.maxlen = refwds.length;
		wordById.put(wi);	
	    }    
	} catch(DatabaseException dbe) {
	    closeDB();
	    System.err.println("Error adding phrase: " + pe);
	    System.exit(-1);
	}
    }

    public void closeDB() {
        if (store != null) {
            try {
                store.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing store: " +
				   dbe.toString());
		System.exit(-1);
            }
        }
	
        if (myEnv != null) {
            try {
                myEnv.close();
            } catch(DatabaseException dbe) {
                System.err.println("Error closing MyDbEnv: " +
				   dbe.toString());
		System.exit(-1);
            }
        }
    }

    public void openDB() {
	openDB(true);
    }

    public void openDBwrite() {
	openDB(false);
    }

    private void openDB(boolean readOnly) {
	try {
	    // Open the environment. Create it if it does not already exist.
	    EnvironmentConfig envConfig = new EnvironmentConfig();
	    StoreConfig storeConfig = new StoreConfig();
	    envConfig.setReadOnly(readOnly);
	    storeConfig.setReadOnly(readOnly);

	    envConfig.setAllowCreate(!readOnly);
	    storeConfig.setAllowCreate(!readOnly);
	    

	    myEnv = new Environment(new File(this.base), envConfig);
	    store = new EntityStore(myEnv, "EntityStore", storeConfig);

	    // Primary key for PhraseNumEntry classes
	    phraseById = store.getPrimaryIndex(Integer.class, 
					       PhraseNumEntry.class);

	    phraseByRef = store.getSecondaryIndex(phraseById,
						  String.class,
						  "refwds");
	    
	    wordById = store.getPrimaryIndex(Integer.class, 
					     WordInfo.class);
	    wordByStr = store.getSecondaryIndex(wordById, 
						String.class, 
						"word");
	} catch (DatabaseException dbe) {
	    System.err.println("Error opening db env (base="+
			       this.base + "): " + dbe.toString());
	    System.exit(-1);
	}
	this.readonly = readOnly;
    }
    
    // PhraseNumEntry Accessors
    PrimaryIndex<Integer,PhraseNumEntry> phraseById;
    SecondaryIndex<String,Integer,PhraseNumEntry> phraseByRef;    

    // WordInfo Accessors
    PrimaryIndex<Integer,WordInfo> wordById;    
    SecondaryIndex<String,Integer,WordInfo> wordByStr;    
}

