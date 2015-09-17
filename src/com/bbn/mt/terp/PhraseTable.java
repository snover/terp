package com.bbn.mt.terp;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.Arrays;
import java.util.HashSet;

import com.bbn.mt.terp.phrasedb.PhraseEntry;
import com.bbn.mt.terp.phrasedb.PhraseDB;

public class PhraseTable {    
    public PhraseTable() {}
    private PhraseDB phrasedb = null;

    public PhraseTable(PhraseDB db) {
	this.phrasedb = db;
    }

    public Comparable getCurKey() { return this.curKey; }

    public boolean setKey(Comparable key) {
	if (key == null) {
	    this.curKey = null;
	    this.curTree = null;
	    return false;
	}
	this.curKey = key;
	this.curTree = rootmap.get(key);
	return (this.curTree != null);
    }

    public int add_phrases(String[][] ref, String[][] hyp, Comparable key) {
	if (key == null) return 0;

	this.curKey = key;
	if (! rootmap.containsKey(key)) {
	    PhraseTree pt = new PhraseTree();
	    rootmap.put(key, pt);
	}
	this.curTree = rootmap.get(key);

	if (this.curTree == null) {
	    System.out.println("ERROR.  VOID curtree (key is " + key + ")");
	    System.exit(-1);
	}
	
	long startTime = System.nanoTime();
	Set<PhraseEntry> fullset = phrasedb.get_phrases(ref, hyp);
	long endTime = System.nanoTime();
	this.dbFetchTime += (endTime - startTime);

	startTime = System.nanoTime();
	int num = insert(fullset);
	endTime = System.nanoTime();
	this.insertTime += (endTime - startTime);

	return num;
    }


    /**
       Returns an ArrayList that contains ArrayLists of valid phrases.
       Each of those ArrayLists is 3 elements long
       0: int - Length of first match
       1: int - Length of second match
       2: Double - Cost
    **/
    public ArrayList retrieve_all(Comparable[] seq_a, int ind_a,
				  Comparable[] seq_b, int ind_b) {
	if (curTree == null) {
	    System.err.println("ERROR. Current tree not set.");
	    System.exit(-1);
	}
	long startTime = System.nanoTime();
	ArrayList toret = curTree.retrieve_all(seq_a, ind_a, seq_b, ind_b);
	long endTime = System.nanoTime();
	this.searchTime += (endTime - startTime);
	return toret;
    } 

    public PhraseTree.phrase_cost retrieve_exact(Comparable[] seq_a, int start_a, int len_a,
						 Comparable[] seq_b, int start_b, int len_b) {
	long startTime = System.nanoTime();
	PhraseTree.phrase_cost pc = fullTree.retrieve_exact(seq_a, start_a, len_a,
							    seq_b, start_b, len_b);
	long endTime = System.nanoTime();
	this.searchTime += (endTime - startTime);
	return pc;
    }

	    
    public void set_adjust_func(String func, double[] params) { 
	set_adjust_func(func, params, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    public void set_adjust_func(String func, double[] params, double min, double max) { 
	set_adjust_func(func, params, min, max, null);
    }

    public void set_adjust_func(String func, double[] params, double min, double max, TERcalc tc) { 
	this.adjust_params = params;
	this.adjust_min = min;
	this.adjust_max = max;
	this.adjust_tercalc = tc;

	if ((func == null) || func.equals(""))
	    func = "NONE";
	
	if (adjust_func_names.containsKey(func)) {
	    adjust_func_spec afs = (adjust_func_spec) adjust_func_names.get(func);
	    if ((afs.num_params > 0) && ((adjust_params == null) || (adjust_params.length != afs.num_params))) {
		System.err.println(afs);
		System.err.println("Aborting.");
		System.exit(-1);		
	    } else {
		this.adjust_func = afs.func;
		this.adjust_need_nedits = afs.need_nedits;		
	    }
	} else {
	    System.err.println("Error!  Invalid adjustment function: " + func);
	    System.err.println(valid_adjust_funcs());
	    System.exit(-1);
	}
    }

    public static String valid_adjust_funcs () {
	String tr = "Valid phrasetable adjustment functions are:\n";
	Iterator it = adjust_func_names.keySet().iterator();
	while (it.hasNext()) {
	    tr += "  " + adjust_func_names.get(it.next()) + "\n";
	}
	tr += "\nWithin phrasetable adjustment functions:\n" +
	    "  %COST% is the original cost\n" +
	    "  %NED% is the TER cost between the two phrases\n" +
	    "  By default log refers to the natural log (base e).\n" +
	    "    log_10 refers to log base 10 in these equations.";   
	return tr;
    }
    
    private static final double _log10 = Math.log(10);
    private static double log10 (double d) {
	return Math.log(d) / _log10;
    }
    private static double ln (double d) {
	return Math.log(d);
    }

    private PhraseTree.phrase_cost adjust_cost(PhraseTree.phrase_cost phcost, 
					       Comparable[] seq_a, Comparable[] seq_b) {
	double numedit = 0;
	int maxlen = ((seq_a.length > seq_b.length) ? seq_a.length : seq_b.length);
	int i = 0;
	if ((adjust_need_nedits) && (adjust_tercalc != null)) {
	    boolean used_phrases = adjust_tercalc.getUsePhrases();
	    adjust_tercalc.setUsePhrases(false);
	    TERalignment curalign = adjust_tercalc.TERpp(seq_b, seq_a);
	    numedit = curalign.numEdits;	
	    adjust_tercalc.setUsePhrases(used_phrases);    
	}
	double cost = phcost.orig_cost;
	double newcost = phcost.orig_cost;
	switch (adjust_func) {
	case NONE: break;
	    /*
	case LIN: newcost = adjust_params[0] + (adjust_params[1] * (adjust_params[2] + cost));
	    break;
	case LIN_NED: newcost = adjust_params[0] + (adjust_params[1] * (adjust_params[2] + cost) * 
						    (adjust_params[3] + numedit));
	    break;
	case LOG: newcost = adjust_params[0] + (adjust_params[1] * ln(adjust_params[2] * 
								      (adjust_params[3] + cost)));
	    break;
	case LOG10: newcost = adjust_params[0] + (adjust_params[1] * log10(adjust_params[2] * 
									 (adjust_params[3] + cost)));
	    break;
	case LOG_NED: newcost = adjust_params[0] + (adjust_params[1] * ln(adjust_params[2] * 
									  (adjust_params[3] + cost)) * 
						    (adjust_params[4] + numedit));
	    break;
	case LOG10_NED: newcost = adjust_params[0] + (adjust_params[1] * log10(adjust_params[2] * 
										(adjust_params[3] + cost)) * 
						      (adjust_params[4] + numedit));
	    break;
	case STDLOG: newcost = adjust_params[0] +
			 (((adjust_params[1] * log10(1.0 - cost)) + adjust_params[2]) *
			  (adjust_params[3] + numedit));
	    break;
	case STDLOG2: newcost = adjust_params[0] +
			  (((adjust_params[1] * log10(cost)) + adjust_params[2]) *
			   (adjust_params[3] + numedit));
	    break;
	    */
	case STD: newcost = adjust_params[0] + 
		      (adjust_params[1] * log10(cost) * numedit) +
		      (adjust_params[2] * cost * numedit) +
		      (adjust_params[3] * numedit);
	    break;
	case STDINV: newcost = adjust_params[0] + 
		      (adjust_params[1] * log10(1.0-cost) * numedit) +
		      (adjust_params[2] * (1.0-cost) * numedit) +
		      (adjust_params[3] * numedit);
	    break;
	case STDNONED: newcost = adjust_params[0] + 
			   (adjust_params[1] * log10(cost)) +
			   (adjust_params[2] * cost);
	    break;
	case STDINVNONED: newcost = adjust_params[0] + 
			      (adjust_params[1] * log10(1.0-cost)) +
			      (adjust_params[2] * (1.0 - cost));
	    break;
	    /*
	case NED: newcost =  adjust_params[0] + (adjust_params[1] * (adjust_params[2] + numedit));
	    break;
	case NED_BINS:
	    if (adjust_params == null) break;
	    for (i = 0; (i+1) < adjust_params.length; i+=2) {
		if (numedit < adjust_params[i]) {
		    newcost = adjust_params[i+1];
		    i = adjust_params.length;
		}
	    } 
	    if (i < adjust_params.length) newcost = adjust_params[i];
	    break;
	case LENGTH_BINS: 
	    if (adjust_params == null) break;
	    for (i = 0; (i+1) < adjust_params.length; i+=2) {
		if (maxlen < adjust_params[i]) {
		    newcost = adjust_params[i+1]; 
		    i = adjust_params.length;
		}
	    } 
	    if (i < adjust_params.length) newcost = adjust_params[i];
	    break;
	    */
	}

	phcost.cost = newcost;
	phcost.numedits = numedit;
	if (phcost.cost > adjust_max) phcost.cost = adjust_max;
	if (phcost.cost < adjust_min) phcost.cost = adjust_min;
	return phcost;

    }

    // Runs the current adjust function over the entire phrase table
    public void adjust_all() {
	for (Comparable key : rootmap.keySet()) {
	    setKey(key);
	    PhraseTree.Iter it = this.getIter();
	    while (it.hasNext()) {
		it.next();
		adjust_cost(it.phcost(), it.phrase_a(), it.phrase_b());
	    }	
	}
	PhraseTree.Iter it = this.getFullIter();
	while (it.hasNext()) {
	    it.next();
	    adjust_cost(it.phcost(), it.phrase_a(), it.phrase_b());
	}	
    }

    private int insert(Set<PhraseEntry> fullset) {
	int num = 0;
	for (PhraseEntry pe : fullset) {
	    boolean added = insert(pe.getRefWds(), 
				   pe.getHypWds(), 
				   pe.getProb());
	    if (added) num++;
	}
	return num;
    }

    private boolean insert(Comparable[] seq_a, Comparable[] seq_b, 
			   double cost) {
	return insert(seq_a, seq_b, new PhraseTree.phrase_cost(cost));
    }

    private boolean insert(Comparable[] seq_a, Comparable[] seq_b, 
			   PhraseTree.phrase_cost cost) {
	if ((seq_a == null) || (seq_b == null)) return false;

	if (adjust_tercalc != null) {
	    seq_a = adjust_tercalc.getCostFunc().process_input_ref((String[]) seq_a);
	    seq_b = adjust_tercalc.getCostFunc().process_input_hyp((String[]) seq_b);
	}
	
	if (adjust_func != ADJUST_FUNC.NONE) {
	    cost = adjust_cost(cost, seq_a, seq_b);
	}

	if (curTree == null) {
	    System.err.println("ERROR. Current tree not set.");
	    System.exit(-1);
	}
	boolean rootbol = curTree.insert(seq_a, seq_b, cost);
	boolean fullbol = fullTree.insert(seq_a, seq_b, cost);
        return (rootbol || fullbol);
    }

    public void output_phrase_table(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    output_phrase_table(out);
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("Output Phrase Table to " + fname + ": " + ioe);
	    System.exit(-1);
	    return;
	}
    }

    public void output_phrase_table(BufferedWriter out) {
	try {
	    PhraseTree.Iter it = this.getIter();
	    while (it.hasNext()) {
		String s = it.next();
		out.write(s + "\n");
	    }
	} catch (IOException ioe) {
	    System.err.println("Output Phrase Table: " + ioe);
	    System.exit(-1);
	    return;
	}
    }

    public PhraseTree.Iter getFullIter() {
	return fullTree.getIter();
    }

    public PhraseTree.Iter getIter() {
	if (curTree == null) {
	    System.err.println("Current tree not set.");
	    System.exit(-1);
	}	
	return curTree.getIter();
    }
    // keys are ArrayList[Comparable word_a, Comparable word_b]
    // word_a and word_b can also be null.  They can't both be null    

    // vals are ArrayList[Double cost, HashMap next]	
    // cost is null if this is not a termination point
    // next is null if this is a leaf
    // they can't both be null

    private PhraseTree fullTree = new PhraseTree();
    private PhraseTree curTree = null;
    private Comparable curKey = null;
    private HashMap<Comparable, PhraseTree> rootmap = new HashMap();
    private long searchTime = 0;
    private long dbFetchTime = 0;
    private long insertTime = 0;

    private static class adjust_func_spec {
	public adjust_func_spec(ADJUST_FUNC func, String name, boolean need_nedits,
				int num_params, String formula) {
	    this.func = func;
	    this.name = name;
	    this.need_nedits = need_nedits;
	    this.num_params = num_params;
	    this.formula = formula;	    
	}

	public String toString() {
	    String tr = null;
	    if (num_params >= 0)
		tr = name + " adjust function needs " + num_params + " parameters.\n";
	    else 
		tr = name + " adjust function can take any number of parameters.\n";
	    tr += "    " + name + " function is: " + formula;
	    return tr;
	}
	public ADJUST_FUNC func;
	public String name;
	public boolean need_nedits;
	public String formula;
	public int num_params; // negative will mean any number is allowed
    }

    private static TreeMap adjust_func_names = new TreeMap();

    static {	
	adjust_func_names.put("NONE", 
			      new adjust_func_spec(ADJUST_FUNC.NONE, "NONE", false, 0, 
						   "NEWCOST = %COST%"));
	/*
	adjust_func_names.put("LIN", 
			      new adjust_func_spec(ADJUST_FUNC.LIN, "LIN", false, 3, 
						   "NEWCOST = a + (b * (c + %COST%))"));
	adjust_func_names.put("LOG", 
			      new adjust_func_spec(ADJUST_FUNC.LOG, "LOG", false, 4, 
						   "NEWCOST = a + (b * log(c * (d + %COST%)))"));
	adjust_func_names.put("LOG10", 
			      new adjust_func_spec(ADJUST_FUNC.LOG10, "LOG10", false, 4, 
						   "NEWCOST = a + (b * log_10(c * (d + %COST%)))"));
	adjust_func_names.put("LIN_NED", 
			      new adjust_func_spec(ADJUST_FUNC.LIN_NED, "LIN_NED", true, 4, 
						   "NEWCOST = NEWCOST = a + (b * (c + %COST%) * (e + %NED%))"));
	adjust_func_names.put("LOG_NED", 
			      new adjust_func_spec(ADJUST_FUNC.LOG_NED, "LOG_NED", true, 5, 
						   "NEWCOST = a + (b * log(c * (d + %COST%)) * (e + %NED%))"));
	adjust_func_names.put("LOG10_NED", 
			      new adjust_func_spec(ADJUST_FUNC.LOG10_NED, "LOG10_NED", true, 5, 
						   "NEWCOST = a + (b * log_10(c * (d + %COST%)) * (e + %NED%))"));
	adjust_func_names.put("NED", 
			      new adjust_func_spec(ADJUST_FUNC.NED, "NED", true, 3, 
						   "NEWCOST = a + (b * (c + %NED%))"));
	adjust_func_names.put("LENGTH_BINS", 
			      new adjust_func_spec(ADJUST_FUNC.LENGTH_BINS, "LENGTH_BINS", false, -1, 
						   " [a1, a2, b1, b2, ... n1, n2, o]\n" +
						   "   where NEWCOST is a2 if the max length is < a1.\n" +
						   "   otherwise NEWCOST is b2 if the max length < b1, etc.\n" +
						   "   and if no case matches NEWCOST is o.\n" +
						   "   if an even number of params is used o will default\n" + 
						   "   to the original cost"));
	adjust_func_names.put("NED_BINS", 
			      new adjust_func_spec(ADJUST_FUNC.NED_BINS, "NED_BINS", true, -1, 
						   " [a1, a2, b1, b2, ... n1, n2, o]\n" +
						   "   where the NEWCOST is a2 if %NED% is < a1.\n" +
 						   "   otherwise NEWCOST is b2 if %NED% < b1, etc.\n" +
						   "   and if no case matches, the NEWCOST is o.\n" +
						   "   if an even number of params is used o will default\n" + 
						   "   to the original cost"));

	adjust_func_names.put("STDLOG", 
			      new adjust_func_spec(ADJUST_FUNC.STDLOG, "STDLOG", true, 4, 
						   "NEWCOST = a + (((b * log_10(1.0 -%COST%)) + c) * (d + %NED%))"));
	adjust_func_names.put("STDLOG2", 
			      new adjust_func_spec(ADJUST_FUNC.STDLOG2, "STDLOG2", true, 4, 
						   "NEWCOST = a + (((b * log_10(%COST%)) + c) * (d + %NED%))"));
*/
	adjust_func_names.put("STD", 
			      new adjust_func_spec(ADJUST_FUNC.STD, "STD", true, 4, 
						   "NEWCOST = a + (b * %NED% * log_10(%COST%))" + 
						   " + (c * %NED% * %COST%) + (d * %NED%)"));
	adjust_func_names.put("STDINV", 
			      new adjust_func_spec(ADJUST_FUNC.STDINV, "STDINV", true, 4, 
						   "NEWCOST = a + (b * %NED% * log_10(1.0 - %COST%))" + 
						   " + (c * %NED% * (1.0 - %COST%)) + (d * %NED%)"));
	adjust_func_names.put("STDNONED", 
			      new adjust_func_spec(ADJUST_FUNC.STDNONED, "STDNONED", false, 3, 
						   "NEWCOST = a + (b * log_10(%COST%))" + 
						   " + (c * %COST%)"));
	adjust_func_names.put("STDINVNONED", 
			      new adjust_func_spec(ADJUST_FUNC.STDINVNONED, "STDINVNONED", false, 3, 
						   "NEWCOST = a + (b * log_10(1.0-%COST%))" + 
						   " + (c * (1.0 - %COST%))"));
    }

    private static enum ADJUST_FUNC {
	NONE, 
	/* LENGTH_BINS, NED_BINS, LIN, LIN_NED, LOG, LOG10, 
	   LOG_NED, LOG10_NED, STDLOG, STDLOG2, NED,  */
	STD, STDINV, STDNONED, STDINVNONED
    }    

    private ADJUST_FUNC adjust_func = ADJUST_FUNC.NONE;
    private TERcalc adjust_tercalc = null;
    private double adjust_min = Double.NEGATIVE_INFINITY;
    private double adjust_max = Double.POSITIVE_INFINITY;
    private double[] adjust_params = null;
    private boolean adjust_need_nedits = false;
    private boolean sum_dup_phrases = false;

    public void set_sum_dup_phrases(boolean b) { sum_dup_phrases = b; }

    public double getInsertTime() {
	double ETsec = this.insertTime / 1.0E09;
	return ETsec;
    }

    public double getSearchTime() {
	double ETsec = this.searchTime / 1.0E09;
	return ETsec;
    }

    public double getDbFetchTime() {
	double ETsec = this.dbFetchTime / 1.0E09;
	return ETsec;
    }

    public int fullTreeNumNodes() { return fullTree.num_nodes(); }
    public int curTreeNumNodes() { return curTree.num_nodes(); }

    public int fullTreeNumTerms() { return fullTree.num_terms(); }
    public int curTreeNumTerms() { return curTree.num_terms(); }

    public int fullTreeNumEntries() { return fullTree.num_entries(); }
    public int curTreeNumEntries() { return curTree.num_entries(); }

    public int fullTreeDepth() { return fullTree.depth(); }
    public int curTreeDepth() { return curTree.depth(); }

    public String get_info() {
	int full_num_nodes = fullTree.num_nodes();
	int full_num_terms = fullTree.num_terms();
	int full_num_entries = fullTree.num_entries();
	int full_depth = fullTree.depth();	

	String s = ("PhraseTable\n" + 
		    "  Full Number Nodes:   " + full_num_nodes + "\n" +
		    "  Full Number Terms:   " + full_num_terms + "\n" +
		    "  Full Number Entries: " + full_num_entries + "\n" +
		    "  Full Tree Depth:     " + full_depth + "\n" +
		    "  Total DB Fetch Time: " + getDbFetchTime() + " sec.\n" +
		    "  Total Insert Time:   " + getInsertTime() + " sec.\n" +
		    "  Total Search Time:   " + getSearchTime() + " sec.\n" +
		    "  Sum Duplicate Phrases: " + sum_dup_phrases + "\n" + 
		    "  Adjust Func:     " + adjust_func + "\n" +
		    "  Adjust Func Min: " + adjust_min + "\n" +
		    "  Adjust Func Max: " + adjust_max + "\n" +
		    "  Adjust Func Calcs NED: " + adjust_need_nedits + "\n" +
		    "  Adjust Func Params: " + TERalignment.join(" ", adjust_params) + "\n");
	return s;
    }


    public int num_params() { 
	if (adjust_params == null) { return 0; }
	return adjust_params.length; 
    }
    public void get_wgt_phrase(int startind, double[] wgt, 
			       Comparable[] hyp, int hstart, int hlen,
			       Comparable[] ref, int rstart, int rlen) {
	// Lookup phrase...
	PhraseTree.phrase_cost pc = fullTree.retrieve_exact(ref, rstart, rlen, hyp, hstart, hlen);
	if (pc == null) return;
	
	switch (adjust_func) {
	case NONE:
	    break;
	case STDNONED: 
	    wgt[startind+0] += 1;
	    wgt[startind+1] += log10(pc.orig_cost);
	    wgt[startind+2] += pc.orig_cost;
	    break;
	case STDINVNONED: 
	    wgt[startind+0] += 1;
	    wgt[startind+1] += log10(1.0 - pc.orig_cost);
	    wgt[startind+2] += (1.0 - pc.orig_cost);
	    break;
	case STDINV:
	    wgt[startind+0] += 1;
	    wgt[startind+1] += log10(1.0 - pc.orig_cost) * pc.numedits;
	    wgt[startind+2] += (1.0 - pc.orig_cost) * pc.numedits;
	    wgt[startind+3] += pc.numedits;
	    break;
	case STD:
	    wgt[startind+0] += 1;
	    wgt[startind+1] += log10(pc.orig_cost) * pc.numedits;
	    wgt[startind+2] += pc.orig_cost * pc.numedits;
	    wgt[startind+3] += pc.numedits;
	    break;
	default:
	    System.out.println("Warning.  Current adjustment function cannot be properly counted.");
	    break;
	}
	return;
    }

    public void set_weights(int startind, double[] wgts) {
	for (int i = 0; i < adjust_params.length; i++) {
	    adjust_params[i] = wgts[i+startind];
	}
	adjust_all();
	return;
    }
    public void current_weights(int startind, double[] wgts) {
	for (int i = 0; i < adjust_params.length; i++) {
	    wgts[i+startind] = adjust_params[i];
	}
	return;
    }    
}
