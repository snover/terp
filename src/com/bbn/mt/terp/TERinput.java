package com.bbn.mt.terp;

import java.io.*;
import java.util.regex.*;
import java.util.Map;
import java.util.List;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import org.w3c.dom.Document;

public class TERinput {
    //    private HashMap hypSysSegs = new HashMap();
    //    private HashMap tokhypSysSegs = new HashMap();
    //    private LinkedHashMap refsegs = new LinkedHashMap();
    //    private HashMap tokrefsegs = new HashMap();
    //    private LinkedHashMap reflensegs = null;
    
    private boolean ignore_setid = false;
    private int in_hyp_format = 0;
    private int in_ref_format = 0;   
    private int in_len_format = 0;   


    private static enum STYPE {
	REF, LEN, HYP
    }

    private List<String> untok_data = new ArrayList<String>();
    private List<String[]> tok_data = new ArrayList<String[]>();
    private int numdata = 0;
    private int add_sen(String sen) {
	sen = sen.intern();
	String[] tokens = NormalizeText.process(sen);
	untok_data.add(sen);
	tok_data.add(tokens);
	numdata++;
	if ((untok_data.size() != tok_data.size()) ||
	    (numdata != tok_data.size())) {
	    System.err.println("Internal error in TERinput.  Aborting.");
	    System.exit(-1);
	}
	return (numdata-1);
    }
    private String[] get_tok(int i) { return tok_data.get(i); }
    private String get_untok(int i) { return untok_data.get(i); }

    private List<String[]> get_tok(List<Integer> ls) { 
	if (ls == null) return null;
	ArrayList<String[]> als = new ArrayList(ls.size());
	for (int i = 0; i < ls.size(); i++) {
	    als.add(i, get_tok(ls.get(i)));
	}
	return als;
    }
    private List<String> get_untok(List<Integer> ls) { 
	if (ls == null) return null;
	ArrayList<String> als = new ArrayList(ls.size());
	for (int i = 0; i < ls.size(); i++) {
	    als.add(i, get_untok(ls.get(i)));
	}
	return als;
    }


    private TreeMap<TERid, TreeSet<TERid> > allSegIds = new TreeMap();
    private HashMap<TERid, TreeSet<TERid> > refSegIds = new HashMap();
    private HashMap<TERid, TreeSet<TERid> > lenSegIds = new HashMap();

    private HashMap<TERid, List<Integer> > ref_segs = new HashMap();
    private HashMap<TERid, List<Integer> > hyp_segs = new HashMap();
    private HashMap<TERid, List<Integer> > len_segs = new HashMap();
    private TreeSet<String> sysids = new TreeSet();

    public TreeSet<String> get_sysids() {
	return this.sysids;
    }

    public boolean hasHypSeg(TERid tid) {
	List<Integer> segs = hyp_segs.get(tid);
	return ((segs != null) && (segs.size() > 0));
    }

    public Set<TERid> getPlainIds() {
	return allSegIds.keySet();
    }

    public Set<TERid> getHypIds(String sys_id, TERid plainId) {
	TreeSet<TERid> toret = new TreeSet();
	TreeSet<TERid> hs = allSegIds.get(plainId);
	boolean foundAny = false;
	for (TERid tid : hs) {
	    if (tid.sys_id.equals(sys_id)) {
		foundAny = true;
		toret.add(tid);
	    }
	}
	if (! foundAny)
	    toret.add(new TERid(plainId, sys_id));
	return toret;
    }

    public Set<TERid> getHypIds(String sys_id) {
	TreeSet<TERid> toret = new TreeSet();
	for (TERid pid : allSegIds.keySet())
	    toret.addAll(this.getHypIds(sys_id, pid));
	return toret;
    }

    private static final List<String> fakevec = new Vector();
    private static final List<String[]> faketokvec = new Vector();
    static {
	fakevec.add("");
	String[] ft = new String[0];
	faketokvec.add(ft);
    }

    public List<String> getAllHyp(TERid tid) {
	List<String> toret = get_untok(hyp_segs.get(tid));
	if ((toret == null) || (toret.size() == 0))
	    return fakevec;
	return toret;
    }

    public List<String[]> getAllHypTok(TERid tid) {
	List<String[]> toret = get_tok(hyp_segs.get(tid));
	if ((toret == null) || (toret.size() == 0))
	    return faketokvec;
	return toret;
    }

    public List<String> getHypForPlain(TERid tid) {
	TERid pid = tid.getPlainIdent();
	Set<TERid> hs = allSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0))
	    return fakevec;
	Vector<String> toret = new Vector();
	for (TERid htid : hs) {
	    List<String> segs = get_untok(hyp_segs.get(htid));
	    if (segs != null)
		toret.addAll(segs);			 
	}
	if ((toret == null) || (toret.size() == 0))
	    return fakevec;
	return toret;
    }    
    public List<String[]> getHypForPlainTok(TERid tid) {
	TERid pid = tid.getPlainIdent();
	TreeSet<TERid> hs = allSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0))
	    return faketokvec;
	Vector<String[]> toret = new Vector();
	for (TERid htid : hs) {
	    List<String[]> segs =get_tok(hyp_segs.get(htid));
	    if (segs != null)
		toret.addAll(segs);			 
	}
	if ((toret == null) || (toret.size() == 0))
	    return faketokvec;
	return toret;
    }    


    public List<String> getRefForHyp(TERid htid) {
	TERid pid = htid.getPlainIdent();
	TreeSet<TERid> hs = refSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0)) {
	    return fakevec;
	}
	Vector<String> toret = new Vector();
	for (TERid tid : hs) {
	    toret.addAll(get_untok(ref_segs.get(tid)));
	}
	if ((toret == null) || (toret.size() == 0))
	    return fakevec;
	return toret;
    }    

    public List<String[]> getRefForHypTok(TERid htid) {
	TERid pid = htid.getPlainIdent();
	TreeSet<TERid> hs = refSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0)) {
	    return faketokvec;
	}
	Vector<String[]> toret = new Vector();
	for (TERid tid : hs) {
	    toret.addAll(get_tok(ref_segs.get(tid)));
	}
	if ((toret == null) || (toret.size() == 0))
	    return faketokvec;
	return toret;
    }    

    public List<String> getLenForHyp(TERid htid) {
	TERid pid = htid.getPlainIdent();
	TreeSet<TERid> hs = lenSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0)) {
	    return null;
	}
	Vector<String> toret = new Vector();
	for (TERid tid : hs) {
	    toret.addAll(get_untok(len_segs.get(tid)));
	}
	if ((toret == null) || (toret.size() == 0))
	    return null;
	return toret;
    }    

    public List<String[]> getLenForHypTok(TERid htid) {
	TERid pid = htid.getPlainIdent();
	TreeSet<TERid> hs = lenSegIds.get(pid);
	if ((hs == null) || (hs.size() == 0)) {
	    return null;
	}
	Vector<String[]> toret = new Vector();
	for (TERid tid : hs) {
	    toret.addAll(get_tok(len_segs.get(tid)));
	}
	if ((toret == null) || (toret.size() == 0))
	    return null;
	return toret;
    }    

    private void add_segment(TERid id, String seg, STYPE type) {
	if (this.ignore_setid)
	    id.test_id = "";

	int seg_i = add_sen(seg);
	TreeSet<TERid> idhs = null;
	TERid pid = id.getPlainIdent();
	idhs = allSegIds.get(pid);
	if (idhs == null) {
	    idhs = new TreeSet();
	    allSegIds.put(pid,idhs);
	}
	idhs.add(id);
	if (type == STYPE.HYP) {
	    sysids.add(id.sys_id);
	} else if (type == STYPE.REF) {
	    TreeSet<TERid> ridhs = null;
	    ridhs = refSegIds.get(pid);
	    if (ridhs == null) {
		ridhs = new TreeSet();
		refSegIds.put(pid,ridhs);
	    }
	    ridhs.add(id);
	} else if (type == STYPE.LEN) {
	    TreeSet<TERid> lidhs = null;
	    lidhs = lenSegIds.get(pid);
	    if (lidhs == null) {
		lidhs = new TreeSet();
		lenSegIds.put(pid,lidhs);
	    }
	    lidhs.add(id);
	}

	HashMap<TERid, List<Integer> > hm;
	switch (type) {
	case HYP: hm = this.hyp_segs; break;
	case REF: hm = this.ref_segs; break;
	case LEN: hm = this.len_segs; break;
	default: return;
	}
	List<Integer> lst = null;
	lst = hm.get(id);
	if (lst == null) {
	    lst = new Vector();
	    hm.put(id, lst);
	}
	lst.add(seg_i);
	return;
    }

    public TERinput() {}
    public TERinput(String hyp_fn, String ref_fn, boolean ignore_setid) {
	this.ignore_setid = ignore_setid;
	load_hyp(hyp_fn);
	load_ref(ref_fn);
    }
    public TERinput(String hyp_fn, String ref_fn, String reflen_fn, boolean ignore_setid) {
	this.ignore_setid = ignore_setid;
	load_hyp(hyp_fn);
	load_ref(ref_fn);
	load_len(reflen_fn);
    }

    public void set_ignore_setid( boolean ignore_setid ) {
	this.ignore_setid = ignore_setid;
    }

    public boolean ignore_setid() {
	return this.ignore_setid;
    }

    public int in_hyp_format() { return in_hyp_format; }
    public int in_ref_format() { return in_ref_format; }    
    public int in_len_format() { return in_len_format; }    

    public void load_ref(String ref_fn) {
	LinkedHashMap<TERid, List<String> > segs = new LinkedHashMap();
	int form = load_file(ref_fn, segs);
	in_ref_format = form;
	
	for (Map.Entry<TERid, List<String> > me : segs.entrySet()) {
	    TERid id = me.getKey();
	    List<String> lst = me.getValue();
	    if (lst.size() > max_num_refs) {
		max_num_refs = lst.size();
	    }
	    for (String s : lst) {
		add_segment(id, s, STYPE.REF);
	    }
	}
    }

    public void load_hyp(String hyp_fn) {
	LinkedHashMap<TERid, List<String> > segs = new LinkedHashMap();
	int form = load_file(hyp_fn, segs);
	in_hyp_format = form;
	
	for (Map.Entry<TERid, List<String> > me : segs.entrySet()) {
	    TERid id = me.getKey();
	    List<String> lst = me.getValue();
	    for (String s : lst) {
		add_segment(id, s, STYPE.HYP);
	    }
	}
    }

    public void load_len(String len_fn) {
	LinkedHashMap<TERid, List<String> > segs = new LinkedHashMap();
	int form = load_file(len_fn, segs);
	in_len_format = form;

	for (Map.Entry<TERid, List<String> > me : segs.entrySet()) {
	    TERid id = me.getKey();
	    List<String> lst = me.getValue();
	    for (String s : lst) {
		add_segment(id, s, STYPE.LEN);
	    }
	}
    }

    private static int load_file(String fn, Map<TERid, List<String> > segs) {
	int form = 0;
	if ((fn == null) || (fn.equals("")))
	    return form;	

	TERsgml sgm = new TERsgml();
	Document doc = sgm.parse(fn);

	if(doc == null) {
	    load_trans_segs(fn, segs);
	    System.out.println("\"" + fn + "\" was successfully parsed as Trans text");
	    form = 1;
	} else {
	    if (doc == null)
		System.out.println("doc is null");
	    
	    TERsgml.loadSegs(doc, segs);
	    form =  2;
	}
	return form;
    }
    
    private static void load_trans_segs(String fn, 
					Map<TERid, List<String> > segs) {
	Pattern p = Pattern.compile("^\\s*(.*?)\\s*\\(([^()]+)\\)\\s*$");
	BufferedReader stream;
	
	try {
	    stream = new BufferedReader(new FileReader(fn));
	} catch (IOException ioe) {
	    System.out.println("Loading segs from " + fn + ": " + ioe);
	    System.exit(-1);
	    return;
	}
	
	try {
	    String line;
	    while ((line = stream.readLine()) != null) {
		if (line.matches("^\\s*$")) {
		    continue;
		}
		Matcher m = p.matcher(line);
		if (m.matches()) {
		    String text = m.group(1);
		    String id = m.group(2);
		    TERid tid = TERid.parseID(id);
		    if (tid == null) {
			System.err.println("WARNING: Unable to fully parse id: " + id);
			tid = new TERid(id);
		    } 
		    List<String> lst = segs.get(tid);
		    if (lst == null) {
			lst = new Vector();
			segs.put(tid, lst);
		    }
		    lst.add(text.trim());
		} else {
		    System.out.println("Warning, Invalid line: " + line);
		}
	    }
	} catch(IOException ioe) {
	    System.out.println("Loading segs from " + fn + ": " + ioe);
	    System.exit(-1);
	}
	return;
    } 

    public int max_num_refs() { return max_num_refs; }
    private int max_num_refs = 0;
}
