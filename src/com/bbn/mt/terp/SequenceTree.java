package com.bbn.mt.terp;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

public class SequenceTree {

    public static class SeqTreeRet {
	public final int len;
	public final List<Object> val;
	public SeqTreeRet(int l, List<Object> v) {
	    len = l;
	    val = v;	    
	}
    }

    protected class Node {
	public List<Object> vals;
	public TreeMap<Comparable,Node> next;
	public Node() {
	    vals = new ArrayList();
	    next = new TreeMap();
	}
    }

    protected Node root = new Node();

    public void insert(Comparable[] seq, Object val) {	
	Node tnode = root;
	for(int i = 0; i < seq.length; i++) {
	    if (tnode.next.containsKey(seq[i])) {
		tnode = tnode.next.get(seq[i]);
	    } else {
		Node nnode = new Node();
		tnode.next.put(seq[i], nnode);
		tnode = nnode;
	    }
	}
	tnode.vals.add(val);
    }

    public void insert(Comparable[] seq, int start, int len, Object val) {	
	Node tnode = root;
	for(int i = start; i < start+len; i++) {
	    if (tnode.next.containsKey(seq[i])) {
		tnode = tnode.next.get(seq[i]);
	    } else {
		Node nnode = new Node();
		tnode.next.put(seq[i], nnode);
		tnode = nnode;
	    }
	}
	tnode.vals.add(val);
    }

    public boolean contains(Comparable[] seq) {
	Node tnode = root;
	int i = 0;
	
	while (tnode != null) {
	    if (i == seq.length)
		return (tnode.vals.size() > 0);
	    if (i < seq.length) {		
		tnode = tnode.next.get(seq[i]);		
		i++;
	    }
	}
	return false;	
    }

    public boolean contains(Comparable[] seq, int start, int len) {
	Node tnode = root;
	int i = 0;
	
	while (tnode != null) {
	    if (i == len)
		return (tnode.vals.size() > 0);
	    if (i < len) {		
		tnode = tnode.next.get(seq[start+i]);		
		i++;
	    }
	}
	return false;	
    }

    public List<Object> find(Comparable[] seq, int start, int len) {
	Node tnode = root;
	int i = 0;

	if (start+len > seq.length) return null;

	while (tnode != null) {
	    if (i == len) {
		if (tnode.vals.size() > 0) {
		    return tnode.vals;
		} else {
		    return null;
		}
	    }
	    if (i < seq.length) {		
		tnode = tnode.next.get(seq[start+i]);		
		i++;
	    }
	}
	return null;	
    }
    
    public List<SeqTreeRet> find(Comparable[] seq, int ind) {
	Node tnode = root;
	int i = 0;
	ArrayList<SeqTreeRet> tr = new ArrayList();
	while (tnode != null) {
	    if (tnode.vals.size() > 0) {
		tr.add(new SeqTreeRet(i, tnode.vals));
	    }
	    if ((ind+i) < seq.length) {		
		tnode = tnode.next.get(seq[ind+i]);		
		i++;
	    }
	}
	return tr;
    }

    public String toString() {
	return _toString("", root);
    }
    protected String _toString(String indent, Node n) {
	String tr = "";
	tr += indent + " [ ";
	for (Object o : n.vals) {
	    tr += o.toString() + " ";
	}
	tr += "]\n";
	for (Comparable k : n.next.keySet()) {
	    tr += indent + " - " + k + "\n";
	    tr += _toString(indent + "   ", n.next.get(k));
	}
	return tr;
    }

    public List<Integer> find_len(Comparable[] seq, int ind) {
	//	System.out.println(this.toString());
	Node tnode = root;
	int i = 0;
	ArrayList<Integer> tr = new ArrayList();
	while (tnode != null) {
	    if ((i != 0) && (tnode.vals.size() > 0)) {
		tr.add(new Integer(i));
	    }
	    if ((ind+i) < seq.length) {		
		if (tnode.next.containsKey(seq[ind+i])) {
		    tnode = tnode.next.get(seq[ind+i]);		
		    i++;	    
		} else {
		    return tr;
		}
	    } else {
		return tr;
	    }
	    if (tnode == null) return tr;
	}
	return tr;
    }
}
