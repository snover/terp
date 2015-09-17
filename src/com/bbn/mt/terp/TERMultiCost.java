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

TERcost.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/


/* 

If you wish to experiment with other cost functions, (including
specific word to word cost matrices), a child class of TERcost should
be made, and an instance of it should be passed as the third arguement
to the TER function in TERcalc.

All costs must be in the range of 0.0 to 1.0.  Deviating outside of
this range may break the functionality of the TERcalc code.

This code does not do phrasal costs functions, such modification would require
changes to the TERcalc code.

In addition shifts only occur when the two subsequences are equal(),
and hash to the same location.  This can be modified from the standard
definition by using a new Comparable data structure which redefines
those functions.

*/

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class TERMultiCost extends TERcost {
  /* For all of these functions, the score should be between 0 and 1
   * (inclusive).  If it isn't, then it will break TERcalc! */

    static {
	IS_MULTI = true;
	COST_NAME = "Default TER MultiCost";
    }

    protected static SequenceTree multitree = new SequenceTree();

    public void insert_multi(Comparable seq[], Object o) {
	insert_multi(seq, 0, seq.length, o);
    }

    public void insert_multi(Comparable seq[], int start, int len, Object o) {
	multitree.insert(seq, start, len, o);
    }
        
    public List<Integer> get_multi_all(Comparable[] seq, int start) {
	if (start >= seq.length) {
	    return new ArrayList<Integer>();
	}

	List<Integer> tr = multitree.find_len(seq, start);
	if (tr.size() == 0) {
	    ArrayList<Integer> oneLen = new ArrayList();
	    oneLen.add(new Integer(1));
	    tr = oneLen;
	}
	return tr;
    }

    /* The cost of matching ref word for hyp word. (They are equal) */
    public double match_cost(Comparable[] hyp, Comparable[] ref) { 
	return match_cost(hyp, 0, hyp.length, ref, 0, ref.length); 
    }
    public double match_cost(Comparable[] hyp, int hstart, int hlen, 
			     Comparable[] ref, int rstart, int rlen) { 
	return _match_cost; 
    }
    
    /* The cost of substituting ref word for hyp word. (They are not equal) */
    public double substitute_cost(Comparable[] hyp, Comparable[] ref) {
	return substitute_cost(hyp, 0, hyp.length, ref, 0, ref.length); 
    }
    public double substitute_cost(Comparable[] hyp, int hstart, int hlen, 
				  Comparable[] ref, int rstart, int rlen) { 
	return _substitute_cost; 
    }

    /* The cost of inserting the hyp word */
    public double insert_cost(Comparable[] hyp) {
	return insert_cost(hyp, 0, hyp.length); 
    }
    public double insert_cost(Comparable[] hyp, int hstart, int hlen) {
	return _insert_cost;
    }
    
    /* The cost of deleting the ref word */
    public double delete_cost(Comparable[] ref) {
	return delete_cost(ref, 0, ref.length); 
    }
    public double delete_cost(Comparable[] ref, int rstart, int rlen) {
	return _delete_cost;
    }

    /* The cost of making a shift */
    public double shift_cost(TERshift shift) {
	return _shift_cost;
    }

    // If they are stems (from Porter)
    public double stem_cost(Comparable[] hyp, Comparable[] ref) {
	return stem_cost(hyp, 0, hyp.length, ref, 0, ref.length); 
    }
    public double stem_cost(Comparable[] hyp, int hstart, int hlen,
			    Comparable[] ref, int rstart, int rlen) {
	return _stem_cost;
    }

    // If they are synonyms (from WordNet)
    public double syn_cost(Comparable[] hyp, Comparable[] ref) {
	return syn_cost(hyp, 0, hyp.length, ref, 0, ref.length); 
    }
    // If they are synonyms (from WordNet)
    public double syn_cost(Comparable[] hyp, int hstart, int hlen,
			   Comparable[] ref, int rstart, int rlen) {
	return _syn_cost;
    }

    public int get_num_features() { 
	if (_phraseTable == null) return 7;
	return 7 + _phraseTable.num_params();
    }

    public void get_wgt_edit(double[] wgt, char type, Comparable hyp, Comparable ref) {
	switch (type) {
	case ' ': wgt[0]++; break;
	case 'I': wgt[1]++; break;
	case 'D': wgt[2]++; break;
	case 'S': wgt[3]++; break;
	case 'T': wgt[4]++; break;
	case 'Y': wgt[5]++; break;
	}
	return;
    }

    public void get_wgt_shift(double[] wgt, TERshift shift) {
	wgt[6]++;
    }

    public double[] current_weights() {
	double[] tr = new double[get_num_features()];
	tr[0] = _match_cost;
	tr[1] = _insert_cost;
	tr[2] = _delete_cost;
	tr[3] = _substitute_cost;
	tr[4] = _stem_cost;
	tr[5] = _syn_cost;
	tr[6] = _shift_cost;
	if (_phraseTable != null) {
	    _phraseTable.current_weights(7, tr);
	}
	return tr;
    }

    public void load_weights(String fname) {
	boolean first = true;
	System.out.println("Loading weights from " + fname);
	try {
	    BufferedReader fh = new BufferedReader(new FileReader(fname));
            String line;
            while ((line = fh.readLine()) != null) {
		if (first) {
		    first = false;
		} else {
		    line = line.trim();
		    String[] swgts = line.split("\\s+");
		    double[] wgts = new double[swgts.length];
		    for (int i = 0; i < wgts.length; i++) {
			wgts[i] = Double.parseDouble(swgts[i]);			
		    }		    
		    System.out.println("Weights are: " + TERalignment.join(" ", wgts));
		    set_weights(wgts);
		    return;
		}
	    }
	} catch (IOException ioe) {
	    System.out.println("Loading weights in TERcost: " + ioe);
	    System.exit(-1);
	}
    }

    public void set_weights(double[] wgts) {
	if ((wgts == null) || (wgts.length < get_num_features())) {
	    int num_w = 0;
	    if (wgts != null) num_w = wgts.length;
	    System.err.println("Invalid number of weights being set: " + num_w + " (need " + get_num_features() + ")");
	    System.err.println("Aborting.");
	    System.exit(-1);
	}
	_match_cost = wgts[0];
	_insert_cost = wgts[1];
	_delete_cost = wgts[2];
	_substitute_cost = wgts[3];
	_stem_cost = wgts[4];
	_syn_cost = wgts[5];
	_shift_cost = wgts[6];	
	if (_phraseTable == null) return;
	_phraseTable.set_weights(7, wgts);
    }

    public void get_wgt_phrase(double[] wgt, 
			     Comparable[] hyp, int hstart, int hlen,
			     Comparable[] ref, int rstart, int rlen) {
	if (_phraseTable == null) return;
	_phraseTable.get_wgt_phrase(7, wgt, hyp, hstart, hlen, ref, rstart, rlen);
    }
				    
    // Return phrase table for phrasal substitutions
    // null value means we don't do phrasal subsitutions
    public void setPhraseTable(PhraseTable pt) {
	_phraseTable = pt;
    }

    public PhraseTable getPhraseTable() {
	return _phraseTable;
    }

    protected PhraseTable _phraseTable = null;

    public Comparable[] process_input_hyp(String[] in) { return process_input(in); }
    public Comparable[] process_input_ref(String[] in) { return process_input(in); }
    protected Comparable[] process_input(String[] in) { return (Comparable[]) in; }

    public double _shift_cost = 1.0;
    public double _insert_cost = 1.0;
    public double _delete_cost = 1.0;
    public double _substitute_cost = 1.0;
    public double _match_cost = 0.0;
    public double _stem_cost = 1.0;
    public double _syn_cost = 1.0;

    public String get_info() {
	String s = (COST_NAME + "\n" +
		    " Match Cost:      " + this._match_cost + "\n" +
		    " Insertion Cost:  " + this._insert_cost + "\n" +
		    " Delete Cost:     " + this._delete_cost + "\n" +
		    " Substitute Cost: " + this._substitute_cost + "\n" +
		    " Stem Cost:       " + this._stem_cost + "\n" +
		    " Synonym Cost:    " + this._syn_cost + "\n" +
		    " Shift Cost:      " + this._shift_cost + "\n");
	if (_phraseTable != null) s += _phraseTable.get_info();
	return s;
    }
}
