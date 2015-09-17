package com.bbn.mt.terp;

/*
Copyright 2006 by BBN Technologies and University of Maryland (UMD)

BBN and UMD grant a nonexclusive, source code, royalty-free right to
use this Software known as Translation Error Rate COMpute (the
"Software") solely for research purposes. Provided, you must agree
to abide by the license and terms stated herein. Title to the
Software and its documentation and all applicable copyrights, trade
secrets, patents and other intellectual rights in it are and remain
with BBN and UMD and shall not be used, revealed, disclosed in
marketing or advertisement or any other activity not explicitly
permitted in writing.

BBN and UMD make no representation about suitability of this
Software for any purposes.  It is provided "AS IS" without express
or implied warranties including (but not limited to) all implied
warranties of merchantability or fitness for a particular purpose.
In no event shall BBN or UMD be liable for any special, indirect or
consequential damages whatsoever resulting from loss of use, data or
profits, whether in an action of contract, negligence or other
tortuous action, arising out of or in connection with the use or
performance of this Software.

Without limitation of the foregoing, user agrees to commit no act
which, directly or indirectly, would violate any U.S. law,
regulation, or treaty, or any other international treaty or
agreement to which the United States adheres or with which the
United States complies, relating to the export or re-export of any
commodities, software, or technical data.  This Software is licensed
to you on the condition that upon completion you will cease to use
the Software and, on request of BBN and UMD, will destroy copies of
the Software in your possession.                                                

*/

import java.io.*;
import java.util.regex.*;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;
import com.bbn.mt.terp.phrasedb.PhraseDB;

public class TERplus {
    public static void main(String[] args) {
	TERpara.getOpts(args);

	TERplus terp = new TERplus();
	terp.run();
    }

    public TERplus() {
	write_to_disk = true;
    }
    public TERplus(boolean write_to_disk) {
	this.write_to_disk = write_to_disk;
    }
    
    private boolean  write_to_disk = true;
    private TERcost _costfunc = null;
    private TERcalc calc = null;
    private TERinput terinput = null;
    private PhraseDB phrasedb = null;

    // Variables for Timing Information
    private double InitPTElapsedSec = 0.0;
    private double InitElapsedSec = 0.0;
    private double OutputElapsedSec = 0.0;
    private double OverallElapsedSec = 0.0;
    private int numScored = 0;

    public TERcost getLastCostFunc() { return _costfunc; }
    public TERcalc getLastTERpCalc() { return calc; }
    public TERinput getLastTERpInput() { return terinput; }
    public PhraseDB getLastPhraseDB() { return phrasedb; }

    // Rerunning does not create a new calc, costfunc, or input
    public TERoutput rerun() {
	return run(true, true, false);
    }

    // Load everything from scratch
    public TERoutput run() { 
	return run(false, true, true); 
    }

    public void set_weights(double[] wgts) {
	if (_costfunc == null) init();	    
	_costfunc.set_weights(wgts);
    }

    public void init() {
	init(true);
    }
    public void init(boolean load_phrases) {
	run(false, false, load_phrases);
    }

    private TERoutput run(boolean rerun, boolean evaldata, boolean load_phrases) {
	long OverallStartTime = System.nanoTime();
	long InitStartTime = System.nanoTime();

	String ref_fn = TERpara.para().get_string(TERpara.OPTIONS.REF);
	String hyp_fn = TERpara.para().get_string(TERpara.OPTIONS.HYP);
	String reflen_fn = TERpara.para().get_string(TERpara.OPTIONS.REFLEN);
	String out_pfx = TERpara.para().get_string(TERpara.OPTIONS.OUTPFX);
	ArrayList formats = new ArrayList(Arrays.asList(TERpara.para().get_stringlist(TERpara.OPTIONS.FORMATS)));
	boolean caseon = TERpara.para().get_boolean(TERpara.OPTIONS.CASEON);
	WordNet.setWordNetDB(TERpara.para().get_string(TERpara.OPTIONS.WORDNET_DB_DIR));
	NormalizeText.init();
	       
	// 2. init variables
	double TOTAL_EDITS = 0.0;
	double TOTAL_WORDS = 0.0;
	if (! rerun) {
	    terinput = new TERinput(hyp_fn, ref_fn, reflen_fn,
				    TERpara.para().get_boolean(TERpara.OPTIONS.IGNORE_SETID));
	}
	
	// 3. verify input formats
	if(!verifyFormats(terinput.in_ref_format(), 
			  terinput.in_hyp_format(), formats)) System.exit(1);

	// 4. init calculation variables
	TERoutput[] iouts = null;

	if ((Boolean) TERpara.para().get(TERpara.OPTIONS.SHOW_ALL_REFS)) {
	    iouts = new TERoutput[terinput.max_num_refs()];	    
	    String bpfx = "";
	    if (out_pfx.endsWith(".") && (out_pfx.length() == 0) || out_pfx.endsWith("/")) {
		bpfx = "ref";
	    } else {
		bpfx = ".ref";
	    }

	    for (int i = 0; i < iouts.length; i++) {
		String npfx = bpfx + i;
		iouts[i] = new TERoutput(out_pfx, formats, hyp_fn, ref_fn, reflen_fn, caseon, terinput);
		iouts[i].refname = bpfx + i;
	    }
	}
	TERoutput terout = new TERoutput(out_pfx, formats, hyp_fn, ref_fn, reflen_fn, caseon, terinput);

	if (! rerun) {
	    if (load_phrases) {
		System.out.println("Creating Segment Phrase Tables From DB");
		String pt_db = TERpara.para().get_string(TERpara.OPTIONS.PHRASE_DB);
		if ((pt_db != null) && (! pt_db.equals(""))) {
		    this.phrasedb = new PhraseDB(pt_db);
		} else {
		    this.phrasedb = null;
		}
	    }
	    this.calc = init_tercalc(terinput);
	    //	    if (load_phrases) loadPTdb(terinput);
	}

	if (! evaldata) return null;
	
	int curScored = 0;
	int totalToScore = 0;
	for (TERid pid : terinput.getPlainIds()) {
	    for (String sysid : terinput.get_sysids()) {
		for (TERid tid : terinput.getHypIds(sysid, pid)) { 
		    if ((TERpara.para().get_boolean(TERpara.OPTIONS.IGNORE_MISSING_HYP)) && 
			(! terinput.hasHypSeg(tid)))
			continue;
		    totalToScore++;
		}
	    }
	}

	// 5. compute TER
	long InitEndTime = System.nanoTime();
	long InitElapsedTime = InitEndTime - InitStartTime;
	this.InitElapsedSec += (double) InitElapsedTime / 1.0E09;

	for (TERid pid : terinput.getPlainIds()) {
	    // Prep as much as we can for this plain ID (useful if we have multiple systems or nbest lists)
	    List<String[]> refs = terinput.getRefForHypTok(pid);
	    List<String[]> lens = terinput.getLenForHypTok(pid);	    
	    List<String> origrefs = terinput.getRefForHyp(pid);

	    String[][] refs_ar = (String[][]) refs.toArray(new String[0][0]);
	    String[][] lens_ar = null;
	    if (lens != null) {
		lens_ar = (String[][]) lens.toArray(new String[0][0]);
	    }
	    calc.setRefLen(lens_ar);	    
	    boolean b = calc.init(pid);
	    if (b == false) {
		System.err.println("Unable to initialize phrase table to id: " + pid);
		System.exit(-1);
	    }

	    for (String sysid : terinput.get_sysids()) {
		for (TERid tid : terinput.getHypIds(sysid, pid)) { 
		    if ((TERpara.para().get_boolean(TERpara.OPTIONS.IGNORE_MISSING_HYP)) && 
			(! terinput.hasHypSeg(tid))) {
			continue;
		    }
		    
		    List<String[]> hyps = terinput.getAllHypTok(tid);		    
		    List<String> orighyps = terinput.getAllHyp(tid);
		    
		    curScored++;
		    System.out.println("Processing " + tid);		
		    if (TERpara.para().get_boolean(TERpara.OPTIONS.VERBOSE)) {
			PhraseTable pt = calc.getCostFunc().getPhraseTable();
			if (pt != null) {
			    System.out.printf("  PT Depth=%d Nodes=%d Terms=%d Entries=%d\n", 
					      pt.curTreeDepth(), 
					      pt.curTreeNumNodes(),
					      pt.curTreeNumTerms(),
					      pt.curTreeNumEntries());
			    System.out.printf("  Segment %d of %d (%.1f%%)\n",
					      curScored, totalToScore, 
					      (100.0 * curScored / totalToScore));
			}
		    }
		    if (hyps.size() > 1) {
			System.out.println("WARNING: Multiple hypotheses found for system " + sysid + " segment " + tid + ".  Only scoring first segment.");
		    }
		    String[] hyp = hyps.get(0);
		    
		    if ((refs.size() == 0) || 
			((refs.size() == 1) && (refs.get(0).length == 0))) {
			System.out.println("WARNING: Blank or empty reference for segment: " + tid); 
		    }
		    
		    if (hyp.length == 0) {
			System.out.println("WARNING: Blank or empty hypothesis for segment: " + tid); 
		    }
		    
		    long startTime = System.nanoTime();
		    TERalignment[] irefs = null;
		    if ((Boolean) TERpara.para().get(TERpara.OPTIONS.SHOW_ALL_REFS))
			irefs = new TERalignment[refs.size()];		
		    		    
		    TERalignment result = score_all_refs(hyp, refs_ar, calc, irefs, orighyps, origrefs);
		    TOTAL_EDITS += result.numEdits;
		    TOTAL_WORDS += result.numWords;
		    
		    long endTime = System.nanoTime();
		    long elapsedTime = endTime - startTime;
		    double eTsec = elapsedTime / 1.0E09;
		    if (TERpara.para().get_boolean(TERpara.OPTIONS.VERBOSE)) {
			System.out.printf("  Time: %.3f sec.  Score: %.2f (%.2f / %.2f)\n", 
					  eTsec, result.score(), result.numEdits, result.numWords);
		    }
		    numScored++;
		    
		    if ((Boolean) TERpara.para().get(TERpara.OPTIONS.SHOW_ALL_REFS)) {
			for (int i = 0; i < irefs.length; i++) {
			    if (i < iouts.length) {
				iouts[i].add_result(tid, irefs[i]);
			    }
			}
		    }
		    
		    terout.add_result(tid, result);
		}
	    }
	}

	System.out.println("Finished Calculating TERp");

	long OutputStartTime = System.nanoTime();

	if (this.write_to_disk) {
	    if (TERpara.para().get_boolean(TERpara.OPTIONS.VERBOSE)) {
		System.out.println("Writing output to disk.");
	    }
	    terout.output();
	    if ((Boolean) TERpara.para().get(TERpara.OPTIONS.SHOW_ALL_REFS)) {
		for (int i = 0; i < iouts.length; i++) {
		    iouts[i].output();
		}
	    }
	}	
	long OutputEndTime = System.nanoTime();
	long OutputElapsedTime = OutputEndTime - OutputStartTime;
	this.OutputElapsedSec += (double) OutputElapsedTime / 1.0E09;	

	long OverallEndTime = System.nanoTime();
	long OverallElapsedTime = OverallEndTime - OverallStartTime;
	this.OverallElapsedSec += (double) OverallElapsedTime / 1.0E09;
	
	if (TERpara.para().get_boolean(TERpara.OPTIONS.VERBOSE)) {
	    System.out.println(calc.get_info());
	    System.out.println(NormalizeText.get_info());
	    showTime();
	}
	
	System.out.printf("Total TER: %.2f (%.2f / %.2f)\n",  
			  (TOTAL_EDITS / TOTAL_WORDS),
			  TOTAL_EDITS, TOTAL_WORDS);

	return terout;
    }

    public void loadPTdb(PhraseTable pt, TERinput terinput, TERcalc calc) {
	if (this.phrasedb == null) return;
	long InitPTStartTime = System.nanoTime();	
	this.phrasedb.openDB();
	for (TERid pid : terinput.getPlainIds()) {
	    // Prep as much as we can for this plain ID (useful if we have multiple systems or nbest lists)
	    List<String[]> refs = terinput.getRefForHypTok(pid);	    
	    String[][] refs_ar = (String[][]) refs.toArray(new String[0][0]);

	    List<String[]> allTokHyps = terinput.getHypForPlainTok(pid);
	    if (allTokHyps != null) {
		String[][] allTokHyps_ar = (String[][]) allTokHyps.toArray(new String[0][0]);
		pt.add_phrases(refs_ar, allTokHyps_ar, pid);
	    }
	}
	this.phrasedb.closeDB();
	long InitPTEndTime = System.nanoTime();
	long InitPTElapsedTime = InitPTEndTime - InitPTStartTime;
	if (TERpara.para().get_boolean(TERpara.OPTIONS.VERBOSE))
	    System.out.printf("  Time: %.3f sec\n", 
			      (double) InitPTElapsedTime / 1.0E09);
	this.InitPTElapsedSec += (double) InitPTElapsedTime / 1.0E09;
    }

    private TERcost terCostFactory() {
	TERcost costfunc = null;
	if ((TERpara.para().get(TERpara.OPTIONS.WORD_CLASS_FNAME) == null) ||
	    (TERpara.para().get(TERpara.OPTIONS.WORD_CLASS_FNAME).equals(""))) {
	    if ((Boolean) TERpara.para().get(TERpara.OPTIONS.GENERALIZE_NUMBERS)) {
		costfunc = new TERnumcost();
	    } else {
		costfunc = new TERcost();
	    }

	    costfunc._delete_cost = (Double) TERpara.para().get(TERpara.OPTIONS.DELETE_COST);
	    costfunc._insert_cost = (Double) TERpara.para().get(TERpara.OPTIONS.INSERT_COST);
	    costfunc._shift_cost = (Double) TERpara.para().get(TERpara.OPTIONS.SHIFT_COST);
	    costfunc._match_cost = (Double) TERpara.para().get(TERpara.OPTIONS.MATCH_COST);
	    costfunc._stem_cost = (Double) TERpara.para().get(TERpara.OPTIONS.STEM_COST);
	    costfunc._syn_cost = (Double) TERpara.para().get(TERpara.OPTIONS.SYN_COST);
	    costfunc._substitute_cost = (Double) TERpara.para().get(TERpara.OPTIONS.SUBSTITUTE_COST);
	} else {
	    if (TERpara.para().get_boolean(TERpara.OPTIONS.USE_MULTI_WORDCLASS)) {
		costfunc = 
		    new TERMultiWordClassCost((String) TERpara.para().get(TERpara.OPTIONS.WORD_CLASS_FNAME), 
					      (Double) TERpara.para().get(TERpara.OPTIONS.SUBSTITUTE_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.MATCH_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.INSERT_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.DELETE_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.SHIFT_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.STEM_COST),
					      (Double) TERpara.para().get(TERpara.OPTIONS.SYN_COST));
	    } else {
		costfunc = new TERWordClassCost((String) TERpara.para().get(TERpara.OPTIONS.WORD_CLASS_FNAME), 
						(Double) TERpara.para().get(TERpara.OPTIONS.SUBSTITUTE_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.MATCH_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.INSERT_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.DELETE_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.SHIFT_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.STEM_COST),
						(Double) TERpara.para().get(TERpara.OPTIONS.SYN_COST));
	    }
	}
	String weight_file = (String) TERpara.para().get(TERpara.OPTIONS.WEIGHT_FILE);
	if ((weight_file != null) && (! weight_file.equals(""))) {				     
	    costfunc.load_weights(weight_file);
	}
	return costfunc;
    }

    private TERcalc terCalcFactory(TERinput terinput, TERcost costfunc) {
	TERcalc calc = new TERcalc(costfunc);

	boolean use_phrases = (this.phrasedb != null);

	// set options to compute TER
	calc.loadShiftStopWordList((String) TERpara.para().get(TERpara.OPTIONS.SHIFT_STOP_LIST));
	calc.setBeamWidth((Integer) TERpara.para().get(TERpara.OPTIONS.BEAMWIDTH));
	calc.setShiftDist((Integer) TERpara.para().get(TERpara.OPTIONS.SHIFTDIST));		
	calc.setShiftCon((String) TERpara.para().get(TERpara.OPTIONS.SHIFT_CONSTRAINT));
	calc.setUseStemming((Boolean) TERpara.para().get(TERpara.OPTIONS.USE_PORTER));
	calc.setUseSynonyms((Boolean) TERpara.para().get(TERpara.OPTIONS.USE_WORDNET));
	calc.setUsePhrases(use_phrases);
       
	// Load Phrase Table
	PhraseTable phrases = null;
	if (use_phrases) {
	    phrases = new PhraseTable(this.phrasedb);
	    phrases.set_sum_dup_phrases(TERpara.para().get_boolean(TERpara.OPTIONS.SUM_DUP_PHRASES));
	    phrases.set_adjust_func(TERpara.para().get_string(TERpara.OPTIONS.ADJUST_PHRASETABLE_FUNC),
				    TERpara.para().get_doublelist(TERpara.OPTIONS.ADJUST_PHRASETABLE_PARAMS),
				    TERpara.para().get_double(TERpara.OPTIONS.ADJUST_PHRASETABLE_MIN),
				    TERpara.para().get_double(TERpara.OPTIONS.ADJUST_PHRASETABLE_MAX),
				    calc);
	    loadPTdb(phrases, terinput, calc);
	}
	costfunc.setPhraseTable(phrases);

	String weight_file = TERpara.para().get_string(TERpara.OPTIONS.WEIGHT_FILE);
	if ((weight_file != null) && (! weight_file.equals(""))) {				     
	    costfunc.load_weights(weight_file);
	}

	return calc;	
    }
    
    private TERcalc init_tercalc(TERinput terinput) {
	TERcost costfunc = terCostFactory();
	TERcalc calc = terCalcFactory(terinput, costfunc);
	_costfunc = costfunc;
	return calc;
    }

    // it will be more flexible to verify the input formats later.
    private static boolean verifyFormats(int in_ref_format,
					 int in_hyp_format,
					 ArrayList out_formats)
    {
	if(in_ref_format != in_hyp_format) {
	    System.out.println("** Error: Both hypothesis and reference have to be in the SAME format");
	    return false;
	} else if (in_ref_format == 1 && out_formats.indexOf("xml") > -1) {
	    System.out.println("** Warning: XML ouput may not have correct doc id for Trans format inputs");
	    return true;
	} else
	    return true;	
    }

    private TERalignment score_all_refs(String[] hyp, 
					String[][] refs,
					TERcalc calc,
					TERalignment[] irefs,
					List<String> orighyps,
					List<String> origrefs) {	
	TERalignment[] results = calc.TERall(hyp, refs);
       	
	if ((results == null) || (results.length == 0)) {
	    System.err.println("Internal error in scoring in TERplus.  Aborting!");
	    System.exit(-1);
	}

	int bestref = 0;
	double tot_words = 0.0;
	for (int i = 0; i < results.length; i++) {
	    results[i].orig_hyp = orighyps.get(0);
	    results[i].orig_ref = origrefs.get(i);
	    tot_words += results[i].numWords;
	    if (results[i].numEdits < results[bestref].numEdits)
		bestref = i;
	    if (irefs != null) irefs[i] = results[i];	       
	}

	TERalignment bestresult = new TERalignment(results[bestref]);
	if (TERpara.para().get_boolean(TERpara.OPTIONS.USE_AVE_LEN)) 
	    bestresult.numWords = tot_words / ((double) results.length);

	return bestresult;	
    }

    
    public void showTime() {
	int maxlen = 1;
	int l = String.format("%d", numScored).length();
	if (l > maxlen) maxlen = l;
	l = String.format("%.3f", this.OverallElapsedSec).length();
	if (l > maxlen) maxlen = l;
	double workTime = this.OverallElapsedSec - 
	    (this.InitElapsedSec+this.OutputElapsedSec);
	String secstr = "%"+maxlen+".3f sec\n";
	System.out.println("Timing Information (averages are per hypothesis segment scored)");
	System.out.printf("  Number of Segments:   %"+maxlen+"d\n", numScored);
	System.out.printf("  Total Elapsed Time:   "+ secstr, OverallElapsedSec);
	System.out.printf("  Initialization Time:  "+secstr, InitElapsedSec);
	System.out.printf("  PT Init Time:         "+secstr, InitPTElapsedSec);
	    
	System.out.printf("  Output Time:          "+secstr, OutputElapsedSec);
	System.out.printf("  Total Work Time:      "+secstr, workTime);
	if (this.numScored > 0) {
	    System.out.printf("  Avg Elapsed Time:     "+secstr,
			      OverallElapsedSec / (double) numScored);
	    System.out.printf("  Avg Work Time:        "+secstr,
			      workTime / (double) numScored);
	    System.out.printf("  Avg PT Init Time:     "+secstr,
			      InitPTElapsedSec / (double) numScored);
	    PhraseTable pt = calc.getCostFunc().getPhraseTable();
	    System.out.printf("  Avg PT Insert Time:   "+secstr,
			      (pt == null ? 0.0 : 
			       (pt.getInsertTime() / (double) numScored)));
	    System.out.printf("  Avg PT DB Fetch Time: "+secstr,
			      (pt == null ? 0.0 : 
			       (pt.getDbFetchTime() / (double) numScored)));
	    System.out.printf("  Avg PT Search Time:   "+secstr,
			      (pt == null ? 0.0 : 
			       (pt.getSearchTime() / (double) numScored)));
	    System.out.printf("  Avg TER Calc Time:    "+secstr,
			      calc.getCalcTime() / (double) numScored);
	}
    }

    public static final String license = 
	("\n" + 
	 "Copyright 2006-2008 by BBN Technologies and University of Maryland (UMD)\n\n" +

	 "BBN and UMD grant a nonexclusive, source code, royalty-free right to\n" + 
	 "use this Software known as Translation Error Rate COMpute (the\n" +
	 "\"Software\") solely for research purposes. Provided, you must agree\n" + 
	 "to abide by the license and terms stated herein. Title to the\n" + 
	 "Software and its documentation and all applicable copyrights, trade\n" + 
	 "secrets, patents and other intellectual rights in it are and remain\n" + 
	 "with BBN and UMD and shall not be used, revealed, disclosed in\n" + 
	 "marketing or advertisement or any other activity not explicitly\n" + 
	 "permitted in writing.\n\n" + 
	 
	 "BBN and UMD make no representation about suitability of this\n" + 
	 "Software for any purposes.  It is provided \"AS IS\" without express\n" + 
	 "or implied warranties including (but not limited to) all implied\n" + 
	 "warranties of merchantability or fitness for a particular purpose.\n" + 
	 "In no event shall BBN or UMD be liable for any special, indirect or\n" + 
	 "consequential damages whatsoever resulting from loss of use, data or\n" + 
	 "profits, whether in an action of contract, negligence or other\n" + 
	 "tortuous action, arising out of or in connection with the use or\n" + 
	 "performance of this Software.\n\n" + 
	 
	 "Without limitation of the foregoing, user agrees to commit no act\n" + 
	 "which, directly or indirectly, would violate any U.S. law,\n" + 
	 "regulation, or treaty, or any other international treaty or\n" + 
	 "agreement to which the United States adheres or with which the\n" + 
	 "United States complies, relating to the export or re-export of any\n" + 
	 "commodities, software, or technical data.  This Software is licensed\n" + 
	 "to you on the condition that upon completion you will cease to use\n" + 
	 "the Software and, on request of BBN and UMD, will destroy copies of\n" + 
	 "the Software in your possession.\n\n");
    
    public static void PrintLicense() {
	System.out.println(license);
    }
}
