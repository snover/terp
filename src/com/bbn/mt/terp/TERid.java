package com.bbn.mt.terp;

import java.util.regex.*;

public class TERid implements Comparable {
    public String test_id = "";
    public String doc_id = "";
    public String seg_id = "";
    public String sys_id = "";
    public String id = "";
    public String nbest = "";
    
    public boolean equals(Object o) {
	return this.equals((TERid) o);
    }
    public int compareTo(Object o) {
	return this.compareTo((TERid) o);
    }

    public boolean equals(TERid tid) {
	return (this.compareTo(tid) == 0);
    }

    public int compareTo(TERid tid) {
	if (! tid.test_id.equals(this.test_id)) return this.test_id.compareTo(tid.test_id);
	if (! tid.sys_id.equals(this.sys_id)) return this.sys_id.compareTo(tid.sys_id);
	if (! tid.doc_id.equals(this.doc_id)) return this.doc_id.compareTo(tid.doc_id);
	if (! tid.seg_id.equals(this.seg_id)) {
	    try { 
		int tid_seg_int = Integer.parseInt(tid.seg_id);
		int this_seg_int = Integer.parseInt(this.seg_id);
		if (tid_seg_int != this_seg_int) return (this_seg_int - tid_seg_int);
	    } catch(Exception ex) {
		return this.seg_id.compareTo(tid.seg_id);
	    }
	}
	if (! tid.nbest.equals(this.nbest)) {
	    try { 
		int tid_nb_int = Integer.parseInt(tid.nbest);
		int this_nb_int = Integer.parseInt(this.nbest);
		if (tid_nb_int != this_nb_int) return (this_nb_int - tid_nb_int);
	    } catch(Exception ex) {
		return this.nbest.compareTo(tid.nbest);
	    }
	}
	if (! tid.id.equals(this.id)) return this.id.compareTo(tid.id);
	return 0;
    }
    
    private static final Pattern systestdocsegnb_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]:0*([0-9]+?)$");
    private static final Pattern systestdocseg_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]$");
    private static final Pattern testdocsegnb_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]:0*([0-9]+?)$");
    private static final Pattern testdocseg_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]$");
    private static final Pattern docseg_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]$");
    private static final Pattern docsegnb_pat = Pattern.compile("^\\[([^\\[\\]]+)\\]\\[0*([0-9]+?)\\]:0*([0-9]+?)$");

    public static TERid parseIDnf(String id) {
	// No fail version.  Always returns some kind of TERid
	TERid tid = parseID(id);
	if (tid != null) return tid;
	return new TERid(id);
    }

    public static TERid parseID(String id) {
	// Reads in ids of the form:
	// (Default version)
	// [Tune.chi.text.nw.v3][AFC20040102.0018][00001]
	//    or 
	// (Nbest list version)
	// [Tune.chi.text.nw.v3][AFC20040102.0018][00001]:9
	// returns null 
	Matcher m1 = systestdocsegnb_pat.matcher(id);
	Matcher m2 = systestdocseg_pat.matcher(id);
	Matcher m3 = testdocsegnb_pat.matcher(id);
	Matcher m4 = testdocseg_pat.matcher(id);
	Matcher m5 = docseg_pat.matcher(id);
	Matcher m6 = docsegnb_pat.matcher(id);

	String set = "";
	String sys = "";
	String doc = "";
	String seg = "";
	String nbst = "";

	if (m1.matches()) {
	    sys = m1.group(1).trim();
	    set = m1.group(2).trim();
	    doc = m1.group(3).trim();
	    seg = m1.group(4).trim();
	    nbst = m1.group(5).trim();
	} else if (m2.matches()) {
	    sys = m2.group(1).trim();
	    set = m2.group(2).trim();
	    doc = m2.group(3).trim();
	    seg = m2.group(4).trim();
	} else if (m3.matches()) {
	    set = m3.group(1).trim();
	    doc = m3.group(2).trim();
	    seg = m3.group(3).trim();
	    nbst = m3.group(4).trim();
	} else if (m4.matches()) {
	    set = m4.group(1).trim();
	    doc = m4.group(2).trim();
	    seg = m4.group(3).trim();
	} else if (m5.matches()) {
	    doc = m5.group(1).trim();
	    seg = m5.group(2).trim();
	} else if (m6.matches()) {
	    doc = m6.group(1).trim();
	    seg = m6.group(2).trim();
	    nbst = m6.group(3).trim();
	} else {
	    return null;
	}

	return new TERid(id, set, sys, doc, seg, nbst);
    }
    
    public TERid(String id) {
	this.test_id = ""; this.sys_id = "";
	this.doc_id = ""; this.seg_id = "";
	this.nbest = "";
	this.id = id;

    }

    public TERid(TERid tid, String sys_id) {
	this.test_id = tid.test_id; this.sys_id = sys_id;
	this.doc_id = tid.doc_id; this.seg_id = tid.seg_id;
	this.nbest = tid.nbest;
	this.id = tid.id;
    }

    public TERid(TERid tid) {
	this.test_id = tid.test_id; this.sys_id = tid.sys_id;
	this.doc_id = tid.doc_id; this.seg_id = tid.seg_id;
	this.nbest = tid.nbest;
	this.id = tid.id;
    }

    public TERid(String id, String test_id, String sys_id, String doc_id, String seg_id) {
	this.test_id = test_id; this.sys_id = sys_id;
	this.doc_id = doc_id; this.seg_id = seg_id;
	this.nbest = "";
	this.id = id;
    }
    
    public TERid(String id, String test_id, String sys_id, String doc_id, String seg_id, String nbest) {
	this.test_id = test_id; this.sys_id = sys_id;
	this.doc_id = doc_id; this.seg_id = seg_id;
	this.nbest = nbest;
	this.id = id;
    }
    
    public String toString () {
	if (! this.id.equals("")) return this.id;

	String testid_str = "";
	if (! test_id.equals("")) testid_str = "[" + test_id + "]";

	String sysid_str = "";
	if (! sys_id.equals("")) sysid_str = "[" + sys_id + "]";

	String docid_str = "";
	if (! doc_id.equals("")) docid_str = "[" + doc_id + "]";

	String segid_str = "";
	if (! seg_id.equals("")) segid_str = "[" + seg_id + "]";

	String nbest_str = "";
	if (! nbest.equals("")) nbest_str = ":" + nbest;

	return testid_str + sysid_str + docid_str + segid_str + nbest_str;
    }

    public String toStringNoSys () {
	if (! this.id.equals("")) return this.id;

	String testid_str = "";
	if (! test_id.equals("")) testid_str = "[" + test_id + "]";

	String docid_str = "";
	if (! doc_id.equals("")) docid_str = "[" + doc_id + "]";

	String segid_str = "";
	if (! seg_id.equals("")) segid_str = "[" + seg_id + "]";

	String nbest_str = "";
	if (! nbest.equals("")) nbest_str = ":" + nbest;

	return testid_str + docid_str + segid_str + nbest_str;
    }

    public int hashCode() {
	return this.toString().hashCode();
    }

    public TERid getPlainIdent() {
	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return new TERid(this);
	String fid = "[" + test_id + "][" + doc_id + "][" + seg_id + "]";
	return new TERid(fid, this.test_id, "", this.doc_id, 
			 this.seg_id, "");
    }
    
    public String toPlainIdentString() {
	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return id + "\t\t";
	return test_id + "\t" + doc_id + "\t" + seg_id;
    }
    
    public String toSysString(String sysid) {
	if (test_id.equals(""))
	    return id + "\t" + sysid;
	return test_id + "\t" + sysid;
    }

    public String toDocString(String sysid) {
	if (test_id.equals("") && doc_id.equals(""))
	    return id + "\t" + sysid + "\t";
	return test_id + "\t" + sysid + "\t" + doc_id;
    }

    public String toSegString(String sysid) {
	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return id + "\t" + sysid + "\t" + "" + "\t" + "";
	return test_id + "\t" + sysid + "\t" + doc_id + "\t" + seg_id;
    }

    public String toPureSegGuid() {
	String sysstr = (sys_id.equals("") ? "" : ("[" + sys_id + "]"));
	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return sysstr + "[" + id + "][][]";
	return sysstr + "[" + test_id + "][" + doc_id + "][" + seg_id + "]";
    }
    
    public String toPureSegGuid(String sysid) {
	String sysstr = (sysid.equals("") ? "" : ("[" + sysid + "]"));
	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return sysstr + "[" + id + "][][]";
	return sysstr + "[" + test_id + "][" + doc_id + "][" + seg_id + "]";
    }

    public String toSegGuid(String sysid) {
	String sysstr = (sysid.equals("") ? "" : ("[" + sysid + "]"));
	String nbeststr = (nbest.equals("") ? "" : (":" + this.nbest));

	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return sysstr + "[" + id + "][][]"  + nbeststr;;	
	return sysstr + "[" + id  + "][][]" + nbeststr;
    }
    
    public String toSegGuid() {
	String sysstr = (sys_id.equals("") ? "" : ("[" + sys_id + "]"));
	String nbeststr = (nbest.equals("") ? "" : (":" + this.nbest));

	if (test_id.equals("") && doc_id.equals("") && seg_id.equals(""))
	    return sysstr + "[" + id + "][][]" + nbeststr;;	
	
	return sysstr + "[" + test_id + "][" + doc_id + "][" + seg_id + "]" + nbeststr;
    }
}

