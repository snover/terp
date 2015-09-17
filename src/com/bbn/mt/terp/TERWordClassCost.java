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
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;


public class TERWordClassCost extends TERcost {
  /* For all of these functions, the score should be between 0 and 1
   * (inclusive).  If it isn't, then it will break TERcalc! */

    static {
	COST_NAME = "Word Class TERcost";
    }

    TERWordClassCost() {
	nameMap.put(OTHER_NAME, 0);
	revNameMap.put(0, OTHER_NAME);
	_sub_cost[0][0] = default_substitute_cost;
	_match_cost[0] = default_match_cost;
	_delete_cost[0] = default_delete_cost;
	_insert_cost[0] = default_insert_cost;
	_shift_cost[0] = default_shift_cost;
	_stem_cost[0][0] = default_stem_cost;
	_syn_cost[0][0] = default_syn_cost;
    }

    TERWordClassCost(String fname) {
	nameMap.put(OTHER_NAME, 0);
	revNameMap.put(0, OTHER_NAME);
	_sub_cost[0][0] = default_substitute_cost;
	_match_cost[0] = default_match_cost;
	_delete_cost[0] = default_delete_cost;
	_insert_cost[0] = default_insert_cost;
	_shift_cost[0] = default_shift_cost;
	_stem_cost[0][0] = default_stem_cost;
	_syn_cost[0][0] = default_syn_cost;
	load_word_classes(fname);
    }

    TERWordClassCost(String fname, double dsub_cost, double dmat_cost,
		     double dins_cost, double ddel_cost, double dsht_cost,
		     double dstm_cost, double dsyn_cost) {
	default_substitute_cost = dsub_cost;
	default_match_cost = dmat_cost;
	default_insert_cost = dins_cost;
	default_delete_cost = ddel_cost;
	default_shift_cost = dsht_cost;
	default_stem_cost = dstm_cost;
	default_syn_cost = dsyn_cost;

	nameMap.put(OTHER_NAME, 0);
	revNameMap.put(0, OTHER_NAME);
	_sub_cost[0][0] = default_substitute_cost;
	_match_cost[0] = default_match_cost;
	_delete_cost[0] = default_delete_cost;
	_insert_cost[0] = default_insert_cost;
	_shift_cost[0] = default_shift_cost;
	_stem_cost[0][0] = default_stem_cost;
	_syn_cost[0][0] = default_syn_cost;
	load_word_classes(fname);
    }
    
    // Map class name to a number
    private HashMap nameMap = new HashMap();
    private HashMap revNameMap = new HashMap();
    private int numClasses = 1;

    // Map word to class
    private HashMap wordMap = new HashMap();;    

    private static final Pattern NUMBER_PAT1 = Pattern.compile("[0-9]");
    private static final Pattern NUMBER_PAT2 = Pattern.compile("^[-$.0-9]*$");
    private int NUMBER_CLASS = -1;
    private static final String NUMBER_STR = new String("[NUMBER]");

    private static final Pattern class_p = Pattern.compile("^CLASS\\s+(.*?)\\s+(.*?)\\s*$");
    private static final Pattern cost_p1 = Pattern.compile("^([IiDdMmHh])\\s+(.*?)\\s+([0-9.]+)\\s*$");
    private static final Pattern cost_p2 = Pattern.compile("^([SsTtYy])\\s+(.*?)\\s+(.*?)\\s+([0-9.]+)\\s*$");

    // OTHER is the default catch all word class.  Any word not
    // specified elsewhere is in this class.
    private static final int OTHER_CLASS = 0;
    private static final String OTHER_NAME = new String("OTHER");

    public static String OTHER_NAME() { return OTHER_NAME; }
    public static String NUMBER_STR() { return NUMBER_STR; }
    public String NUMBER_CLASS() { 
	if (NUMBER_CLASS < 0) return OTHER_NAME;
	return (String) revNameMap.get(NUMBER_CLASS);
    }

    public int get_num_features() {
	int my_num_feat = (4 * numClasses) + (3 * numClasses * numClasses);
	if (_phraseTable == null) return my_num_feat;
	return my_num_feat + _phraseTable.num_params();       
    }

    public void get_wgt_edit(double[] wgt, char type, Comparable hyp, Comparable ref) {
	switch (type) {
	case ' ': wgt[findClass(hyp)]++; break;
	case 'I': wgt[(1 * numClasses) + findClass(hyp)]++; break;
	case 'D': wgt[(2 * numClasses) + findClass(ref)]++; break;
	case 'S': wgt[(4 * numClasses) + (findClass(ref) * numClasses) + findClass(hyp)]++; break;
	case 'T': wgt[(4 * numClasses) + (numClasses * numClasses) + 
		      (findClass(ref) * numClasses) + findClass(hyp)]++; break;
	case 'Y': wgt[(4 * numClasses) + (2 * (numClasses * numClasses)) + 
		      (findClass(ref) * numClasses) + findClass(hyp)]++; break;
	}
	return;
    }

    public void get_wgt_shift(double[] wgt, TERshift shift) {
	wgt[(3 * numClasses) + findClass(shift)]++;
    }

    public double[] current_weights() {
	double[] tr = new double[get_num_features()];
	for (int i = 0; i < numClasses; i++) {
	    tr[i] = _match_cost[i];
	    tr[numClasses + i] = _insert_cost[i];
	    tr[(2*numClasses) + i] = _delete_cost[i];
	    tr[(3*numClasses) + i] = _shift_cost[i];
	    for (int j = 0; j < numClasses; j++) {
		tr[(4*numClasses) + (numClasses * i) + j] = _sub_cost[i][j];
		tr[(4*numClasses) + (numClasses * numClasses) + 
		   (numClasses * i) + j] = _stem_cost[i][j];
		tr[(4*numClasses) + (2 * numClasses * numClasses) + 
		   (numClasses * i) + j] = _syn_cost[i][j];
	    }
	}
	int my_num_feat = (4 * numClasses) + (3 * numClasses * numClasses);
	if (_phraseTable != null) {
	    _phraseTable.current_weights(my_num_feat, tr);
	}
	return tr;
    }

    public void set_weights(double[] wgts) {
	if ((wgts == null) || (wgts.length < get_num_features())) {
	    int num_w = 0;
	    if (wgts != null) num_w = wgts.length;
	    System.err.println("Invalid number of weights being set: " + num_w + " (need " + get_num_features() + ")");
	    System.err.println("Aborting.");
	    System.exit(-1);
	}
	for (int i = 0; i < numClasses; i++) {
	    _match_cost[i] = wgts[i];
	    _insert_cost[i] = wgts[numClasses+i];
	    _delete_cost[i] = wgts[(2*numClasses)+i];
	    _shift_cost[i] = wgts[(3*numClasses)+i];
	    for (int j = 0; j < numClasses; j++) {
		_sub_cost[i][j] = wgts[(4*numClasses) + (i*numClasses) + j];
		_stem_cost[i][j] = wgts[(4*numClasses) + (numClasses*numClasses) + 
					(i*numClasses) + j];
		_syn_cost[i][j] = wgts[(4*numClasses) + (2*numClasses*numClasses) + 
				       (i*numClasses) + j];
	    }
	}
	if (_phraseTable == null) return;
	int my_num_feat = (4 * numClasses) + (3 * numClasses * numClasses);
	_phraseTable.set_weights(my_num_feat, wgts);
    }

    public void get_wgt_phrase(double[] wgt, 
			       Comparable[] hyp, int hstart, int hlen,
			       Comparable[] ref, int rstart, int rlen) {
	if (_phraseTable == null) return;
	int my_num_feat = (4 * numClasses) + (3 * numClasses * numClasses);
	_phraseTable.get_wgt_phrase(my_num_feat, wgt, hyp, hstart, hlen, ref, rstart, rlen);
    }


    private double[][] _sub_cost = new double[1][1];
    private double[][] _stem_cost = new double[1][1];
    private double[][] _syn_cost = new double[1][1];
    private double[] _match_cost = new double[1];
    private double[] _shift_cost = new double[1];
    private double[] _insert_cost = new double[1];
    private double[] _delete_cost = new double[1];

    public String to_out_html(Comparable c) { 
	String cl = (String) revNameMap.get(findClass(c));
	return c.toString() + "<BR><font size=-1><i>" + cl + "</i></font>"; 	
    }
    public String to_out_html(Comparable[] c) { 
	return to_out_html(c, 0, c.length);
    }

    public String to_out_html(Comparable[] c, int s, int l) { 
	if (l == 0) return "";
	int fcl = findClass(c[s]);
	String cl = (String) revNameMap.get(fcl);
	if ((fcl == 0) || (l > 1)) {
	    cl = "";
	}
	String tr = "";
	for (int i = 0; i < l; i++) {
	    if (i == 0) {
		tr += c[i+s].toString();
	    } else {
		tr += " " + c[i+s].toString();
	    }
	}
	if (! cl.equals(""))
	    tr += "<BR><font size=-1><i>(" + cl + ")</i></font>";
	return tr;
    }

    private int findClass(Comparable s) {
	return _findClass(s);
	//	word_class wc = (word_class) s;
	//	return wc.wclass;
    }
    
    private int _findClass(Comparable s) {	
	int tr = -1;
	if (wordMap.containsKey(s)) {
	    tr = ((Integer) wordMap.get(s)).intValue();
	} else {
	    Integer iob = null;
	    Matcher np1 = NUMBER_PAT1.matcher((String) s);
	    Matcher np2 = NUMBER_PAT2.matcher((String) s);
	    if ((NUMBER_CLASS >= 0) && np1.matches() && np2.matches()) {
		iob = new Integer(NUMBER_CLASS);
		tr = NUMBER_CLASS;
	    } else {
		iob = new Integer(OTHER_CLASS);
		tr = OTHER_CLASS;	    
	    }
	    wordMap.put(s, iob);
	}
	return tr;
    }

    public void load_word_list(String fname, int classnum) {
	Integer class_i = new Integer(classnum);

	BufferedReader stream;
        try {
            stream = new BufferedReader(new FileReader(fname));
        } catch (IOException ioe) {
            System.out.println("load word class list: " + ioe);
	    System.exit(-1);
	    return;
        }

        try {
            String line;
            while ((line = stream.readLine()) != null) {
		line = line.trim();
                if (line.length() == 0) continue;
		if (line.equals(NUMBER_STR)) NUMBER_CLASS = classnum;
		else {
		    wordMap.put(line, classnum);
		}
	    }	    
        } catch(IOException ioe) {
            System.out.println("load word class list: " + ioe);
	    System.exit(-1);
	    return;
	}
	return;	
    }

    public void load_word_classes(String fname) {
	System.out.println("Loading word class information from " + fname);

	HashMap match_cost = new HashMap();
	HashMap delete_cost = new HashMap();
	HashMap insert_cost = new HashMap();
	HashMap shift_cost = new HashMap();
	HashMap sub_cost = new HashMap();
	HashMap syn_cost = new HashMap();
	HashMap stem_cost = new HashMap();

        BufferedReader stream;
        try {
            stream = new BufferedReader(new FileReader(fname));
        } catch (IOException ioe) {
            System.out.println("load word classes: (" + fname + ") " + ioe);
	    System.exit(-1);
            return;
        }

        try {
            String line;
            while ((line = stream.readLine()) != null) {
		line = line.trim();
		if ((line.length() == 0) || line.matches("^\\s*#")) continue;
                Matcher class_m = class_p.matcher(line);
                Matcher cost_m1 = cost_p1.matcher(line);
                Matcher cost_m2 = cost_p2.matcher(line);
                if (class_m.matches()) {
                    String classname = class_m.group(1);
                    String wordlistfile = class_m.group(2);
		    int class_num = get_class_num(classname);
		    load_word_list(wordlistfile, class_num);
                } else if (cost_m1.matches()) {
		    String type = cost_m1.group(1);
		    String classname = cost_m1.group(2);
		    int classnum = get_class_num(classname);
		    String cost_s = cost_m1.group(3);
		    double cost = Double.parseDouble(cost_s);
		    char t = type.charAt(0);
		    switch(t) {
		    case 'm': case 'M':
			match_cost.put(new Integer(classnum), new Double(cost));
			break;
		    case 'D': case 'd':
			delete_cost.put(new Integer(classnum), new Double(cost));
			break;
		    case 'i': case 'I':
			insert_cost.put(new Integer(classnum), new Double(cost));
			break;
		    case 'h': case 'H':
			shift_cost.put(new Integer(classnum), new Double(cost));
			break;
		    }
		} else if (cost_m2.matches()) {
		    String type = cost_m2.group(1);
		    String cn1 = cost_m2.group(2);
		    String cn2 = cost_m2.group(3);
		    int cnum1 = get_class_num(cn1);
		    int cnum2 = get_class_num(cn2);
		    String cost_s = cost_m2.group(4);
		    double cost = Double.parseDouble(cost_s);
		    char t = type.charAt(0);
		    switch(t) {
		    case 's': case 'S':
			if (! (sub_cost.containsKey(cnum1))) {
			    sub_cost.put(cnum1, new HashMap());			    
			}
			((HashMap) sub_cost.get(cnum1)).put(cnum2, new Double(cost));
			break;
		    case 't': case 'T':
			if (! (stem_cost.containsKey(cnum1))) {
			    stem_cost.put(cnum1, new HashMap());			    
			}
			((HashMap) stem_cost.get(cnum1)).put(cnum2, new Double(cost));
			break;
		    case 'y': case 'Y':
			if (! (syn_cost.containsKey(cnum1))) {
			    syn_cost.put(cnum1, new HashMap());			    
			}
			((HashMap) syn_cost.get(cnum1)).put(cnum2, new Double(cost));
			break;
		    }
		} else {
                    System.out.println("Warning, Invalid line: " + line);
                }
            }
        } catch(IOException ioe) {
            System.out.println("Load word class list " + fname + ": " + ioe);
	    System.exit(-1);
	    return;
	}
	
	if (_match_cost.length < numClasses) {
	    double[][] _sc = new double[numClasses][numClasses];
	    double[][] _yc = new double[numClasses][numClasses];
	    double[][] _tc = new double[numClasses][numClasses];
	    double[] _ic = new double[numClasses];
	    double[] _dc = new double[numClasses];
	    double[] _mc = new double[numClasses];
	    double[] _hc = new double[numClasses];	
	    for (int i = 0; i < _match_cost.length; i++) {
		_mc[i] = _match_cost[i];
		_hc[i] = _shift_cost[i];
		_ic[i] = _insert_cost[i];
		_dc[i] = _delete_cost[i];
		for (int j = 0; j < _match_cost.length; j++) {
		    _sc[i][j] = _sub_cost[i][j];
		    _tc[i][j] = _stem_cost[i][j];
		    _yc[i][j] = _syn_cost[i][j];
		}
	    }
	    for (int i = _match_cost.length; i < numClasses; i++) {
		_mc[i] = default_match_cost;
		_hc[i] = default_shift_cost;
		_dc[i] = default_delete_cost;
		_ic[i] = default_insert_cost;
		for (int j = 0; j < numClasses; j++) {
		    _sc[i][j] = default_substitute_cost;
		    _tc[i][j] = default_stem_cost;
		    _yc[i][j] = default_syn_cost;
		}
	    }
	    _sub_cost = _sc;
	    _syn_cost = _yc;
	    _stem_cost = _tc;
	    _match_cost = _mc;
	    _insert_cost = _ic;
	    _delete_cost = _dc;
	    _shift_cost = _hc;
	}

	// Now copy the new vals we read into the cost vectors
	for (Iterator it = match_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    _match_cost[((Integer) e.getKey()).intValue()] = ((Double) e.getValue()).doubleValue();
	}
	for (Iterator it = insert_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    _insert_cost[((Integer) e.getKey()).intValue()] = ((Double) e.getValue()).doubleValue();
	}
	for (Iterator it = delete_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    _delete_cost[((Integer) e.getKey()).intValue()] = ((Double) e.getValue()).doubleValue();
	}
	for (Iterator it = shift_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    _shift_cost[((Integer) e.getKey()).intValue()] = ((Double) e.getValue()).doubleValue();
	}
	for (Iterator it = sub_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    for (Iterator it2 = ((HashMap) e.getValue()).entrySet().iterator();
		 it2.hasNext(); ) {
		Map.Entry e2 = (Map.Entry) it2.next();
		_sub_cost[((Integer) e.getKey()).intValue()][((Integer) e2.getKey()).intValue()] = 
		    ((Double) e2.getValue()).doubleValue();
	    }
	}
	for (Iterator it = syn_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    for (Iterator it2 = ((HashMap) e.getValue()).entrySet().iterator();
		 it2.hasNext(); ) {
		Map.Entry e2 = (Map.Entry) it2.next();
		_syn_cost[((Integer) e.getKey()).intValue()][((Integer) e2.getKey()).intValue()] = 
		    ((Double) e2.getValue()).doubleValue();
	    }
	}
	for (Iterator it = stem_cost.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    for (Iterator it2 = ((HashMap) e.getValue()).entrySet().iterator();
		 it2.hasNext(); ) {
		Map.Entry e2 = (Map.Entry) it2.next();
		_stem_cost[((Integer) e.getKey()).intValue()][((Integer) e2.getKey()).intValue()] = 
		    ((Double) e2.getValue()).doubleValue();
	    }
	}
    }
    
  private int get_class_num(String name) {
      int class_num = 0;
      if (! (nameMap.containsKey(name))) {
	  class_num = numClasses;
	  nameMap.put(name, class_num);
	  revNameMap.put(class_num, name);
	  numClasses++;
      } else {
	  class_num = ((Integer) nameMap.get(name)).intValue();
      }
      return class_num;
  }
    
    /* The cost of matching ref word for hyp word. (They are equal) */    
    public double match_cost(Comparable hyp, Comparable ref) { 
	return _match_cost[findClass(hyp)];
    }
    
    /* The cost of substituting ref word for hyp word. (They are not equal) */
    public double substitute_cost(Comparable hyp, Comparable ref) {
	return _sub_cost[findClass(ref)][findClass(hyp)];
    }
    
    /* The cost of substituting ref word for hyp word. (They are not equal, but have the same stem) */
    public double stem_cost(Comparable hyp, Comparable ref) {
	return _stem_cost[findClass(ref)][findClass(hyp)];
    }
    
    /* The cost of substituting ref word for hyp word. (They are not equal, but are synonyms) */
    public double syn_cost(Comparable hyp, Comparable ref) {
	return _syn_cost[findClass(ref)][findClass(hyp)];
    }
    
    /* The cost of inserting the hyp word */
    public double insert_cost(Comparable hyp) {
	return _insert_cost[findClass(hyp)];
    }
    
    /* The cost of deleting the ref word */
    public double delete_cost(Comparable ref) {
	return _delete_cost[findClass(ref)];
    }
    
    /* The cost of making a shift */
    /* The maximum of the cost of shifting any word in the shift */
    public double shift_cost(TERshift shift) {
	double cost = 0.0;
	for (int i = 0; i < shift.shifted.size(); i++) {	  
	    double mc = _shift_cost[findClass((Comparable) shift.shifted.get(i))];
	    if (mc > cost) { cost = mc; }
	}
	return cost;
    }

    
//     protected Comparable[] process_input(String[] in) {
// 	Comparable[] wc = new Comparable[in.length];
// 	for (int i = 0; i < in.length; i++) {
// 	   wc[i] = new word_class(in[i], _findClass(in[i]));
// 	}	
// 	return wc;
//     }

    private int findClass(TERshift shift) {
	double cost = -0.0;
	int sclass = -1;
	for (int i = 0; i < shift.shifted.size(); i++) {	  
	    int mclass = findClass((Comparable) shift.shifted.get(i));
	    double mc = _shift_cost[mclass];
	    if ((sclass < 0) || (mc > cost)) { 
		cost = mc; 
		sclass = mclass;
	    }
	}
	return sclass;
    }
    
    private int[] countWords() {
	int tr[] = new int[numClasses];
	for (int i = 0; i < tr.length; i++) tr[i] = 0;
	Iterator it = wordMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry me = (Map.Entry) it.next();
	    Integer cn = (Integer) me.getValue();
	    tr[cn.intValue()]++;
	}
	return tr;
    }

    public String get_info() {
	String s = (COST_NAME + "\n");
	s += " Number of Word Classes: " + numClasses + "\n";
	s += " Generic Number Class: ";
	if (NUMBER_CLASS >= 0)
	    s += "[" + revNameMap.get(NUMBER_CLASS) + "]\n";
	else  
	    s += "none\n";
	int[] wordcounts = countWords();
	for (int i = 0; i < numClasses; i++) {	    
	    String cname = (String) revNameMap.get(i);
	    s += " Number of Words in class [" + cname + "]: " + wordcounts[i] + "\n";	    
	    s += " Match Cost      [" + cname + "] : " + _match_cost[i] + "\n";
	    s += " Insertion Cost  [" + cname + "] : " + _insert_cost[i] + "\n";
	    s += " Deletion Cost   [" + cname + "] : " + _delete_cost[i] + "\n";
	    s += " Shift Cost      [" + cname + "] : " + _shift_cost[i] + "\n";
	    for (int j = 0; j < numClasses; j++) {
		String hcname = (String) revNameMap.get(j);
		s += " Substitute Cost [" + cname + "] -> [" + hcname + "] : " + _sub_cost[i][j] + "\n";
		s += " Stem Cost       [" + cname + "] -> [" + hcname + "] : " + _stem_cost[i][j] + "\n";
		s += " Synonym Cost    [" + cname + "] -> [" + hcname + "] : " + _syn_cost[i][j] + "\n";
	    }
	}
	if (_phraseTable != null) s += _phraseTable.get_info();
	return s;		    
    }

    // Default Values
    private double default_shift_cost = 1.0;
    private double default_insert_cost = 1.0;
    private double default_delete_cost = 1.0;
    private double default_substitute_cost = 1.0;
    private double default_match_cost = 0.0;
    private double default_syn_cost = 1.0;
    private double default_stem_cost = 1.0;

    public static class word_class implements Comparable {
	public int wclass = 0;
	public String wd = null;
	public word_class(String wd, int wclass) {
	    this.wd = wd.intern();
	    this.wclass = wclass;
	}	

	public String toString() { return this.wd; }
	public boolean equals(Object o) {
	    word_class wc = (word_class) o;
	    return this.wd.equals(wc.wd);
	}
	public boolean equals(word_class wc) {
	    return this.wd.equals(wc.wd);
	}
	public boolean equals(String s) {
	    return this.wd.equals(s);
	}
	public int compareTo(Object o) {
	    word_class wc = (word_class) o;
	    return this.wd.compareTo(wc.wd);
	}
	public int compareTo(word_class wc) {
	    return this.wd.compareTo(wc.wd);
	}
	public int compareTo(String s) {
	    return this.wd.compareTo(s);
	}
    }
}
