package com.bbn.mt.terp;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.TreeMap;

public class PhraseTree {
    private HashMap<Comparable,NodeNext> _root = new HashMap();
    private int num_nodes = 1;
    private int num_terms = 0;
    private int num_entries = 0;
    private int depth = 0;

    public Iter getIter() {
	return new Iter(_root, depth);
    }

    // Add a paraphrase of sequence_a to sequence_b with cost
    // This is not symmetric!  You need to add twice to have symmetric rules

    public boolean insert(Comparable[] seq_a, Comparable[] seq_b,
			  phrase_cost cost) {
	return _insert(seq_a, seq_b, cost, _root);
    }

    private boolean _insert(Comparable[] seq_a, Comparable[] seq_b,
			    phrase_cost cost,
			    Map<Comparable,NodeNext> node) {
	
	NodeNext cur = null;
	if ((seq_a.length <= 0) || (seq_b.length <= 0)) return false;
	for (int i = 0; i < seq_a.length; i++) {
	    Comparable nk = seq_a[i];
	    if (node.containsKey(nk.toString())) { 		
		cur = (NodeNext) node.get(nk.toString()); 
	    } else {
		cur = new NodeNext();
		node.put(nk, cur);
	    }
	    if (i == (seq_a.length-1)) {
		phrase_cost prev_cost = cur.getPhraseCost(seq_b, 0, seq_b.length);
		boolean add_ok = true;
		if (prev_cost != null) {
		    // if (sum_dup_phrases) {   
		    // cost.orig_cost += prev_cost.orig_cost;
		    // cost = adjust_cost(cost, seq_a, seq_b);
		    // } else
		    if (cost.cost > prev_cost.cost) {
			add_ok = false;
		    }
		}
		if (add_ok) {
		    boolean nt = cur.add_term(seq_b, cost);
		    if (nt) this.num_terms++;
		    this.num_entries++;
		}
	    } else {
		node = cur.getCreateNext();
	    }
	}
	if (this.depth < seq_a.length) {
	    this.depth = seq_a.length;
	}
	return true;
    }
    
    // Accessor Functions for bookkeeping.
    public int num_nodes() { return num_nodes; }
    public int num_terms() { return num_terms; }
    public int num_entries() { return num_entries; }
    public int depth() {return depth;}

    public ArrayList retrieve_all(Comparable[] seq_a, int ind_a,
				  Comparable[] seq_b, int ind_b) {
	ArrayList toret = new ArrayList(10);  // Arbitrary start point
	_retrieve_all(seq_a, ind_a, seq_b, ind_b, toret, _root, 0);
	return toret;
    } 

    public phrase_cost retrieve_exact(Comparable[] seq_a, int start_a, int len_a,
				      Comparable[] seq_b, int start_b, int len_b) {
	phrase_cost pc = _retrieve_exact(seq_a, start_a, len_a,					 
					 seq_b, start_b, len_b);
	if (pc == null) return null;
	return new phrase_cost(pc);	
    }
    
    private void _retrieve_all(Comparable[] seq_a, int ind_a,
			       Comparable[] seq_b, int ind_b,
			       ArrayList toret, Map node,
			       int len_a) {
	if (node == null) return;	
	if ((ind_a + len_a) < seq_a.length) {
	    Comparable k = seq_a[ind_a + len_a];
	    if (node.containsKey(k.toString())) {
		NodeNext cur = (NodeNext) node.get(k.toString());
		if (cur.is_term()) cur.find_matches(len_a+1, seq_b, ind_b, toret);
		_retrieve_all(seq_a, ind_a, seq_b, ind_b, toret, 
			      cur.next, len_a+1);
	    }
	}
    }

    private phrase_cost _retrieve_exact(Comparable[] seq_a, int start_a, int len_a,
				       Comparable[] seq_b, int start_b, int len_b) {
	Map node = _root;
	NodeNext cur = null;
	for (int a = 0; a < len_a; a++) {
	    if (node == null) return null;
	    if (node.containsKey(seq_a[a + start_a].toString())) {
		cur = (NodeNext) node.get(seq_a[a + start_a].toString());
		node = cur.next;
	    } else {
		return null;
	    }
	}
	if (cur == null) return null;
	return cur.getPhraseCost(seq_b, start_b, len_b);
    }
    
    public static class OffsetScore {
	public OffsetScore(int len_a, int len_b, double c) {
	    ref_len = len_a; hyp_len = len_b; cost = c;
	}
	public int ref_len = 0;
	public int hyp_len = 0;
	public double cost = 9999;
	public String toString() { return "rl=" + ref_len + " hl=" + hyp_len + " cost=" + cost; }
    }

    public static class phrase_cost {	
	public phrase_cost(double ocost) {
	    orig_cost = ocost;
	    cost = ocost;
	    numedits = 0.0;
	}
	public phrase_cost(double ocost, double ncost) {
	    orig_cost = ocost;
	    cost = ncost;
	}
	public phrase_cost(double ocost, double ncost, double numedits) {
	    orig_cost = ocost;
	    cost = ncost;
	    this.numedits = numedits;
	}
	public phrase_cost(phrase_cost ph) {
	    cost = ph.cost;
	    orig_cost = ph.orig_cost;
	    numedits = ph.numedits;
	}
	public String toString() { return "" + cost; }
	public double cost;
	public double orig_cost;
	public double numedits;
    }

    private class NodeNext {
	public Map poss_b = null;
	public Map next = null;

	public Map getCreateNext() {
	    if (next == null) {
		next = new TreeMap();
	    }
	    num_nodes++;
	    return next;
	}

	public boolean add_term(Comparable[] seq, phrase_cost cost) { 
	    boolean newterm = false;
	    if (poss_b == null) {
		newterm = true;
		poss_b = new TreeMap(pt_compare);		
	    }
	    poss_b.put(seq, cost);
	    return newterm;
	}
	public ArrayList find_matches(int len_a, Comparable[] seq_b, int ind_b) {
	    ArrayList al = new ArrayList(10);
	    find_matches(len_a, seq_b, ind_b, al);
	    return al;
	}

	public void find_matches(int len_a, Comparable[] seq_b, int ind_b, ArrayList toret) {
	    if (poss_b == null) return;
	    // Check to see which of poss_b are in seq_b
	    Iterator it = poss_b.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry e = (Map.Entry) it.next();
		Comparable[] bseq = (Comparable[]) e.getKey();
		phrase_cost cost = (phrase_cost) e.getValue();
		boolean ok = true;
		if (bseq.length > (seq_b.length-ind_b)) ok = false;
		for (int i = 0; (i < bseq.length) && ok; i++) {
		    if (! (bseq[i].equals(seq_b[ind_b+i])))
			ok = false;
		}
		if (ok)
		    toret.add(new OffsetScore(len_a, bseq.length, cost.cost));
	    }
	    return;
	}

	public phrase_cost getPhraseCost(Comparable[] seq_b, int start_b, int len_b) {
	    if (poss_b == null) return null;

	    Iterator it = poss_b.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry e = (Map.Entry) it.next();
		Comparable[] bseq = (Comparable[]) e.getKey();
		if (bseq.length != len_b) continue;
		phrase_cost cost = (phrase_cost) e.getValue();
		boolean ok = true;
		for (int i = 0; (i < bseq.length) && ok; i++) {
		    if (! (bseq[i].equals(seq_b[start_b+i])))
			ok = false;
		}
		if (ok) {
		    return cost;
		}
	    }
	    return null;
	}

	public boolean is_leaf() { return (next == null); }
	public boolean is_term() { return (poss_b != null); }	
    }

    public class Iter {
	private Iter(Map root, int depth) {
	    path = new ArrayList(depth+1);
	    path.add(root.keySet().iterator());
	    path_hm = new ArrayList(depth+1);
	    path_hm.add(root);
	    seq_a = new ArrayList(depth);	    

	    cur = null;
	    curIter = null;

	    seq_b = null;
	    _cost = null;
	}

	public boolean hasNext() {
	    if ((curIter != null) && curIter.hasNext()) return true;	    
	    if ((cur != null) && (cur.next != null)) return true;
	    for (int i = path.size() - 1; i >= 0; i--) {
		if (((Iterator) path.get(i)).hasNext()) return true;
	    }
	    return false;
	}

	public String next() {	   
	    seq_b = null;
	    _cost = null;

	    boolean walk_up = true;

	    if ((curIter != null) && curIter.hasNext()) {
		// Is there another seq_b at this node?
		seq_b = (Comparable[]) curIter.next();
		_cost = (phrase_cost) cur.poss_b.get(seq_b);
		return this.toString();
	    } else if (curIter != null) { 
		// Let's keep going down the nodes if we can
		curIter = null; 
		if (cur.next == null) {
		    // That's it, we're at a leaf.
		    cur = null;		
		    walk_up = true;
		} else {
		    // There should be something below us!
		    walk_up = false;
		    // Move down the path one.
		    Iterator cp = cur.next.keySet().iterator();
		    path.add(cp);
		    path_hm.add(cur.next);
		    cur = null;
		    if (! cp.hasNext()) {
			// This shouldn't be possible, it means we are at a leaf afterall
			walk_up = true;
		    }
		}
	    }

	    if (walk_up) {
		// We're at a lead, we need to walk back up.
		while ((seq_a.size() > 0) && (seq_a.size() >= path.size()))
		    seq_a.remove(seq_a.size() - 1);
		
		int i = 0;
		for (i = path.size() - 1; 
		     (i >= 0) && (! ((Iterator) path.get(i)).hasNext()); 
		     i--) {
		    path.remove(i);
		    path_hm.remove(i);
		    while ((seq_a.size() > 0) && (seq_a.size() >= i)) 
			seq_a.remove(seq_a.size() - 1);
		}
		if (i < 0) return null;
	    }

	    // We've now walked up to a point where there should be something below this node. (path.get(i) hasNext)
	    Iterator ci = (Iterator) path.get(path.size() - 1);
	    Map cm = (Map) path_hm.get(path_hm.size() - 1);
	    
	    while (true) {
		if (! ci.hasNext()) {
		    // This shouldn't be possible.
		    return next();
		}

		Comparable na = (Comparable) ci.next();
		seq_a.add(na);
		cur = (NodeNext) cm.get(na);
		curIter = null;

		if (cur.poss_b != null) {
		    // There is something at this node!
		    curIter = cur.poss_b.keySet().iterator();
		    return next();		    
		} 
		
		if (cur.next == null) {
		    // This shouldn't be possible....
		    return next();
		}

		ci = cur.next.keySet().iterator();
		cm = cur.next;
		path.add(ci);
		path_hm.add(cm);
		cur = null;
	    }	    
	}


	public String toString() {
	    if (cur == null) return "";
	    return "" + cost() + " <p>" + 
		TERalignment.join(" ", phrase_a()) + "</p> <p>" + 
		TERalignment.join(" ", phrase_b()) + "</p>";
	}

	public Comparable[] phrase_a() {
	    Comparable[] tr = new Comparable[seq_a.size()];
	    for (int i = 0; i < seq_a.size(); i++) {
		tr[i] = (Comparable) seq_a.get(i);
	    }
	    return tr;
	}

	public Comparable[] phrase_b() {
	    Comparable[] tr = new Comparable[seq_b.length];
	    for (int i = 0; i < seq_b.length; i++) {
		tr[i] = seq_b[i];
	    }
	    return tr;
	}

	public phrase_cost phcost() {
	    return _cost;
	}

	public double cost() {
	    return _cost.cost; 
	}

	private phrase_cost _cost = null;
	private NodeNext cur = null;
	private Iterator curIter = null;
	private ArrayList path;
	private ArrayList path_hm;
	private ArrayList seq_a = null;
	private Comparable[] seq_b = null;
    }

    private static class PhraseComparator implements Comparator {
	public int compare(Object o1, Object o2) { 
	    Comparable[] a1 = (Comparable[]) o1;
	    Comparable[] a2 = (Comparable[]) o2;
	    if ((a1 == null) && (a1 == null)) {
		return 0;
	    } else if (a1 == null) { return -1; 
	    } else if (a2 == null) { return 1;
	    }

	    if (a1.length > a2.length) {
		return 1;
	    } else if (a2.length > a1.length) {
		return -1;
	    } else {
		for (int i = 0; i < a1.length; i++) {
		    int c1 = a1[i].compareTo(a2[i]);
		    if (c1 != 0) return c1;
		}
	    }
	    return 0;
	}
    }

    private static Comparator pt_compare = new PhraseComparator();
}
