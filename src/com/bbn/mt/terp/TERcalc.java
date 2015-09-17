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

TERcalc.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.*;
import java.io.*;

public class TERcalc {
    public TERcalc(TERcost costfunc) {
	this.costfunc = costfunc;
    }
    private TERcost costfunc;

  /* Turn on if you want a lot of debugging info. */
    static final private boolean DEBUG = false;
    public double ref_len = -1.;
    private long calcTime = 0;
  
    public void loadPT(String[] hyps, String[] refs, Comparable key) {
	PhraseTable pt = costfunc.getPhraseTable();
	if (pt == null) return;

	String[][] tokhyp = new String[hyps.length][];
	String[][] tokref = new String[refs.length][];
	for (int i = 0; i < hyps.length; i++) {
	    Comparable[] hyparr = costfunc.process_input_hyp(NormalizeText.process(hyps[i]));
	    tokhyp[i] = new String[hyparr.length];
	    for (int j = 0; j < hyparr.length; j++) {
		tokhyp[i][j] = hyparr[j].toString();
	    }
	}

	for (int i = 0; i < refs.length; i++) {
	    Comparable[] refarr = costfunc.process_input_ref(NormalizeText.process(refs[i]));
	    tokref[i] = new String[refarr.length];
	    for (int j = 0; j < refarr.length; j++) {
		tokref[i][j] = refarr[j].toString();
	    }
	}
	loadPT(tokhyp, tokref, key);
    }
    
    public void loadPT(String[][] tokhyps, String[][] tokrefs, Comparable key) {
	PhraseTable pt = costfunc.getPhraseTable();
	if (pt == null) return;
	pt.add_phrases(tokrefs, tokhyps, key);
    }

    public boolean init(Comparable key) {
	PhraseTable pt = costfunc.getPhraseTable();
	if (pt == null) return true;
	return pt.setKey(key);	
    }

    public TERcost getCostFunc() { return costfunc; }

    public double getCalcTime() {
	double ETsec = this.calcTime / 1.0E09;
	return ETsec;
    }

    public void output_phrasetable(String fname) {
	PhraseTable pt = costfunc.getPhraseTable();
	if (pt == null) return;
	pt.output_phrase_table(fname);
    }

    public void setUseStemming(boolean b) {use_porter = b; }
    public void setUseSynonyms(boolean b) {use_wordnet = b; }
    public void setUsePhrases(boolean b) {use_phrases = b; } 
    public boolean getUsePhrases() { return use_phrases; }

    public void setShiftSize(int i) {
	MAX_SHIFT_SIZE = i;
    }

  public void setBeamWidth(int i) {
	BEAM_WIDTH = i;
  }

  public void setShiftDist(int i) {
      MAX_SHIFT_DIST = i;
  }


  public void setRefLen(String[][] reflens) {
      if ((reflens == null) || (reflens.length == 0)) {
	  setRefLen(-1.0);
      } else {
	  double alen = 0.0;
	  for (String[] ref : reflens) {
	      alen += ref.length;
	  }
	  setRefLen(alen / reflens.length);
      }
  }
    
  public void setRefLen(String[] reflens) {
    if (reflens == null || reflens.length == 0) {
      setRefLen(-1.0);
    } else {	
	String[][] ls = new String[reflens.length][];
	for (int i = 0; i < reflens.length; i++)
	    ls[i] = NormalizeText.process(reflens[i]);
	setRefLen(ls);
    }
  }
    
    public void setRefLen(double d) {
	ref_len = (d >= 0) ? d : -1;
    }

    public void showInternalTable(Comparable[] hyp, Comparable[] ref) {
	System.out.printf("%12s ", "");
	for (int rj = 0; rj <= ref.length; rj++) {
	    if (rj == 0) {
		System.out.printf("%2d/%-9s ", rj, 
				  "");
	    } else {
		int maxindex = ref[rj-1].toString().length();
		if (maxindex > 9) maxindex = 9;
		System.out.printf("%2d/%-9s ", rj, 
				  ref[rj-1].toString().substring(0, maxindex));
	    }
	}
	System.out.printf("\n");
	
	for (int hj = 0; hj <= hyp.length; hj++) {
	    if (hj == 0) {
		System.out.printf("%2d/%-9s", hj, 
				  "");
	    } else {
		int maxindex = hyp[hj-1].toString().length();
		if (maxindex > 9) maxindex = 9;
		System.out.printf("%2d/%-9s", hj, 
				  hyp[hj-1].toString().substring(0, maxindex));
	    }
	    for (int ri = 0; ri <= ref.length; ri++) {
		if (S[ri][hj] >= 0) {
		    System.out.printf(" %1c/%04.1f/%02d/%02d", 
				      P[ri][hj], S[ri][hj],
				      PH[ri][hj], PR[ri][hj]);
		} else {
		    System.out.printf(" %1s %4s %2s %2s", 
				      "", "", "", "");
				    
		}
	    }
	    System.out.printf("\n");
	}
    }
            
    public TERalignment[] TERall(String[] hyp, String[][] refs) {
	// Run TER on hyp and each ref (both should already be tokenized), 
	//  and return an array of TERalignment[]
	TERalignment[] tr = new TERalignment[refs.length];
	Comparable[] pphyp = costfunc.process_input_hyp(hyp);
	for (int i = 0; i < refs.length; i++) {
	    Comparable[] ppref = costfunc.process_input_ref(refs[i]);
	    TERalignment res = null;
	    if ((ppref.length == 0) || (pphyp.length == 0))
		res = TERnullstr(pphyp, ppref);
	    else
		res = TERpp(pphyp, ppref);
	    if (ref_len >= 0) res.numWords = ref_len;
	    tr[i] = res;
	}
	return tr;
    }

    public TERalignment TER(String hyp, String ref) {
	/* Tokenize the strings and pass them off to TER */
	TERalignment to_return;

	Comparable[] hyparr = costfunc.process_input_hyp(NormalizeText.process(hyp));
	Comparable[] refarr = costfunc.process_input_ref(NormalizeText.process(ref));
	
	if(refarr.length == 0 || hyparr.length == 0) {
	    to_return = TERnullstr(hyparr, refarr);
	    if (ref_len >= 0) to_return.numWords = ref_len;
	} else {
	    to_return = TERpp(hyparr, refarr);
	    if(ref_len >= 0) to_return.numWords = ref_len;
	}
	
	to_return.orig_hyp = hyp;
	to_return.orig_ref = ref;
	return to_return;
  }    

  public TERalignment TERnullstr(Comparable[] hyparr, Comparable[] refarr) {
	TERalignment to_return = new TERalignment(costfunc);
	
	if(hyparr.length == 0 && refarr.length == 0) {
	    to_return.alignment = new char[0];
	    to_return.alignment_r = new int[0];
	    to_return.alignment_h = new int[0];
	    to_return.numWords = 0;
	    to_return.numEdits = 0;
	} else if (hyparr.length == 0) {
	    to_return.alignment = new char[refarr.length];
	    to_return.alignment_r = new int[to_return.alignment.length];
	    to_return.alignment_h = new int[to_return.alignment.length];
	    for(int i = 0; i < refarr.length; ++i) {
		to_return.alignment[i] = 'D';
		to_return.alignment_h[i] = 0;
		to_return.alignment_r[i] = 1;
		to_return.numEdits += costfunc.delete_cost(refarr[i]);
	    }
	    to_return.numWords = refarr.length;
	} else {
	    to_return.alignment = new char[hyparr.length];
	    to_return.alignment_r = new int[to_return.alignment.length];
	    to_return.alignment_h = new int[to_return.alignment.length];
	    for(int i = 0; i < hyparr.length; ++i) {
		to_return.alignment[i] = 'I';
		to_return.alignment_h[i] = 1;
		to_return.alignment_r[i] = 0;
		to_return.numEdits += costfunc.insert_cost(hyparr[i]);
	    }
	    to_return.numWords = 0;
	}
	to_return.hyp = hyparr;
	to_return.ref = refarr;
	to_return.aftershift = hyparr;
	
	return to_return;
  }
  
    public TERalignment TER(String[] hyp, String[] ref) {
	return TERpp(costfunc.process_input_hyp(hyp), costfunc.process_input_ref(ref));
    }

    // Run TER on preprocessed segment pair
    public TERalignment TERpp(Comparable[] hyp, Comparable[] ref) {	
	/* Calculates the TER score for the hyp/ref pair */
	long startTime = System.nanoTime();
	TERalignment cur_align = MinEditDist(hyp,ref);
	Comparable[] cur = hyp;

	cur_align.hyp = hyp;
	cur_align.ref = ref;
	cur_align.aftershift = hyp;

	double edits = 0;
	int numshifts = 0;
	ArrayList allshifts = new ArrayList(hyp.length+ref.length);
	
	if (DEBUG)
	    System.out.println("Initial Alignment:\n" + cur_align + "\n");
	
	while (true) {
	    Object[] returns = CalcBestShift(cur, hyp, ref, cur_align);
	    //      Object[] returns = CalcBestShift(cur, hyp, ref, rloc, cur_align);
	    if (returns == null) {
		break;
	    }
	    TERshift bestShift = (TERshift) returns[0];
	    edits += bestShift.cost;
	    cur_align = (TERalignment) returns[1];
	    	    
	    bestShift.alignment = cur_align.alignment;
	    bestShift.aftershift = cur_align.aftershift;
	    	    
	    allshifts.add(bestShift);
	    cur = cur_align.aftershift;  
	}

	TERalignment to_return = cur_align;
	to_return.allshifts = (TERshift[]) allshifts.toArray(new TERshift[0]);
	
	to_return.numEdits += edits;

	NUM_SEGMENTS_SCORED++;
	long endTime = System.nanoTime();
	this.calcTime += endTime - startTime;
	return to_return;
  }
    
    private void _gather_exposs_shifts(Comparable[] hyp, Comparable[] ref,
				       boolean[] herr, boolean[] rerr, 
				       int[] ralign, int[] halign,
				       Set[] allshifts, TreeMap[] paramap,
				       int h_start, int r_start, int h_len, int r_len,
				       int num_mismatch) {
	boolean ok = true;
	if (h_len >= MAX_SHIFT_SIZE) return;

	for (int len = 0; ok && (len < (MAX_SHIFT_SIZE-h_len)); len++) {	    
	    int hind = h_start+h_len+len;
	    int rind = r_start+r_len+len;

	    if (hind >= hyp.length) return;
	    if (rind >= ref.length) return;
	    if ((paramap != null) && (paramap[rind] != null)) {
		ArrayList pal = (ArrayList) paramap[rind].get(hind);
		if (pal != null) {
		    for (int i = 0; i < pal.size(); i++) {
			PhraseTree.OffsetScore sc = 
			    (PhraseTree.OffsetScore) pal.get(i);
			_gather_exposs_shifts(hyp, ref, herr, rerr, ralign, halign,
					      allshifts, paramap, h_start, r_start,
					      h_len+len+sc.hyp_len, r_len+len+sc.ref_len,
					      num_mismatch);
		    }
		}
	    }
	    Comparable hp = hyp[hind];
	    Comparable rp = ref[rind];
	    boolean is_mismatch = true;
	    if ((rp.equals(hp)) ||
		(((shift_con == SHIFT_CON.RELAX) || 
		  (shift_con == SHIFT_CON.RELAX_NP) ||
		  (shift_con == SHIFT_CON.ALLOW_MISMATCH)) &&
		 ((use_porter && Porter.equivStems(hp, rp)) ||
		  (use_wordnet && WordNet.areSynonyms(hp, rp))))) {
		is_mismatch = false;
	    }
	    if ((is_mismatch == false) || 
		((shift_con == SHIFT_CON.ALLOW_MISMATCH) && 
		 ((num_mismatch+1) <= max_mismatch_num) && 
		 ((num_mismatch+1) <= (MAX_SHIFT_SIZE * max_mismatch_percent)))) {
		if (is_mismatch) {
		    num_mismatch++;
		    _gather_exposs_shifts(hyp, ref, herr, rerr, ralign, halign,
					  allshifts, paramap, h_start, r_start,
					  h_len+len+1, r_len+len,
					  num_mismatch);
		    _gather_exposs_shifts(hyp, ref, herr, rerr, ralign, halign,
					  allshifts, paramap, h_start, r_start,
					  h_len+len, r_len+len+1,
					  num_mismatch);		    
		}
		
		// Check number of mismatches (only matters for ALLOW_MISMATCH
		boolean too_many_mismatch = false;
		if ((shift_con != SHIFT_CON.ALLOW_MISMATCH) && (num_mismatch > 0)) too_many_mismatch = true;
		if ((num_mismatch > 0) && ((num_mismatch > max_mismatch_num) || 
					   ((num_mismatch / (1+h_len+len)) > max_mismatch_percent)))
		    too_many_mismatch = true;
		if (too_many_mismatch) continue;
		
		// Check if there is an error anywhere
		boolean no_err = true;
		for (int i = h_start; no_err && (i <= hind); i++) {
		    if (herr[i]) no_err = false;
		}
		if (no_err) continue;
		no_err = true;
		for (int i = r_start; no_err && (i <= rind); i++) {
		    if (rerr[i]) no_err = false;
		}
		if (no_err) continue;

		// Check the stop word list
		boolean all_hyp_stop = true;
		boolean all_ref_stop = true;
		if (shift_stop_words != null) {
		    for (int i = h_start; all_hyp_stop && (i <= hind); i++)
			if (! shift_stop_words.contains(hyp[i]))
			    all_hyp_stop = false;
		    if (all_hyp_stop) continue;
		    
		    for (int i = r_start; all_ref_stop && (i <= rind); i++)
			if (! shift_stop_words.contains(ref[i]))
			    all_ref_stop = false;
		    if (all_ref_stop) continue;		    
		}
		
		if ((! too_many_mismatch) && (! all_hyp_stop) && 
		    (! all_ref_stop) && (! no_err)) {
		    int moveto = ralign[r_start];
		    if ((moveto != h_start) &&
			((moveto < h_start) || (moveto > hind)) &&
			((moveto - h_start) <= MAX_SHIFT_DIST) &&
			((h_start - moveto) <= MAX_SHIFT_DIST)) {		    		
			for (int roff = -1; roff <= (hind - h_start); roff++) {
			    TERshift topush = null;		
			    if ((roff == -1) && (r_start == 0)) {
				topush = new TERshift(h_start, hind, -1, -1);
			    } else if ((r_start+roff >= 0) && 
				       (r_start+roff < ralign.length) &&
				       (h_start != ralign[r_start+roff]) &&
				       ((roff == 0) ||
					(ralign[r_start+roff] != ralign[r_start]))) {
				topush = new TERshift(h_start, hind, r_start+roff, ralign[r_start+roff]);			    
			    }
			    if (topush != null) {
				Comparable[] sh = new Comparable[(hind - h_start) + 1];
				for (int hl = 0; hl < sh.length; hl++) 
				    sh[hl] = hyp[h_start + hl];
				Comparable[] sr = new Comparable[(rind - r_start) + 1];
				for (int rl = 0; rl < sr.length; rl++) 
				    sr[rl] = ref[r_start + rl];
				topush.shifted = Arrays.asList(sh);
				topush.shiftedto = Arrays.asList(sr);
				topush.cost = costfunc.shift_cost(topush);
				int maxlen = (sr.length > sh.length) ? sr.length : sh.length; 
				if (maxlen > allshifts.length) maxlen = allshifts.length;
				allshifts[maxlen-1].add(topush);
			    }
			}
		    }
		}
	    } else {
		ok = false;
	    }
	}
    }
    
    private TERshift[][] gather_poss_shifts(Comparable[] hyp, Comparable[] ref,
					    boolean[] herr, boolean[] rerr, int[] ralign, int[] halign) {
	if ((shift_con == SHIFT_CON.NOSHIFT) || 
	    (MAX_SHIFT_SIZE <= 0) || (MAX_SHIFT_DIST <= 0)) {
	    TERshift[][] to_return = new TERshift[0][];
	    return to_return;
	}
	Set[] allshifts = new TreeSet[MAX_SHIFT_SIZE+1];
	for (int i = 0; i < allshifts.length; i++)
	    allshifts[i] = new TreeSet();
	
	TreeMap[] paramap = null;

	if (use_phrases && (costfunc.getPhraseTable() != null) && 
	    (shift_con != SHIFT_CON.EXACT) &&
	    (shift_con != SHIFT_CON.RELAX_NP)) {
	    paramap = new TreeMap[ref.length];
	    PhraseTable pt = costfunc.getPhraseTable();
	    for (int ri = 0; ri < ref.length; ri++) {
		paramap[ri] = new TreeMap();
	    }
	    for (int hi = 0; hi < hyp.length; hi++) {
		// We don't need to look at paraphrases that are
		// too far away to shift
		int starth =  hi-MAX_SHIFT_DIST;
		if (starth < 0) starth = 0;
		int endh = hi+MAX_SHIFT_DIST;
		if (endh >= hyp.length) endh = hyp.length-1;
		
		int startr = halign[starth];
		int endr = halign[endh];
		if (startr < 0) startr = 0;
		if (endr >= ref.length) endr = ref.length-1;
		for (int ri = startr; ri < (endr+1); ri++) {
		    ArrayList ph = pt.retrieve_all(ref, ri, hyp, hi);
		    if ((ph != null) && (ph.size() > 0)) {
			paramap[ri].put(hi, ph);
		    }
		}
	    }
	}
	
	for (int ri = 0; ri < ref.length; ri++) {
	    for (int hi = 0; hi < hyp.length; hi++) {
		_gather_exposs_shifts(hyp, ref,
				      herr, rerr, ralign, halign,
				      allshifts, paramap,
				      hi, ri, 0, 0, 0);
	    }
	}

	TERshift[][] to_return = new TERshift[MAX_SHIFT_SIZE+1][];
	for (int i = 0; i < to_return.length; i++) {
	    ArrayList al = new ArrayList(allshifts[i]);
	    to_return[i] = (TERshift[]) al.toArray(new TERshift[0]);
	}

	return to_return;
    }
    

  private Map BuildWordMatches(Comparable[] hyp, 
			       Comparable[] ref) {
	Set hwhash = new HashSet();
	for (int i = 0; i < hyp.length; i++) {
      hwhash.add(hyp[i]);
	}
	boolean[] cor = new boolean[ref.length];
	for (int i = 0; i < ref.length; i++) {
      if (hwhash.contains(ref[i])) {
		cor[i] = true;
      } else {
		cor[i] = false;
      }
	}

	List reflist = Arrays.asList(ref);
	HashMap to_return = new HashMap();
	for (int start = 0; start < ref.length; start++) {
      if (cor[start]) {
		for (int end = start; ((end < ref.length) &&
                               (end - start <= MAX_SHIFT_SIZE) &&
                               (cor[end]));
		     end++) {
          List topush = reflist.subList(start, end+1);
          if (to_return.containsKey(topush)) {
			Set vals = (Set) to_return.get(topush);
			vals.add(new Integer(start));
          } else {
			Set vals = new TreeSet();
			vals.add(new Integer(start));
			to_return.put(topush, vals);			
          }
		}
      }
	}
	return to_return;
  }    

  private void FindAlignErr(TERalignment align, boolean[] herr,
			    boolean[] rerr,
			    int[] ralign, int[] halign) {
	int hpos = -1;
	int rpos = -1;
	for (int i = 0; i < align.alignment.length; i++) {
      char sym = align.alignment[i];
      if (sym == ' ') {
		hpos++; rpos++;
		herr[hpos] = false; 
		rerr[rpos] = false;
		ralign[rpos] = hpos;
		halign[hpos] = rpos;
      } else if ((sym == 'S') || (sym == 'T') ||
		 (sym == 'Y')) {
		hpos++; rpos++;
		herr[hpos] = true; 
		rerr[rpos] = true;
		ralign[rpos] = hpos;
		halign[hpos] = rpos;
      } else if (sym == 'P') {
	  int srpos = rpos+1;
	  int shpos = hpos+1;
	  for (int j = 0; j < align.alignment_h[i]; j++) {
	      hpos++;
	      herr[hpos] = true;
	      halign[hpos] = srpos;
	  }
	  for (int j = 0; j < align.alignment_r[i]; j++) {
	      rpos++;
	      rerr[rpos] = true;	      
	      ralign[rpos] = shpos;
	  }	  
      } else if (sym == 'I') {
		hpos++;
		herr[hpos] = true; 
		halign[hpos] = rpos;
      } else if (sym == 'D') {
		rpos++; 		
		rerr[rpos] = true;
		ralign[rpos] = hpos;
      } else {
		System.err.print("Error!  Invalid mini align sequence " + sym + " at pos " + i + "\n");
		System.exit(-1);
      }
	}
  }

  private Object[] CalcBestShift(Comparable[] cur,
				 Comparable[] hyp, Comparable[] ref, 
				 TERalignment med_align) {
      /* 
	 return null if no good shift is found
	 or return Object[ TERshift bestShift, 
	 TERalignment cur_align ]
      */
      Object[] to_return = new Object[2];
      
      boolean anygain = false;
      
      /* Arrays that records which hyp and ref words are currently wrong */
      boolean[] herr = new boolean[hyp.length];
      boolean[] rerr = new boolean[ref.length];
	/* Array that records the alignment between ref and hyp */
	int[] ralign = new int[ref.length];
	int[] halign = new int[hyp.length];
	FindAlignErr(med_align, herr, rerr, ralign, halign);
	
	TERshift[][] poss_shifts;
	//	if ((Boolean) TERpara.para().get(TERpara.OPTIONS.RELAX_SHIFTS)) {
	//	    poss_shifts = GatherAllPossRelShifts(cur, ref, rloc, med_align, herr, rerr, ralign);		
	//	} else {

	poss_shifts = gather_poss_shifts(cur, ref, herr, rerr, ralign, halign);
	//	    poss_shifts = GatherAllPossShifts(cur, ref, rloc, med_align, herr, rerr, ralign);		
	    //	}
	double curerr = med_align.numEdits;
	
	if (DEBUG) {
      // CUT HERE        
	    System.out.println("Possible Shifts:");
	    for (int i = poss_shifts.length - 1; i >= 0; i--) {
		for (int j = 0; j < poss_shifts[i].length; j++) {
		    System.out.println(" [" + i + "] " + poss_shifts[i][j]);
		}
	    }
	    System.out.println("");
	    // CUT HERE
	}

	double cur_best_shift_cost = 0.0;
	TERalignment cur_best_align = med_align;
	TERshift cur_best_shift = new TERshift();	

	for (int i = poss_shifts.length - 1; i >= 0; i--) {
	    if (DEBUG) System.out.println("Considering shift of length " + i + " (" + poss_shifts[i].length  +")");
	    
	    /* Consider shifts of length i+1 */
	    double curfix = curerr - 
		(cur_best_shift_cost + cur_best_align.numEdits);
	    double maxfix = (2 * (1 + i));
	    
	    if ((curfix > maxfix) || 
		((cur_best_shift_cost != 0) && (curfix == maxfix))) {
		break;
	    }
	    
	    for (int s = 0; s < poss_shifts[i].length; s++) {		
		curfix = curerr - 
		    (cur_best_shift_cost + cur_best_align.numEdits);
		if ((curfix > maxfix) || 
		    ((cur_best_shift_cost != 0) && (curfix == maxfix))) {
		    break;
		}
		
		TERshift curshift = poss_shifts[i][s];
		
		Comparable[] shiftarr = PerformShift(cur, curshift);
		
		TERalignment curalign = MinEditDist(shiftarr, ref);
		
		curalign.hyp = hyp;
		curalign.ref = ref;
		curalign.aftershift = shiftarr;
		
		double gain = (cur_best_align.numEdits + cur_best_shift_cost) 
		    - (curalign.numEdits + curshift.cost);
		
		if (DEBUG) {
          System.out.println("Gain for " + curshift + " is " + gain + ". (result: [" + TERalignment.join(" ", shiftarr) + "]");
          System.out.println("" + curalign + "\n");		
		}

		if ((gain > 0) || ((cur_best_shift_cost == 0) && (gain == 0))) {
          anygain = true;
          cur_best_shift = curshift;
          cur_best_shift_cost = curshift.cost;
          cur_best_align = curalign;
          if (DEBUG) System.out.println("Tmp Choosing shift: " + cur_best_shift + " gives:\n" + cur_best_align + "\n");
		}

      }
	}

	if (anygain) {
      to_return[0] = cur_best_shift;
      to_return[1] = cur_best_align;	    
      return to_return;
	} else {
      if (DEBUG) System.out.println("No good shift found.\n");
      return null;
	}
  }


//   private TERshift[][] GatherAllPossShifts(Comparable[] hyp, Comparable[] ref, Map rloc,
// 					   TERalignment align,
// 					   boolean[] herr, boolean[] rerr, int[] ralign) {
      
//       // Don't even bother to look if shifts can't be done
//       if ((MAX_SHIFT_SIZE <= 0) || (MAX_SHIFT_DIST <= 0)) {
// 	  TERshift[][] to_return = new TERshift[0][];
// 	  return to_return;
//       }
      
//       ArrayList[] allshifts = new ArrayList[MAX_SHIFT_SIZE+1];
//       for (int i = 0; i < allshifts.length; i++)
// 	  allshifts[i] = new ArrayList();

//       List hyplist = Arrays.asList(hyp);	
//       for (int start = 0; start < hyp.length; start++) {
// 	  if (! rloc.containsKey(hyplist.subList(start, start+1))) continue;
	  
// 	  boolean ok = false;
// 	  Iterator mti = ((Set) rloc.get(hyplist.subList(start, start+1))).iterator();
// 	  while (mti.hasNext() && (! ok)) {
// 	      int moveto = ((Integer) mti.next()).intValue();
// 	      if ((start != ralign[moveto]) &&
// 		  (ralign[moveto] - start <= MAX_SHIFT_DIST) &&
// 		  ((start - ralign[moveto] - 1) <= MAX_SHIFT_DIST)) 
// 		  ok = true;		
// 	  }
// 	  if (! ok) continue;
	  
// 	  ok = true;
// 	  for (int end = start; (ok && (end < hyp.length) && (end < start + MAX_SHIFT_SIZE));
// 	       end++) {
	      
// 	      /* check if cand is good if so, add it */
// 	      List cand = hyplist.subList(start, end+1);
// 	      ok = false;		
// 	      if (! (rloc.containsKey(cand))) continue;
	      
// 	      boolean any_herr = false;
// 	      for (int i = 0; (i <= end - start) && (! any_herr); i++) {
// 		  if (herr[start+i]) any_herr = true;
// 	      }
	      
// 	      if (any_herr == false) {
// 		  ok = true;
// 		  continue;
// 	      }
	      
// 	      Iterator movetoit = ((Set) rloc.get(cand)).iterator();
// 	      while (movetoit.hasNext()) {
// 		  int moveto = ((Integer) movetoit.next()).intValue();
// 		  if (! ((ralign[moveto] != start) &&
// 			 ((ralign[moveto] < start) || (ralign[moveto] > end)) &&
// 			 ((ralign[moveto] - start) <= MAX_SHIFT_DIST) &&
// 			 ((start - ralign[moveto]) <= MAX_SHIFT_DIST)))
// 		      continue;
// 		  ok = true;
		  
// 		  /* check to see if there are any errors in either string
// 		     (only move if this is the case!)
// 		  */
		  
// 		  boolean any_rerr = false;
// 		  for (int i = 0; (i <= end - start) && (! any_rerr); i++) {
// 		      if (rerr[moveto+i]) any_rerr = true;
// 		  }
// 		  if (! any_rerr) continue;
		  
// 		  for (int roff = -1; roff <= (end - start); roff++) {
// 		      TERshift topush = null;
// 		      if ((roff == -1) && (moveto == 0)) {
// 			  if (DEBUG) System.out.println("Consider making " + start + "..." + end + " moveto: " + moveto + " roff: " 
// 							+ roff + " ralign[mt+roff]: " + -1); 
			  
// 			  topush = new TERshift(start, end, -1, -1);
// 		      } else if ((start != ralign[moveto+roff]) &&
// 				 ((roff == 0) || 
// 				  (ralign[moveto+roff] != ralign[moveto]))) {
// 			  int newloc = ralign[moveto+roff];
// 			  if (DEBUG) System.out.println("Consider making " + start + "..." + end + " moveto: " + moveto + " roff: " 
// 							+ roff + " ralign[mt+roff]: " + newloc); 			  
// 			  topush = new TERshift(start, end, moveto+roff, newloc);
// 		      }
// 		      if (topush != null) {
// 			  topush.shifted = cand;
// 			  topush.cost  = costfunc.shift_cost(topush);
// 			  allshifts[end - start].add(topush);
// 		      }	      
// 		  }		
// 	      }
// 	  }
//       }

//       TERshift[][] to_return = new TERshift[MAX_SHIFT_SIZE+1][];
//       for (int i = 0; i < to_return.length; i++) {
// 	  to_return[i] = (TERshift[]) allshifts[i].toArray(new TERshift[0]);
//       }
//       return to_return;
//   }



  public Comparable[] PerformShift(Comparable[] words, TERshift s) {
	return PerformShift(words, s.start, s.end, s.newloc);
  }

    public static Object[] PerformShiftNS(Object[] words, TERshift s) {
	int c = 0;
	Object[] nwords = words.clone();
	int start = s.start; 
	int end = s.end; 
	int newloc = s.newloc;

	if (newloc == -1) {
	    for (int i = start; i<=end;i++) 
		nwords[c++] = words[i];
	    for (int i = 0; i<=start-1;i++) 
		nwords[c++] = words[i]; 	  
	    for (int i = end+1; i<words.length;i++) 
		nwords[c++] = words[i];
	} else if (newloc < start) {
	    for (int i = 0; i<=newloc; i++) 
		nwords[c++] = words[i]; 
	    for (int i = start; i<=end;i++) 
		nwords[c++] = words[i]; 
	    for (int i = newloc+1; i<=start-1;i++) 
		nwords[c++] = words[i];
	    for (int i = end+1; i<words.length;i++) 
		nwords[c++] = words[i]; 
	} else if (newloc > end) {
	    for (int i = 0; i<=start-1; i++) 
		nwords[c++] = words[i];
	    for (int i = end+1; i<=newloc;i++)
		nwords[c++] = words[i]; 
	    for (int i = start; i<=end;i++) 
		nwords[c++] = words[i];
	    for (int i = newloc+1; i<words.length;i++) 
		nwords[c++] = words[i]; 
	} else {
	    // we are moving inside of ourselves
	    for (int i = 0; i<=start-1; i++)  
		nwords[c++] = words[i];	      
	    for (int i = end+1; (i< words.length) && (i<=(end + (newloc - start))); i++) 
		nwords[c++] = words[i]; 
	    for (int i = start; i<=end;i++) 
		nwords[c++] = words[i]; 
	    for (int i = (end + (newloc - start)+1); i<words.length;i++)
		nwords[c++] = words[i];
	}
	return nwords;
    }
    
    private Comparable[] PerformShift(Comparable[] words, int start, int end, int newloc) {	
	int c = 0;
	Comparable[] nwords = (Comparable[]) words.clone();
	
	if(DEBUG) { 
	    System.out.println("word length: " + words.length);
	}
      
	if (newloc == -1) {
	    for (int i = start; i<=end;i++) nwords[c++] = words[i];  
	    for (int i = 0; i<=start-1;i++) nwords[c++] = words[i];
	    for (int i = end+1; i<words.length;i++) nwords[c++] = words[i];
	} else if (newloc < start) {
	    for (int i = 0; i<=newloc; i++) nwords[c++] = words[i];	  
	    for (int i = start; i<=end;i++) nwords[c++] = words[i]; 
	    for (int i = newloc+1; i<=start-1;i++) nwords[c++] = words[i];
	    for (int i = end+1; i<words.length;i++) nwords[c++] = words[i];
	} else if (newloc > end) {
	    for (int i = 0; i<=start-1; i++) nwords[c++] = words[i]; 
	    for (int i = end+1; i<=newloc;i++) nwords[c++] = words[i]; 
	    for (int i = start; i<=end;i++) nwords[c++] = words[i]; 
	    for (int i = newloc+1; i<words.length;i++) nwords[c++] = words[i]; 
	} else {
	    // we are moving inside of ourselves
	    for (int i = 0; i<=start-1; i++) nwords[c++] = words[i]; 
	    for (int i = end+1; (i< words.length) && (i<=(end + (newloc - start))); i++) nwords[c++] = words[i]; 
	    for (int i = start; i<=end;i++) nwords[c++] = words[i]; 
	    for (int i = (end + (newloc - start)+1); i<words.length;i++) nwords[c++] = words[i];
	}
	NUM_SHIFTS_CONSIDERED++;
	
	return nwords;
    }
    
    private TERalignment MinEditDist(Comparable[] hyp, Comparable[] ref) {	
	if (costfunc.is_multi()) {
	    return MinEditDist_MW(hyp, ref);
	} else {
	    return MinEditDist_SW(hyp, ref);
	}
    }

    private TERalignment MinEditDist_SW(Comparable[] hyp, Comparable[] ref) {
	double current_best = INF;
	double last_best = INF;
	int first_good = 0;
	int current_first_good = 0;
	int last_good = -1;
	int cur_last_good = 0;
	int last_peak = 0;
	int cur_last_peak = 0;
	int i, j;
	double cost, icost, dcost;
	double score;

	int hwsize = hyp.length-1;
	int rwsize = ref.length-1;
	
	NUM_BEAM_SEARCH_CALLS++;

	PhraseTable pt = null;
	if (use_phrases) {
	    pt = costfunc.getPhraseTable();
	}

	if ((ref.length+1 > S.length) || (hyp.length+1 > S.length)) {
	    int max = (hyp.length > ref.length) ? hyp.length : ref.length;
	    max += 26; // we only need a +1 here, but let's pad for future use
	    S = new double[max][max];
	    P = new char[max][max];
	    PR = new int[max][max];
	    PH = new int[max][max];
	    PL = new PhraseTree.OffsetScore[max][max];
	}
	
	for (i=0; i <= ref.length; i++){
	    for (j=0; j <= hyp.length; j++){
		S[i][j]=-1.0;
		P[i][j]='0';
		PR[i][j]=0;
		PH[i][j]=0;
		PL[i][j] = null;
	    }
	}
 	S[0][0] = 0.0;
	
	for (j=0; j <= hyp.length; j++) {
	    last_best = current_best;
	    current_best = INF;
	    
	    first_good = current_first_good;
	    current_first_good = -1;
	    
	    last_good = cur_last_good;
	    cur_last_good = -1;
	    
	    last_peak = cur_last_peak;
	    cur_last_peak = 0;
	    
	    for (i=first_good; i <= ref.length; i++) {
		if ((j != hyp.length) && (i > last_good))
		    break;

		if (S[i][j] < 0) 
		    continue;
		score = S[i][j];
		
		if ((j < hyp.length) && (score > last_best+BEAM_WIDTH))
		    continue;

		if (current_first_good == -1)
		    current_first_good = i;
				
		if ((i < ref.length) && (j < hyp.length)) {
		    if (ref[i].equals(hyp[j])) {
			cost = costfunc.match_cost(hyp[j], ref[i]) + score;
			if ((S[i+1][j+1] == -1) || (cost < S[i+1][j+1])) {
			    S[i+1][j+1] = cost;
			    P[i+1][j+1] = ' ';
			}
			if (cost < current_best)
			    current_best = cost;
			
			if (current_best == cost)
			    cur_last_peak = i+1;
		    } else {
			boolean are_stems = false;
			boolean are_syns = false;
			if (use_porter)  are_stems = Porter.equivStems(hyp[j], ref[i]);
			if (use_wordnet) are_syns = WordNet.areSynonyms(hyp[j], ref[i]); 

			cost = costfunc.substitute_cost(hyp[j], ref[i]) + score;
			if ((S[i+1][j+1] <0) || (cost < S[i+1][j+1])) {
			    S[i+1][j+1] = cost;
			    P[i+1][j+1] = 'S';
			    if (cost < current_best)
				current_best = cost;
			    if (current_best == cost)
				cur_last_peak = i+1 ;
			}
			
			if (are_stems) {
			    cost = costfunc.stem_cost(hyp[j], ref[i]) + score;
			    if ((S[i+1][j+1] <0) || (cost < S[i+1][j+1])) {
				S[i+1][j+1] = cost;
				P[i+1][j+1] = 'T';
				if (cost < current_best)
				    current_best = cost;
				if (current_best == cost)
				    cur_last_peak = i+1 ;
			    }
			}
			
			if (are_syns) {
			    cost = costfunc.syn_cost(hyp[j], ref[i]) + score;
			    if ((S[i+1][j+1] <0) || (cost < S[i+1][j+1])) {
				S[i+1][j+1] = cost;
				P[i+1][j+1] = 'Y';
				if (cost < current_best)
				    current_best = cost;
				if (current_best == cost)
				    cur_last_peak = i+1 ;
			    }
			}			
		    }
		}
		cur_last_good = i+1;
		
		if (pt != null) {
		    ArrayList phrases = pt.retrieve_all(ref, i, hyp, j);
		    for (int pi = 0; pi < phrases.size(); pi++) {
			PhraseTree.OffsetScore ph = (PhraseTree.OffsetScore) phrases.get(pi);
			cost = score+ph.cost;
			if ((S[i+ph.ref_len][j+ph.hyp_len] < 0) || (cost < S[i+ph.ref_len][j+ph.hyp_len])) {
			    S[i+ph.ref_len][j+ph.hyp_len] = cost;
			    P[i+ph.ref_len][j+ph.hyp_len] = 'P';
			    PL[i+ph.ref_len][j+ph.hyp_len] = ph;
			}
			// System.out.println(" At i=" + i + " j=" + j + " found phrase: "+ ph +"\n");
		    }
		}

		if  (j < hyp.length) {
		    icost = score+costfunc.insert_cost(hyp[j]);
		    if ((S[i][j+1] < 0) || (S[i][j+1] > icost)) {
			S[i][j+1] = icost;
			P[i][j+1] = 'I';
			if (( cur_last_peak <  i) && ( current_best ==  icost))
			    cur_last_peak = i;
		    }
		}		
		
		if  (i < ref.length) {
		    dcost =  score + costfunc.delete_cost(ref[i]);
		    if ((S[ i+1][ j]<0.0) || ( S[i+1][j] > dcost)) {
			S[i+1][j] = dcost;
			P[i+1][j] = 'D';
			if (i >= last_good)
			    last_good = i + 1 ;
		    }		
		}
	    }
	}
	
	int tracelength = 0;
	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
	    tracelength++;
	    if (P[i][j] == ' ') {
		i--; j--;
	    } else if ((P[i][j] == 'S') || (P[i][j] == 'Y') || 
		       (P[i][j] == 'T')) {
		i--; j--;
	    } else if (P[i][j] == 'D') {
		i--;
	    } else if (P[i][j] == 'I') {
		j--;
	    } else if (P[i][j] == 'P') {
		PhraseTree.OffsetScore os = PL[i][j];
		i -= os.ref_len;
		j -= os.hyp_len;
	    } else {
		System.out.println("Invalid path: P[" + i +"][" + j + "] = " + P[i][j]);
		System.out.println("Ref Len=" + ref.length + " Hyp Len=" + hyp.length + " TraceLength=" + tracelength);
		System.exit(-1);
	    }
	}
	char[] path = new char[tracelength];
	int[] r_path = new int[tracelength];
	int[] h_path = new int[tracelength];
	double[] cost_path = new double[tracelength];
	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
	    path[--tracelength] = P[i][j];
	    cost_path[tracelength] = S[i][j];
	    if (P[i][j] == ' ') {		
		i--; j--;
		r_path[tracelength] = 1;
		h_path[tracelength] = 1;
	    } else if ((P[i][j] == 'S') || (P[i][j] == 'T') || 
		       (P[i][j] == 'Y')) {
		i--; j--;
		r_path[tracelength] = 1;
		h_path[tracelength] = 1;
	    } else if (P[i][j] == 'D') {
		i--;
		r_path[tracelength] = 1;
		h_path[tracelength] = 0;
	    } else if (P[i][j] == 'I') {
		j--;
		r_path[tracelength] = 0;
		h_path[tracelength] = 1;
	    } else if (P[i][j] == 'P') {
		PhraseTree.OffsetScore os = PL[i][j];
		i -= os.ref_len;
		j -= os.hyp_len;
		r_path[tracelength] = os.ref_len;
		h_path[tracelength] = os.hyp_len;
	    }
	    cost_path[tracelength] -= S[i][j];
	}
	TERalignment to_return = new TERalignment(costfunc);
	to_return.numWords = ref.length;
	to_return.alignment = path;
	to_return.alignment_h = h_path;
	to_return.alignment_r = r_path;
	to_return.alignment_cost = cost_path;
	to_return.numEdits = S[ref.length][hyp.length];

	return to_return;
    }

    private TERalignment MinEditDist_MW(Comparable[] hyp, Comparable[] ref) {
	TERMultiCost tmc = (TERMultiCost) costfunc;
	
	double current_best = INF;
	double last_best = INF;
	int first_good = 0;
	int current_first_good = 0;
	int last_good = -1;
	int cur_last_good = 0;
	int last_peak = 0;
	int cur_last_peak = 0;
	int i, j;
	double cost, icost, dcost;
	double score;

	int hwsize = hyp.length-1;
	int rwsize = ref.length-1;
	
	NUM_BEAM_SEARCH_CALLS++;

	PhraseTable pt = null;
	if (use_phrases) {
	    pt = costfunc.getPhraseTable();
	}

	if ((ref.length+1 > S.length) || (hyp.length+1 > S.length)) {
	    int max = (hyp.length > ref.length) ? hyp.length : ref.length;
	    max += 26; // we only need a +1 here, but let's pad for future use
	    S = new double[max][max];
	    P = new char[max][max];
	    PH = new int[max][max];
	    PR = new int[max][max];
	    PL = new PhraseTree.OffsetScore[max][max];
	}
	boolean[] has_hyp_v = new boolean[hyp.length+1];	
	has_hyp_v[0] = true;
	for (j=1; j <= hyp.length; j++){has_hyp_v[j] = false;}
			
	for (i=0; i <= ref.length; i++){
	    for (j=0; j <= hyp.length; j++){
		S[i][j]= -1.0;
		P[i][j]= '-';
		PR[i][j]=0;
		PH[i][j]=0;
		PL[i][j] = null;
	    }
	}
 	S[0][0] = 0.0;
	//	showInternalTable(hyp, ref);

	for (j=0; j <= hyp.length; j++) {
	    if (has_hyp_v[j] == false) continue;

	    List<Integer> h_lens = tmc.get_multi_all(hyp, j);

	    last_best = current_best;
	    current_best = INF;
	    
	    first_good = current_first_good;
	    current_first_good = -1;
	    
	    last_good = cur_last_good;
	    cur_last_good = -1;
	    
	    last_peak = cur_last_peak;
	    cur_last_peak = 0;

	    //	    if (first_good == -1) 
	    //		first_good = 0;
	    
	    //	    System.out.printf("j=%d First good=%d\n", j, first_good);
	    //	    showInternalTable(hyp, ref);
	    for (i=first_good; i <= ref.length; i++) {
		if ((j != hyp.length) && (i > last_good))
		    break;
		    
		if (S[i][j] < 0) 
		    continue;
		score = S[i][j];
		
		if ((j < hyp.length) && (score > last_best+BEAM_WIDTH))
		    continue;
		
		if (current_first_good == -1)
		    current_first_good = i;

		List<Integer> r_lens = tmc.get_multi_all(ref, i);		
		/*
		System.out.println("Examining i=" + i + " j=" + j + " S=" +
				   S[i][j] + " P=" + P[i][j] + 
				   " PH=" + PH[i][j] + 
				   " PR=" + PR[i][j]);
		*/
		if ((i < ref.length) && (j < hyp.length)) {
		    for (Integer hiI : h_lens) {
			int hi = hiI.intValue();
			for (Integer riI : r_lens) {
			    int ri = riI.intValue();
			    /*			    
			    System.out.printf("At %d,%d. hi=%d ri=%d\n",
					      i, j, hi, ri);
			    */
			    
			    boolean isMatch = false;
			    if (hi == ri) {
				isMatch = true;
				for (int g = 0; isMatch && (g < hi); g++) {
				    if (! ref[i+g].equals(hyp[j+g])) 
					isMatch = false;
				}
			    }
			    
			    if (isMatch) { 
				cost = tmc.match_cost(hyp, j, hi, ref, i, ri) + score;
				if ((S[i+ri][j+hi] == -1) || (cost < S[i+ri][j+hi])) {
				    S[i+ri][j+hi] = cost;
				    P[i+ri][j+hi] = ' ';				    
				    PH[i+ri][j+hi] = hi;
				    PR[i+ri][j+hi] = ri;
				    has_hyp_v[j+hi] = true;
				}
				if (cost < current_best)
				    current_best = cost;
				
				if (current_best == cost)
				    cur_last_peak = i+ri;

				cur_last_good = i+ri;
			    } else {
				boolean are_stems = false;
				boolean are_syns = false;
				if ((ri == hi) && (ri == 1)) {
				    if (use_porter)  are_stems = Porter.equivStems(hyp[j], ref[i]);
				    if (use_wordnet) are_syns = WordNet.areSynonyms(hyp[j], ref[i]); 
				}
				
				cost = tmc.substitute_cost(hyp, j, hi, ref, i, ri) + score;
				if ((S[i+ri][j+hi] <0) || (cost < S[i+ri][j+hi])) {
				    S[i+ri][j+hi] = cost;
				    P[i+ri][j+hi] = 'S';
				    PH[i+ri][j+hi] = hi;
				    PR[i+ri][j+hi] = ri;
				    has_hyp_v[j+hi] = true;
				    if (cost < current_best)
					current_best = cost;
				    if (current_best == cost)
					cur_last_peak = i+ri ;
				}
				
				if (are_stems) {
				    cost = tmc.stem_cost(hyp, j, hi, ref, i, ri) + score;
				    if ((S[i+ri][j+hi] <0) || (cost < S[i+ri][j+hi])) {
					S[i+ri][j+hi] = cost;
					P[i+ri][j+hi] = 'T';
					PH[i+ri][j+hi] = hi;
					PR[i+ri][j+hi] = ri;
					has_hyp_v[j+hi] = true;
					if (cost < current_best)
					    current_best = cost;
					if (current_best == cost)
					    cur_last_peak = i+ri ;
				    }
				}
				
				if (are_syns) {
				    cost = tmc.syn_cost(hyp, j, hi, ref, i, ri) + score;
				    if ((S[i+ri][j+hi] <0) || (cost < S[i+ri][j+hi])) {
					S[i+ri][j+hi] = cost;
					P[i+ri][j+hi] = 'Y';
					PH[i+ri][j+hi] = hi;
					PR[i+ri][j+hi] = ri;	
					has_hyp_v[j+hi] = true;			    
					if (cost < current_best)
					    current_best = cost;
					if (current_best == cost)
					    cur_last_peak = i+ri;
				    }
				}
			    }
			}
		    }
		}
		cur_last_good = i+1;
		
		if (pt != null) {
		    ArrayList phrases = pt.retrieve_all(ref, i, hyp, j);
		    for (int pi = 0; pi < phrases.size(); pi++) {
			PhraseTree.OffsetScore ph = (PhraseTree.OffsetScore) phrases.get(pi);
			cost = score+ph.cost;
			if ((S[i+ph.ref_len][j+ph.hyp_len] < 0) || (cost < S[i+ph.ref_len][j+ph.hyp_len])) {
			    S[i+ph.ref_len][j+ph.hyp_len] = cost;
			    P[i+ph.ref_len][j+ph.hyp_len] = 'P';
			    PL[i+ph.ref_len][j+ph.hyp_len] = ph;
			    PH[i+ph.ref_len][j+ph.hyp_len] = ph.hyp_len;
			    PR[i+ph.ref_len][j+ph.hyp_len] = ph.ref_len;
			    has_hyp_v[j+ph.hyp_len] = true;
			}
			// System.out.println(" At i=" + i + " j=" + j + " found phrase: "+ ph +"\n");
		    }
		}
		
		if  (j < hyp.length) {
		    List<Integer> th_lens = tmc.get_multi_all(hyp, j);
		    for (Integer hiI : th_lens) {
			int hi = hiI.intValue();
			// System.out.printf("At %d,%d I hi=%d\n", i, j, hi);
			icost = score+tmc.insert_cost(hyp, j, hi);
			if ((S[i][j+hi] < 0) || (S[i][j+hi] > icost)) {
			    S[i][j+hi] = icost;
			    P[i][j+hi] = 'I';
			    PH[i][j+hi] = hi;
			    PR[i][j+hi] = 0;
			    has_hyp_v[j+hi] = true;
			    if (( cur_last_peak <  i) && ( current_best ==  icost))
				cur_last_peak = i;
			}
		    }
		}		
			
		if  (i < ref.length) {
		    List<Integer> tr_lens = tmc.get_multi_all(ref, i);
		    for (Integer riI2 : tr_lens) {
			int ri = riI2.intValue();
			if (i+ri <= ref.length) {
			    // System.out.printf("At %d,%d D ri=%d\n", i, j, ri);
			    dcost =  score + tmc.delete_cost(ref, i, ri);
			    if ((S[i+ri][j]<0.0) || (S[i+ri][j] > dcost)) {
				S[i+ri][j] = dcost;
				P[i+ri][j] = 'D';
				PR[i+ri][j] = ri;
				PH[i+ri][j] = 0;
				if (i >= last_good)
				    last_good = i + ri;
			    }
			}		
		    }
		}
	    }
	}

	//	System.out.println("\nInternal Table:");
	//	showInternalTable(hyp, ref);
	int tracelength = 0;

	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
	    tracelength++;
	    if ((PR[i][j] == 0) && (PH[i][j] == 0)) {
		System.out.println("Invalid path: P[" + i +"][" + j + "] = " + P[i][j]);
		System.out.println("             PH[" + i +"][" + j + "] = " + PH[i][j]);
		System.out.println("             PR[" + i +"][" + j + "] = " + PR[i][j]);
		System.out.println("Ref Len=" + ref.length + " Hyp Len=" + hyp.length + " TraceLength=" + tracelength);
		System.exit(-1);
	    }
	    /*
	    System.out.printf("PR[%d][%d] = %d PH[%d][%d] = %d\n",
			       i, j, PR[i][j], i, j, PH[i][j]);
	    */
	    i -= PR[i][j];
	    j -= PH[i][j];

	}
	char[] path = new char[tracelength];
	int[] r_path = new int[tracelength];
	int[] h_path = new int[tracelength];
	double[] cost_path = new double[tracelength];
	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
	    tracelength--;
	    path[tracelength] = P[i][j];
	    cost_path[tracelength] = S[i][j];
	    r_path[tracelength] = PR[i][j];
	    h_path[tracelength] = PH[i][j];
	    i -= r_path[tracelength];
	    j -= h_path[tracelength];
	    cost_path[tracelength] -= S[i][j];
	}
	/*	
	System.out.println("Traceback Path:  (" + TERalignment.join("", path) + ")");
	System.out.println("Traceback RPath: (" + TERalignment.join("", r_path) + ")");
	System.out.println("Traceback HPath: (" + TERalignment.join("", h_path) + ")");
	System.out.println("Traceback Ref:   " + TERalignment.join(" ", ref) + "");
	System.out.println("Traceback Hyp:   " + TERalignment.join(" ", hyp) + "");
	*/
	TERalignment to_return = new TERalignment(costfunc);
	to_return.numWords = ref.length;
	to_return.alignment = path;
	to_return.alignment_h = h_path;
	to_return.alignment_r = r_path;
	to_return.alignment_cost = cost_path;
	to_return.numEdits = S[ref.length][hyp.length];

	return to_return;
    }
    
  /* Accessor functions to some internal counters */
  public int numBeamCalls () { return NUM_BEAM_SEARCH_CALLS; }
  public int numSegsScored () { return NUM_SEGMENTS_SCORED; }
  public int numShiftsTried () { return NUM_SHIFTS_CONSIDERED; }

  /* We may want to add some function to change the beam width */
  public int BEAM_WIDTH = 20;

    public String get_info() {
	String s = ("TER Calculation\n" +
		    " Stemming Used: " + use_porter + "\n" + 
		    " Synonyms Used: " + use_wordnet + "\n" +
		    " Beam Width: " + BEAM_WIDTH + "\n" + 
		    " Max Shift Size: " + MAX_SHIFT_SIZE + "\n" + 
		    " Max Shift Dist: " + MAX_SHIFT_DIST + "\n" +
		    " Shifting Constraint: " + shift_con + "\n" + 
		    " Number of Shifting Stop Words: " + shift_stop_words.size() + "\n" +
		    " Size of MinEditDist Matrix: " + S.length + "x" + S.length + "\n" + 
		    " Number of Segments Scored: " + NUM_SEGMENTS_SCORED + "\n" + 
		    " Number of Shifts Considered: " + NUM_SHIFTS_CONSIDERED + "\n" + 
		    " Number of Calls to Beam Search: " + NUM_BEAM_SEARCH_CALLS + "\n" + 
		    " Total Calc Time: " + String.format("%.3f sec\n", getCalcTime()) +
		    costfunc.get_info()
		    );
	return s;
    }
    
  private boolean use_porter = false;
  private boolean use_wordnet = false;
    private boolean use_phrases = false;

  private static final double INF = 999999.0;

  private int MAX_SHIFT_SIZE = 10;
  private int MAX_SHIFT_DIST = 50;
   
  /* Variables for some internal counting.  */
  private int NUM_SEGMENTS_SCORED = 0;
  private int NUM_SHIFTS_CONSIDERED = 0;
  private int NUM_BEAM_SEARCH_CALLS = 0;

 
  /* These are resized by the MIN_EDIT_DIST code if they aren't big enough */
  private double[][] S = new double[350][350];
  private char[][] P = new char[350][350];   
  private int[][] PR = new int[350][350];   
  private int[][] PH = new int[350][350];   
  private PhraseTree.OffsetScore[][] PL = new PhraseTree.OffsetScore[350][350];   

    private static enum SHIFT_CON {
	NOSHIFT, EXACT, RELAX_NP, RELAX, ALLOW_MISMATCH
    }
    
    private SHIFT_CON shift_con = SHIFT_CON.EXACT;
    private int max_mismatch_num = 1;
    private double max_mismatch_percent = 0.5;
    private HashSet shift_stop_words = new HashSet();

    public void loadShiftStopWordList(String fname) {
	if ((fname == null) || fname.equals("")) {
	    shift_stop_words = new HashSet();
	    return;
	}	
	try {
	    BufferedReader fh = new BufferedReader(new FileReader(fname));
	    shift_stop_words = new HashSet();
	    String line;
	    while ((line = fh.readLine()) != null) {
		line = line.trim();
		if (line.length() == 0) continue;
		String[] wds = NormalizeText.process(line);
		Comparable[] nswds = costfunc.process_input_hyp(wds);
		for (int i = 0; i < nswds.length; i++) {
		    shift_stop_words.add(nswds[i]);
		}
	    }
	    fh.close();
	} catch (IOException ioe) {
	    System.out.println("Loading shift stop word list from " + 
			       fname + ": " + ioe);
	    System.exit(-1);
	}
    }

    public void setShiftCon(String sc) {
	if (sc.equals("exact")) {
	    shift_con = SHIFT_CON.EXACT;
	} else if (sc.equals("relax_nophrase")) {
	    shift_con = SHIFT_CON.RELAX_NP;
	} else if (sc.equals("relax")) {
	    shift_con = SHIFT_CON.RELAX;
	} else if (sc.equals("allow_mismatch")) {
	    shift_con = SHIFT_CON.ALLOW_MISMATCH;
	} else if (sc.equals("noshift")) {
	    shift_con = SHIFT_CON.NOSHIFT;
	} else {
	    System.err.println("Invalid shift constraint: " + sc + "\n"
			       + "  valid constraints are: noshift, exact, relax_nophrase, relax and allow_mismatch");
	    System.exit(-1);
	}
    }

}
