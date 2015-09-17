package com.bbn.mt.terp;

/*

Copyright 2008 by BBN Technologies and University of Maryland (UMD)

BBN and UMD grant a nonexclusive, source code, royalty-free right to
use this Software known as Translation Error Rate Plus COMpute (the
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

Matthew Snover (snover@cs.umd.edu)                           
*/

import java.lang.Math;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/* Storage Class for TER alignments */
public class TERalignment {
    public TERalignment() {}
    public TERalignment(TERcost costfunc) {this.costfunc = costfunc;}

    public TERalignment(TERalignment ot) {
	this.ref = ot.ref;
	this.hyp = ot.hyp;
	this.orig_ref = ot.orig_ref;
	this.orig_hyp = ot.orig_hyp;
	this.aftershift = ot.aftershift;
	this.allshifts = ot.allshifts;
	this.numEdits = ot.numEdits;
	this.numWords = ot.numWords;
	this.alignment = ot.alignment;
	this.alignment_h = ot.alignment_h;
	this.alignment_r = ot.alignment_r;
	this.alignment_cost = ot.alignment_cost;
	this.bestRef = ot.bestRef;
	this.numPara = ot.numPara;
	this.numIns = ot.numIns;
	this.numDel = ot.numDel;
	this.numSub = ot.numSub;
	this.numStem = ot.numStem;
	this.numSyn = ot.numSyn;
	this.numSft = ot.numSft;
	this.numWsf = ot.numWsf;	
	this.numMatch = ot.numMatch;
	this.costfunc = ot.costfunc;
    }

  public String toString() {
      String s = "";
      if (orig_ref != null) s += "Original Reference: " + orig_ref + "\n";
      if (orig_hyp != null) s += "Original Hypothesis: " + orig_hyp + "\n";

      s += "Reference: " + join(" ", ref) + 
	  "\nHypothesis: " + join(" ", hyp) + 
	  "\nHypothesis After Shift: " + join(" ", aftershift);
      
      if (alignment != null) {
	  s += "\nAlignment: (";
	  for (int i = 0; i < alignment.length; i++) {s+=alignment[i];}
	  s += ")";
      }
      if (allshifts == null) {
	  s += "\nNumShifts: 0";
      } else {
	  s += "\nNumShifts: " + allshifts.length;
	  for (int i = 0; i < allshifts.length; i++) {
	      s += "\n  " + allshifts[i];
	  }
      }
      
      s += "\nScore: " + this.score() +
	  " (" + this.numEdits + "/" + this.numWords + ")";
      return s;
  }

  public String toPhraseString() {
      String s = "";
      if (orig_ref != null) s += "Original Reference: " + orig_ref + "\n";
      if (orig_hyp != null) s += "Original Hypothesis: " + orig_hyp + "\n";

      s += "Reference: " + join(" ", ref) + 
	  "\nHypothesis: " + join(" ", hyp) + 
	  "\nHypothesis After Shift: " + join(" ", aftershift);
      
      if (alignment != null) {
	  s += "\nAlignment: (";
	  for (int i = 0; i < alignment.length; i++) {s+=alignment[i];}
	  s += ")";
      }

      s += String.format("\nHypErrs: %s\nOtherErr: %.3f\nHypLocMap: %s\n", 
			 join(" ", "%.3f", getHypEdits()),
			 getHypOthEdits(),
			 join(" ", getHypLocMap()));

      if (allshifts == null) {
	  s += "\nNumShifts: 0";
      } else {
	  s += "\nNumShifts: " + allshifts.length;
	  for (int i = 0; i < allshifts.length; i++) {
	      s += "\n  " + allshifts[i];
	  }
      }
      
      String[] phrases = getPhraseStrings();
      int numphrase = 0;
      if (phrases != null) numphrase = phrases.length;
      s += "\nNum Phrase Substitutions: "+numphrase+"\n";
      if (phrases != null) {
	  for  (int i = 0; i < phrases.length; i++) {
	      s += "  " + phrases[i] + "\n";
	  }
      }

      s += String.format("\nScore: %.3f (%.3f / %.3f)", this.score(), this.numEdits, this.numWords);
      return s;
  }

    public String[][] getAlignmentStrings() {
	if (alignment == null) return null;
	int msize = 0;
	for (int i = 0; i < alignment.length; i++) {
	    if (alignment_r[i] > alignment_h[i]) {
		msize += alignment_r[i];
	    } else {
		msize += alignment_h[i];
	    }
	}

	String[][] tr = new String[3][msize];
	for (int j = 0; j < 3; j++) {
	    for (int i = 0; i < msize; i++) {
		tr[j][i] = ""; 
	    }
	}

	int hind = 0;
	int rind = 0;
	int ind = 0;
	for (int i = 0; i < alignment.length; i++) {
	    tr[2][ind] = Character.toString(alignment[i]);
	    for (int j = 0; j < alignment_r[i]; j++)
		tr[0][ind+j] = ref[rind+j].toString();		
	    for (int j = 0; j < alignment_h[i]; j++)
		tr[1][ind+j] = aftershift[hind+j].toString();		
	    hind += alignment_h[i];
	    rind += alignment_r[i];
	    ind += ((alignment_r[i] > alignment_h[i]) ? alignment_r[i] : alignment_h[i]);
	}
	return tr;
    }

    public static class AlignDetail {
	public AlignDetail(int hyp_ind_, int ref_ind_, String hyp_word_, String ref_word_,
			   int num_shifts_,
			   String edit_, double edit_cost_, int edit_num_) {
	    hyp_ind = hyp_ind_; ref_ind = ref_ind_;
	    hyp_word = hyp_word_; ref_word = ref_word_;
	    num_shifts = num_shifts_;
	    edit = edit_; edit_cost = edit_cost_;
	    edit_num = edit_num_;
	}
	public int hyp_ind = -1;
	public int ref_ind = -1;
	public String hyp_word = "";
	public String ref_word = "";
	public int num_shifts = 0;
	public String edit = "";
	public double edit_cost = 0;
	public int edit_num = -1;
    }

    public AlignDetail[] getAlignDetail() {
	if (alignment == null) return null;

	int asize = 0;
	for (int i = 0; i < alignment.length; i++) {
	    if (alignment_r[i] == 0) { 
		asize += alignment_h[i]; 
	    } else if (alignment_h[i] == 0) { 
		asize += alignment_r[i]; 
	    } else {
		asize += (alignment_h[i] * alignment_r[i]);
	    }
	}

	AlignDetail[] tr = new AlignDetail[asize];
	int[] locmap = getHypLocMap();
	int[] shift_count = getHypShiftCount();
	int hind = 0;
	int rind = 0;
	int ind = 0;

	for (int i = 0; i < alignment.length; i++) {
	    double edit_cost = alignment_cost[i];
	    String edit_name = Character.toString(alignment[i]);
	    if (alignment[i] == ' ') { edit_name = "M"; }

	    if (alignment_r[i] == 0) {
		for (int h = 0; h < alignment_h[i]; h++) {
		    int hyp_index = hind+h;
		    tr[ind++] = new AlignDetail(locmap[hyp_index], -1, aftershift[hyp_index].toString(), "", 
						shift_count[hyp_index],
						edit_name, edit_cost, i);
		}
	    } else if (alignment_h[i] == 0) {
		for (int r = 0; r < alignment_r[i]; r++) {
		    int ref_index = rind+r;
		    tr[ind++] = new AlignDetail(-1, ref_index, "", ref[ref_index].toString(), 0,
						edit_name, edit_cost, i);
		}
	    } else {		
		for (int r = 0; r < alignment_r[i]; r++) {
		    int ref_index = rind+r;
		    for (int h = 0; h < alignment_h[i]; h++) {
			int hyp_index = hind+h;
			tr[ind++] = new AlignDetail(locmap[hyp_index], ref_index, 
						    aftershift[hyp_index].toString(), ref[ref_index].toString(), 
						    shift_count[hyp_index],
						    edit_name, edit_cost, i);
		    }
		}
	    }
	    hind += alignment_h[i];
	    rind += alignment_r[i];
	}
	return tr;
    }


    public String[][] getAlignmentStringsHtml() {
	if (alignment == null) return null;
	int msize = alignment.length;
// 	int msize = 0;
// 	for (int i = 0; i < alignment.length; i++) {
// 	    if (alignment_r[i] > alignment_h[i]) {
// 		msize += alignment_r[i];
// 	    } else {
// 		msize += alignment_h[i];
// 	    }
// 	}

	String[][] tr = new String[3][msize];
	for (int j = 0; j < 3; j++) {
	    for (int i = 0; i < msize; i++) {
		tr[j][i] = ""; 
	    }
	}

	int hind = 0;
	int rind = 0;
	int ind = 0;
	for (int i = 0; i < alignment.length; i++) {
	    tr[0][i] = costfunc.to_out_html(ref, rind, alignment_r[i]);
	    tr[1][i] = costfunc.to_out_html(aftershift, hind, alignment_h[i]);
	    tr[2][i] = Character.toString(alignment[i]);
	    hind += alignment_h[i];
	    rind += alignment_r[i];
	}
	return tr;
    }

    public String[][] getSplitPhraseStrings() {
	if(alignment != null) {
	    int hind = 0;
	    int rind = 0;
	    int numphrase = 0;
	    for(int i = 0; i < alignment.length; i++) {
		if (alignment[i] == 'P') numphrase++;
	    }
	    String[][] tr = new String[numphrase][4];

	    int pcind = 0;
	    ArrayList tmpList = new ArrayList(aftershift.length);
	    for(int i = 0; i < alignment.length; i++) {		
		if (alignment[i] == 'P') {
		    PhraseTree.phrase_cost pc = 
			costfunc.getPhraseTable().retrieve_exact(ref, rind, alignment_r[i],
								 aftershift, hind, alignment_h[i]);
		    for (int j = 0; j < alignment_r[i]; j++) {
			tmpList.add(ref[rind+j]);
		    }
		    String ref_ph = join(" ", tmpList);
		    tmpList.clear();
		    
		    for (int j = 0; j < alignment_h[i]; j++) {
			tmpList.add(aftershift[hind+j]);
		    }
		    String hyp_ph = join(" ", tmpList);
		    tmpList.clear();
		    
		    tr[pcind][0] = "" + pc.cost;
		    tr[pcind][1] = "" + pc.orig_cost;
		    tr[pcind][2] = ref_ph;
		    tr[pcind][3] = hyp_ph;
		    pcind++;
		}
		hind += alignment_h[i];
		rind += alignment_r[i];
	    }
	    return tr;
	}
	return null;
    }

    public String[] getPhraseStrings() {
	String[][] splitstr = getSplitPhraseStrings();
	if (splitstr == null) return null;
	String[] tr = new String[splitstr.length];
	for (int i = 0; i < tr.length; i++) {
	    tr[i] = "NewCost: " + splitstr[i][0] + " OrigCost: " + 
		splitstr[i][1] + " <p>" + splitstr[i][2] + "</p> <p>" + 
		splitstr[i][3] + "</p>";
	}
	return tr;
    }

  public String toMoreString() {
	String s = "Best Ref: " + join(" ", ref) + 
	    "\nOrig Hyp: " + join(" ", hyp) + "\n";
	s += getPraStr(ref, aftershift, alignment, allshifts, false);
	s += String.format("TER Score: %1$6.2f (%2$5.1f/%3$5.1f)\n", 100*this.score(),
			   this.numEdits, this.numWords);
	s += prtShift(ref, allshifts);
	return s;
  }

  public String toDotString() {      
      //      String s = "Best Ref: " + join(" ", ref) + 
      //	  "\nOrig Hyp: " + join(" ", hyp) + "\n";
      String s = "";
      for (int i = 0; i < hyp.length; i++) {
	  s += "nodeOH" + i + "[label = \"" + hyp[i] + "\"];\n";
	  if (i > 0) {
	      s += "nodeOH" + (i-1) + " -> nodeOH" + i + " [rankdir=\"LR\",shape=\"none\"];\n";
	  }
      }
      for (int i = 0; i < aftershift.length; i++) {
	  s += "nodeAS" + i + "[label = \"" + aftershift[i] + "\"];\n";
	  if (i > 0) {
	      s += "nodeAS" + (i-1) + " -> nodeRF" + i + " [rankdir=\"LR\",shape=\"none\"];\n";
	  }
      }
      for (int i = 0; i < ref.length; i++) {
	  s += "nodeRF" + i + "[label = \"" + ref[i] + "\"];\n";
	  if (i > 0) {
	      s += "nodeRF" + (i-1) + " -> nodeRF" + i + " [rankdir=\"LR\",shape=\"none\"];\n";
	  }
      }
      String sc =  String.format("TER Score: %1$6.2f (%2$5.1f/%3$5.1f)\n", 100*this.score(),
				 this.numEdits, this.numWords);
      s += "nodeScore [shape=\"rect\",label=\"" + sc + "\"];\n";
      return s;
  }

  public double score() { 
      if ((numWords <= 0.0) && (this.numEdits > 0.0)) return 1.0; 
      if (numWords <= 0.0) return 0.0; 
      if (((Boolean) TERpara.para().get(TERpara.OPTIONS.CAP_TER)) && (numEdits > numWords)) return 1.0;
      return (double) numEdits / numWords;
  }    

  public static String join(String delim, Object[] arr) {
      if (arr == null) return "";
      if (delim == null) delim = new String("");
      String s = new String("");
      for (int i = 0; i < arr.length; i++) {
	  if (i == 0) { s += arr[i]; }
	  else { s += delim + arr[i]; }
      }
      return s;
  }

  public static String join(String delim, double[] arr) {
      if (arr == null) return "";
      if (delim == null) delim = new String("");
      String s = new String("");
      for (int i = 0; i < arr.length; i++) {
	  if (i == 0) { s += arr[i]; }
	  else { s += delim + arr[i]; }
      }
      return s;
  }

  public static String join(String delim, String format, double[] arr) {
      if (arr == null) return "";
      if (delim == null) delim = new String("");
      String s = new String("");
      for (int i = 0; i < arr.length; i++) {
	  if (i == 0) { s += String.format(format, arr[i]); }
	  else { s += delim + String.format(format, arr[i]); }
      }
      return s;
  }

  public static String join(String delim, int[] arr) {
      if (arr == null) return "";
      if (delim == null) delim = new String("");
      String s = new String("");
      for (int i = 0; i < arr.length; i++) {
	  if (i == 0) { s += arr[i]; }
	  else { s += delim + arr[i]; }
      }
      return s;
  }


  public static String join(String delim, List arr) {
      if (arr == null) return "";
      if (delim == null) delim = new String("");
      String s = new String("");
      for (int i = 0; i < arr.size(); i++) {
	  if (i == 0) { s += arr.get(i); }
	  else { s += delim + arr.get(i); }
      }
      return s;
  }
    
  public static String join(String delim, char[] arr) {
	if (arr == null) return "";
	if (delim == null) delim = new String("");
	String s = new String("");
	for (int i = 0; i < arr.length; i++) {
      if (i == 0) { s += arr[i]; }
      else { s += delim + arr[i]; }
	}
	return s;
  }

  public void scoreDetails() {
      numSyn = numStem = numPara = numIns = numDel = numSub = numWsf = numSft = 0;
      if(allshifts != null) {
	  for(int i = 0; i < allshifts.length; ++i)
	      numWsf += allshifts[i].size();
	  numSft = allshifts.length;
      }
      
      if(alignment != null) {
	  for(int i = 0; i < alignment.length; ++i) {
	      switch (alignment[i]) {
	      case 'P':
		  numPara++;
		  break;
	      case ' ':
		  numMatch++;
		  break;
	      case 'Y':
		  numSyn++;
		  break;
	      case 'T':
		  numStem++;
		  break;
	      case 'S': 
		  numSub++;
		  break;
	      case 'D':
		  numDel++;
		  break;
	      case 'I':
		  numIns++;
		  break;
	      }		
	  }
      }
      //	if(numEdits != numSft + numDel + numIns + numSub)
      //      System.out.println("** Error, unmatch edit erros " + numEdits + 
      //                         " vs " + (numSft + numDel + numIns + numSub));
  }

    // Use the current alignment and rescore it.
    public double rescore_alignment() {
	return rescore_alignment(this.costfunc);
    }

    public double rescore_alignment(TERcost costfunc) {
	if (costfunc == null) return Double.POSITIVE_INFINITY;
	double numEdits = 0.0;
	if (allshifts != null) {
	    for (int i = 0; i < allshifts.length; i++) {
		numEdits += costfunc.shift_cost(allshifts[i]);
	    }
	}
	PhraseTable pt = costfunc.getPhraseTable();

	if(alignment != null) {
	    int hind = 0;
	    int rind = 0;
	    for(int i = 0; i < alignment.length; i++) {		
		switch (alignment[i]) {
		case ' ': 
		    alignment_cost[i] = costfunc.match_cost(aftershift[hind], ref[rind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'S': 
		    alignment_cost[i] = costfunc.substitute_cost(aftershift[hind], ref[rind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'Y': 
		    alignment_cost[i] = costfunc.syn_cost(aftershift[hind], ref[rind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'T':
		    alignment_cost[i] = costfunc.stem_cost(aftershift[hind], ref[rind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'I':
		    alignment_cost[i] = costfunc.insert_cost(aftershift[hind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'D':
		    alignment_cost[i] = costfunc.delete_cost(ref[rind]);
		    numEdits += alignment_cost[i];
		    break;
		case 'P':
		    if (pt == null) {
			alignment_cost[i] = Double.POSITIVE_INFINITY;
		    } else {
			PhraseTree.phrase_cost pc = pt.retrieve_exact(ref, rind, 
								       alignment_r[i],
								       aftershift, hind, 
								       alignment_h[i]);
			alignment_cost[i] = pc.cost;
		    }
		    numEdits += alignment_cost[i];
		    break;
		}
		hind += alignment_h[i];
		rind += alignment_r[i];
	    }
	}
	this.numEdits = numEdits;
	this.hyp_loc_map = null;
	this.ref_only_err = 0.0;
	this.hyp_err = null;

	return this.score();
    }

    public double[] current_weights() {
	return costfunc.current_weights();
    }

    public double[] norm_weight_vector() {
	double[] tr = weight_vector();
	for (int i = 0; i < tr.length; i++) {
	    if (numWords > 0)
		tr[i] /= numWords;
	    else
		tr[i] = 0.0;
	}
	return tr;
    }

    public double[] weight_vector() {
	int num_features = costfunc.get_num_features();	
	if (num_features < 0) return null;
	double[] tr = new double[num_features];
	for (int i = 0; i < num_features; i++) {
	    tr[i] = 0.0;
	}

	if (allshifts != null) {
	    for (int i = 0; i < allshifts.length; i++) {
		costfunc.get_wgt_shift(tr, allshifts[i]);
	    }
	}
	if(alignment != null) {
	    int hind = 0;
	    int rind = 0;
	    for(int i = 0; i < alignment.length; i++) {		
		switch (alignment[i]) {
		case ' ': case 'S': case 'Y': case 'T':
		    costfunc.get_wgt_edit(tr, alignment[i], aftershift[hind], 
					  ref[rind]);
		    break;
		case 'I':
		    costfunc.get_wgt_edit(tr, alignment[i], aftershift[hind], null);
		    break;
		case 'D':
		    costfunc.get_wgt_edit(tr, alignment[i], null, ref[rind]);
		    break;
		case 'P':
		    costfunc.get_wgt_phrase(tr, aftershift, hind, alignment_h[i],
					  ref, rind, alignment_r[i]);		    
		    break;
		}
		hind += alignment_h[i];
		rind += alignment_r[i];
	    }
	}
	return tr;
    }
    
    public static void performShiftArray(HashMap hwords,
					 int start,
					 int end,
					 int moveto,
					 int capacity) {
	HashMap nhwords = new HashMap();
	
	if(moveto == -1) {
	    copyHashWords(hwords, nhwords, start, end, 0);
	    copyHashWords(hwords, nhwords, 0, start - 1, end - start + 1);
	    copyHashWords(hwords, nhwords, end + 1, capacity, end + 1);	    
	} else if (moveto < start) {
	    copyHashWords(hwords, nhwords, 0, moveto, 0);
	    copyHashWords(hwords, nhwords, start, end, moveto + 1);
	    copyHashWords(hwords, nhwords, moveto + 1, start - 1, end - start + moveto + 2);
	    copyHashWords(hwords, nhwords, end + 1, capacity, end + 1);
	} else if (moveto > end) {
	    copyHashWords(hwords, nhwords, 0, start - 1, 0);
	    copyHashWords(hwords, nhwords, end + 1, moveto, start);
	    copyHashWords(hwords, nhwords, start, end, start + moveto - end);
	    copyHashWords(hwords, nhwords, moveto + 1, capacity, moveto + 1);
	} else {
	    copyHashWords(hwords, nhwords, 0, start - 1, 0);
	    copyHashWords(hwords, nhwords, end + 1, end + moveto - start, start);
	    copyHashWords(hwords, nhwords, start, end, moveto);
	    copyHashWords(hwords, nhwords, end + moveto - start + 1, capacity, end + moveto - start + 1);
	}
	hwords.clear();
	hwords.putAll(nhwords);
    }

  private String prtShift(Comparable[] ref,
                          TERshift[] allshifts) {
	String to_return = "";
	int ostart, oend, odest;
	int nstart, nend;
	int dist;
	String direction = "";
	
	if(allshifts != null) {
	    for(int i = 0; i < allshifts.length; ++i) {
		TERshift[] oneshift = new TERshift[1];
		ostart = allshifts[i].start;
		oend = allshifts[i].end;
		odest = allshifts[i].newloc;
		
		if(odest >= oend) {
		    // right
		    nstart = odest + 1 - allshifts[i].size();
		    nend = nstart + allshifts[i].size() - 1;
		    dist = odest - oend;
		    direction = "right";
		} else {
		    // left
		    nstart = odest + 1;
		    nend = nstart + allshifts[i].size() - 1;
		    dist = ostart - odest -1;
		    direction = "left";
		}
		
		to_return += "\nShift " + allshifts[i].shifted + " " + dist + " words " + direction;
		oneshift[0] = new TERshift(ostart, oend, allshifts[i].moveto, odest);
		to_return += getPraStr(ref, allshifts[i].aftershift, allshifts[i].alignment, oneshift, true); 
	    }
	    to_return += "\n";
	}
	return to_return;
  }

    private String getPraStr(Comparable[] ref,
			     Comparable[] aftershift,
			     char[] alignment,
			     TERshift[] allshifts,
			     boolean shiftonly) {
	String to_return = "";
	String rstr = "";
	String hstr = "";
	String estr = "";
	String sstr = "";
	HashMap align_info = new HashMap();
	ArrayList shift_dists = new ArrayList();
	int anum = 1;
	int ind_start = 0;
	int ind_end = 1;
	int ind_from = 2;
	int ind_in = 3;
	int ostart, oend, odest;
	int slen = 0;
	int nstart, nend, nfrom, dist;
	int non_inserr = 0;

	if(allshifts != null) {
	    for(int i = 0; i < allshifts.length; ++i) {
		ostart = allshifts[i].start;
		oend = allshifts[i].end;
		odest = allshifts[i].newloc;
		slen = allshifts[i].size();

		if(odest >= oend) {
		    // right
		    nstart = odest + 1 - slen;
		    nend = nstart + slen - 1;
		    nfrom = ostart;
		    dist = odest - oend;
		} else {
		    // left
		    nstart = odest + 1;
		    nend = nstart + slen - 1;
		    nfrom = ostart + slen;
		    dist = (ostart - odest -1) * -1;
		}
	
		//dist = (allshifts[i].leftShift())?-1*allshifts[i].distance():allshifts[i].distance();
		shift_dists.add(dist);
		//		System.out.println("[" + hyp[ostart] + ".." + hyp[oend] + " are shifted " + dist);

		if(anum > 1) performShiftArray(align_info, ostart, oend, odest, alignment.length);

		Object val = align_info.get(nstart + "-" + ind_start);
		if(val == null) {
          ArrayList al = new ArrayList();
          al.add(anum);
          align_info.put(nstart + "-" + ind_start, al);
		} else {
          ArrayList al = (ArrayList) val;
          al.add(anum);
		}
		
		val = align_info.get(nend + "-" + ind_end);
		if(val == null) {
          ArrayList al = new ArrayList();
          al.add(anum);
          align_info.put(nend + "-" + ind_end, al);
		} else {
          ArrayList al = (ArrayList) val;
          al.add(anum);
		}
		
		val = align_info.get(nfrom + "-" + ind_from);
		if(val == null) {
          ArrayList al = new ArrayList();
          al.add(anum);
          align_info.put(nfrom + "-" + ind_from, al);
		} else {
          ArrayList al = (ArrayList) val;
          al.add(anum);
		}

		/*
          val = align_info.get("60-"+ind_start);
          if(val != null)
          System.out.println(((ArrayList) val).get(0));
          else
          System.out.println("empty");

          System.out.println("nstart: " + nstart + ", nend:" + nend + "," + ostart +"," + oend +","+ odest + "," + align_info.size());
		*/
		if(slen > 0) {
          for(int j = nstart; j <= nend; ++j) {
			val = align_info.get(j + "-" + ind_in);
			if(val == null) {
              ArrayList al = new ArrayList();
              al.add(anum);
              align_info.put(j + "-" + ind_in, al);
			} else {
              ArrayList al = (ArrayList) val;
              al.add(anum);
			}
          }
		}
		anum++;
      }
	}

	int hyp_idx = 0;
	int ref_idx = 0;
	Object val = null;
	if(alignment != null) {
      for(int i = 0; i < alignment.length; ++i) {
	  String shift_in_str = "";
	  String ref_wd = (ref_idx < ref.length)?String.valueOf(ref[ref_idx]):"";
	  String hyp_wd = (hyp_idx < hyp.length)?String.valueOf(aftershift[hyp_idx]):"";
	  int l = 0;
	  
	  if(alignment[i] != 'D') {
	      val = align_info.get(hyp_idx + "-" + ind_from);
	      if(val != null) {
		  //						System.out.println("hyp_idx: " + hyp_idx + "," + hyp_wd);
		  ArrayList list = (ArrayList) val;
		  for(int j = 0; j < list.size(); ++j) {
		      String s = "" + list.get(j);
		      hstr += " @";
		      rstr += "  ";
		      estr += "  ";
		      sstr += " " + s;
		      for(int k = 1; k < s.length(); ++k) {
			  hstr += " ";
			  rstr += " ";
			  estr += " ";
		      }
		  }
	      }
	      
	      val = align_info.get(hyp_idx + "-" + ind_start);
	      if(val != null) {
		  //			System.out.println("hyp_idx: " + hyp_idx + "," + hyp_wd + "," + alignment.length);
		  ArrayList list = (ArrayList) val;
		  for(int j = 0; j < list.size(); ++j) {
		      String s = "" + list.get(j);
		      hstr += " [";
		      rstr += "  ";
		      estr += "  ";
		      sstr += " " + s;
		      for(int k = 1; k < s.length(); ++k) {
			  hstr += " ";
			  rstr += " ";
			  estr += " ";
		      }
		  }
	      }
	      if(slen > 0) {
		  val = align_info.get(hyp_idx + "-" + ind_in);
		  if(val != null)
		      shift_in_str = TERsgml.join(",", (ArrayList) val);
		  //	if(val != null) System.out.println("shiftstr: " + ref_idx + "," + hyp_idx + "-" + ind_in + ":" + shift_in_str);
	      } 
	  }
	  switch (alignment[i]) {
          case ' ':
	      l = Math.max(ref_wd.length(), hyp_wd.length());
	      hstr += " " + hyp_wd;
	      rstr += " " + ref_wd;
	      estr += " ";
	      sstr += " ";
	      for(int j = 0; j < l; ++j) {
		  if(hyp_wd.length() <= j) hstr += " ";
		  if(ref_wd.length() <= j) rstr += " ";
		  estr += " ";
		  sstr += " ";
	      }
	      hyp_idx++;
	      ref_idx++;
	      non_inserr++;
	      break;
          case 'S': case 'Y': case 'T':
	      l = Math.max(ref_wd.length(), Math.max(hyp_wd.length(), Math.max(1, shift_in_str.length())));
	      hstr += " " + hyp_wd;
	      rstr += " " + ref_wd;
	      if(hyp_wd.equals("") || ref_wd.equals("")) 
		  System.out.println("unexpected empty: sym="+ alignment[i]  + 
				     " hyp_wd=" + hyp_wd + 
				     " ref_wd=" + ref_wd + 
				     " i=" + i + " alignment=" + join(",", alignment));
	      estr += " " + alignment[i];
	      sstr += " " + shift_in_str;
	      for(int j = 0; j < l; ++j) {
		  if(hyp_wd.length() <= j) hstr += " ";
		  if(ref_wd.length() <= j) rstr += " ";
		  if(j > 0) estr += " ";
		  if(j >= shift_in_str.length()) sstr += " ";
	      }
	      ref_idx++;
	      hyp_idx++;
	      non_inserr++;
	      break;
	  case 'P':
	      int min = alignment_r[i];
	      if (alignment_h[i] < min) min = alignment_h[i];
	      for (int k = 0; k < min; k++) {
		  ref_wd = (ref_idx < ref.length)?String.valueOf(ref[ref_idx]):"";
		  hyp_wd = (hyp_idx < hyp.length)?String.valueOf(aftershift[hyp_idx]):"";    
		  //		  System.out.println("Saying that " + ref_wd + " & " + hyp_wd + " are P. " + alignment_r[i] + " " +
		  //				     alignment_h[i]);
		  l = Math.max(ref_wd.length(), Math.max(hyp_wd.length(), Math.max(1, shift_in_str.length())));
		  hstr += " " + hyp_wd;
		  rstr += " " + ref_wd;
		  if(hyp_wd.equals("") || ref_wd.equals("")) 		  
		      System.out.println("unexpected empty: sym="+ alignment[i]  + 
					 " hyp_wd=" + hyp_wd + 
					 " ref_wd=" + ref_wd + 
					 " i=" + i + " alignment=" + join(",", alignment));
		  estr += " " + alignment[i];
		  sstr += " " + shift_in_str;
		  for(int j = 0; j < l; ++j) {
		      if(hyp_wd.length() <= j) hstr += " ";
		      if(ref_wd.length() <= j) rstr += " ";
		      if(j > 0) estr += " ";
		      if(j >= shift_in_str.length()) sstr += " ";
		  }
		  ref_idx++;
		  hyp_idx++;
		  non_inserr++;
	      }
	      if (alignment_h[i] > alignment_r[i]) {
		  for (int k = alignment_r[i]; 
		       k < alignment_h[i]; 
		       k++) {
		      ref_wd = (ref_idx < ref.length)?String.valueOf(ref[ref_idx]):"";
		      hyp_wd = (hyp_idx < hyp.length)?String.valueOf(aftershift[hyp_idx]):"";    
		      l = Math.max(hyp_wd.length(), shift_in_str.length());
		      hstr += " " + hyp_wd;
		      rstr += " ";
		      estr += " P";
		      sstr += " " + shift_in_str;
		      for(int j = 0; j < l; ++j) {
			  rstr += "*"; 
			  if(j >= hyp_wd.length()) hstr += " ";
			  if(j > 0) estr += " ";
			  if(j >= shift_in_str.length()) sstr += " ";
		      }
		      hyp_idx++;
		  }
	      } else if (alignment_r[i] > alignment_h[i]) {
		  for (int k = alignment_h[i]; 
		       k < alignment_r[i]; 
		       k++) {
		      ref_wd = (ref_idx < ref.length)?String.valueOf(ref[ref_idx]):"";
		      hyp_wd = (hyp_idx < hyp.length)?String.valueOf(aftershift[hyp_idx]):"";    
		      l = ref_wd.length();
		      hstr += " ";
		      rstr += " " + ref_wd;
		      estr += " P";
		      sstr += " ";
		      for(int j = 0; j < l; ++j) {
			  hstr += "*"; 
			  if(j > 0) estr += " ";
			  sstr += " ";
		      }
		      ref_idx++;
		      non_inserr++;
		  }
	      }
	      break;
	  case 'D':
	      l = ref_wd.length();
	      hstr += " ";
	      rstr += " " + ref_wd;
	      estr += " D";
	      sstr += " ";
	      for(int j = 0; j < l; ++j) {
		  hstr += "*"; 
		  if(j > 0) estr += " ";
		  sstr += " ";
	      }
	      ref_idx += alignment_r[i];
	      hyp_idx += alignment_h[i];
	      non_inserr++;
	      break;
	  case 'I':
	      l = Math.max(hyp_wd.length(), shift_in_str.length());
	      hstr += " " + hyp_wd;
	      rstr += " ";
	      estr += " I";
	      sstr += " " + shift_in_str;
	      for(int j = 0; j < l; ++j) {
		  rstr += "*"; 
		  if(j >= hyp_wd.length()) hstr += " ";
		  if(j > 0) estr += " ";
		  if(j >= shift_in_str.length()) sstr += " ";
	      }
	      hyp_idx++;
	      break;
	  }
	  
	  if(alignment[i] != 'D') {
	      val = align_info.get((hyp_idx-1) + "-" + ind_end);
	      if(val != null) {
		  ArrayList list = (ArrayList) val;
		  for(int j = 0; j < list.size(); ++j) {
		      String s = "" + list.get(j);
		      hstr += " ]";
		      rstr += "  ";
		      estr += "  ";
		      sstr += " " + s;
		      for(int k = 1; k < s.length(); ++k) {
			  hstr += " ";
			  rstr += " ";
			  estr += " ";
		      }
		  }
	      }		    
	  }
      }
	}
      //	if(non_inserr != ref.length && ref.length > 1)
      //      System.out.println("** Error, unmatch non-insertion erros " + non_inserr + 
      //                         " and reference length " + ref.length );	
    String indent = "";
    if (shiftonly) indent = " ";
	to_return += "\n" + indent + "REF: " + rstr;
	to_return += "\n" + indent + "HYP: " + hstr;
    if(!shiftonly) {
      to_return += "\n" + indent + "EVAL:" + estr;
      to_return += "\n" + indent + "SHFT:" + sstr;
    }
	to_return += "\n";
	return to_return;
  }

  private static void copyHashWords(HashMap ohwords,
                                    HashMap nhwords,
                                    int start,
                                    int end,
                                    int nstart) {
	int ind_start = 0;
	int ind_end = 1;
	int ind_from = 2;
	int ind_in = 3;
	Object val = null;
	int k = nstart;

	for(int i = start; i <= end; ++k, ++i) {
      for(int j = ind_start; j <= ind_in; ++j) {
		val = ohwords.get(i + "-" + j);
		if(val != null) {
          ArrayList al = (ArrayList) val;		    
          nhwords.put(k + "-" + j, al);
		}
      }
	}
  }

    private void calc_hyp_err () {
	if ((this.hyp == null) || (this.hyp.length == 0)) {
	    this.hyp_err = new double[0];
	    this.ref_only_err = this.numEdits;
	    this.hyp_loc_map = new int[0];
	    this.hyp_shift_count = new int[0];
	    return;
	}

	double[] tr = new double[this.hyp.length];
	Integer[] loc = new Integer[this.hyp.length];
	int[] shift_count = new int[this.hyp.length];
	for (int i = 0; i < tr.length; i++) {
	    loc[i] = i;
	    tr[i] = 0.0;
	    shift_count[i] = 0;
	}	

	if (this.allshifts != null) {
	    for (int i = 0; i < this.allshifts.length; i++) {
		TERshift shift = this.allshifts[i];
		double delta = shift.cost / (1+ shift.end - shift.start);
		for (int j = shift.start; j <= shift.end; j++) {
		    tr[loc[j]] += delta;		
		    shift_count[loc[j]]++;
		}
		loc = (Integer[]) TERcalc.PerformShiftNS(loc, shift);
	    }
	}
	    
	double ref_ocost = 0.0;
	int cur_hpos = 0;
	for (int i = 0; i < alignment_h.length; i++) {
	    // if (alignment_r[i] == 0) hyp_ocost += alignment_cost[i];		
	    if (alignment_h[i] == 0) ref_ocost += alignment_cost[i];
	    
	    if ((alignment_h[i] > 0) && (alignment_cost != null)) {
		double delta = alignment_cost[i] / alignment_h[i];
		for (int j = 0; j < alignment_h[i]; j++)
		    tr[loc[cur_hpos++]] += delta;
	    }
	}
	this.hyp_loc_map = new int[loc.length];
	this.hyp_shift_count = new int[loc.length];
	for (int i = 0; i < loc.length; i++) {
	    this.hyp_loc_map[i] = loc[i];
	    this.hyp_shift_count[i] = shift_count[loc[i]];
	}
	this.hyp_err = tr;
	this.ref_only_err = ref_ocost;
    }

    public int[] getHypLocMap() {
	if (hyp_loc_map == null) {
	    calc_hyp_err();
	}
	return hyp_loc_map;
    }

    public double[] getHypEdits() {
	if (hyp_loc_map == null) {
	    calc_hyp_err();
	}
	return hyp_err;
    }

    public double getHypOthEdits() {
	if (hyp_loc_map == null) {
	    calc_hyp_err();
	}
	return ref_only_err;
    }

    public int[] getHypShiftCount() {
	if (hyp_loc_map == null) {
	    calc_hyp_err();
	}
	return hyp_shift_count;
    }


    private int[] hyp_loc_map = null;
    private double[] hyp_err = null;
    private double ref_only_err = 0.0;
    private int[] hyp_shift_count = null;

    private TERcost costfunc = null;
    
    public Comparable[] ref;
    public Comparable[] hyp;
    public Comparable[] aftershift;
    public String orig_ref = null;
    public String orig_hyp = null;
    
    public TERshift[] allshifts = null;
    
    public double numEdits = 0.0;
    public double numWords = 0.0;
    public char[] alignment = null;
    public double[] alignment_cost = null;
    public int[] alignment_h = null;
    public int[] alignment_r = null;

    public int bestRef = 0;
    
    public int numMatch = 0;
    public int numPara = 0;
    public int numIns = 0;
    public int numDel = 0;
    public int numSub = 0;
    public int numStem = 0;
    public int numSyn = 0;
    public int numSft = 0;
    public int numWsf = 0;
}
