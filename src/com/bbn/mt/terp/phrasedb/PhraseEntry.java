package com.bbn.mt.terp.phrasedb;

import java.util.Arrays;
import java.util.List;

public class PhraseEntry implements Comparable<PhraseEntry> {
    public PhraseEntry() {
	refwds = null;
	hypwds = null;
	pr = 0.0;
	source = 0;
    }

    public PhraseEntry(String[] refwds, String[] hypwds, double pr, int source) {
	this.setRefWds(refwds);
	this.setHypWds(hypwds);
	this.setProb(pr);
	this.setSource(source);
    }

    private String[] refwds;
    private String[] hypwds;
    private double pr;
    private int source;

    private static int strArCompare(String[] s1, String[] s2) {
	if (s1 == s2) return 0;
	if (s1 == null) return -1;
	if (s2 == null) return 1;
	for (int i = 0; i < s1.length; i++) {
	    if (i >= s2.length) return -1;
	    int scomp = s1[i].compareTo(s2[i]);
	    if (scomp != 0) return scomp;
	}
	if (s2.length > s1.length) return 1;
	return 0;
    }

    public boolean equals(PhraseEntry pe) {
	return (this.compareTo(pe) == 0);
    }

    public int compareTo(PhraseEntry pe) {
	int srcCompare = this.source - pe.source;
	if (srcCompare != 0) return srcCompare;

	int refCompare = strArCompare(this.refwds, pe.refwds);
	if (refCompare != 0) return refCompare;

	int hypCompare = strArCompare(this.hypwds, pe.hypwds);
	if (hypCompare != 0) return hypCompare;

	int prCompare = Double.compare(this.pr, pe.pr);
	if (prCompare != 0) return prCompare;

	return 0;
    }

    public String toString() {
	return "" + pr + " <p>" + sjoin(refwds) + "</p> <p>" + sjoin(hypwds) + "</p>";
    }

    private static String[] copyStrAr(String[] data) {
	String[] nar = new String[data.length];
	for (int i = 0; i < data.length; i++)
	    nar[i] = data[i].intern();
	return nar;
    }

    private static String sjoin(Object[] data) {
	String toret = "";
	for (int i = 0; i < data.length; i++) {
	    if (i != 0) toret += " ";
	    toret += data[i];
	}
	return toret;
    }

    public void setRefWds(String[] data) {refwds = copyStrAr(data);}
    public void setHypWds(String[] data) {hypwds = copyStrAr(data);}
    public void setSource(int data) { source = data;}
    public void setProb(double data) { pr = data;}
    
    public String[] getRefWds() {return refwds;}
    public String[] getHypWds() {return hypwds;}
    public int getSource() {return source;}
    public double getProb() {return pr;}
}
