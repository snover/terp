package com.bbn.mt.terp.phrasedb;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import static com.sleepycat.persist.model.Relationship.*;
import com.sleepycat.persist.model.SecondaryKey;

import java.util.Arrays;
import java.util.List;

@Entity
public class PhraseNumEntry {
    public PhraseNumEntry() {
	id = null;
	refwds = null;
	hypwds = null;
	pr = 0.0;
	source = 0;
    }
    public PhraseNumEntry(Integer[] refwds, Integer[] hypwds, double pr, int source) {
	id = null;
	this.setRefWds(refwds);
	this.setHypWds(hypwds);
	this.setProb(pr);
	this.setSource(source);
    }

    // Primary key is an id number
    @PrimaryKey(sequence="PHRASE_ENTRY")
    private Integer id = null;    
   
    // Secondary key
    @SecondaryKey(relate=MANY_TO_ONE)
    private String refwds;

    private Integer[] hypwds;
    private double pr;
    private int source;

    public String toString() {
	return "" + pr + " <p>" + refwds + "</p> <p>" + sjoin(hypwds) + "</p>";
    }

    private static Integer[] copyIntAr(int[] data) {
	Integer[] nar = new Integer[data.length];
	for (int i = 0; i < data.length; i++)
	    nar[i] = data[i];
	return nar;
    }
    private static Integer[] copyIntAr(Integer[] data) {
	Integer[] nar = new Integer[data.length];
	for (int i = 0; i < data.length; i++)
	    nar[i] = data[i];
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
    private static String sjoin(int[] data) {
	String toret = "";
	for (int i = 0; i < data.length; i++) {
	    if (i != 0) toret += " ";
	    toret += data[i];
	}
	return toret;
    }

    public void setRefWds(int[] data) {
	refwds = sjoin(data);
    }
    public void setRefWds(Integer[] data) {
	refwds = sjoin(data);
    }

    public void setHypWds(Integer[] data) {
	hypwds = copyIntAr(data);
    }
    public void setHypWds(int[] data) {
	hypwds = copyIntAr(data);
    }
    public void setSource(int data) { source = data;}
    public void setProb(double data) { pr = data;}
    
    public Integer[] getRefIds() {
	String[] sar = refwds.split(" ");
	Integer[] toret = new Integer[sar.length];
	for (int i = 0; i < sar.length; i++) {
	    toret[i] = Integer.valueOf(sar[i]);
	}
	return toret;
    }

    public Integer[] getHypIds() {return hypwds;}
    public int getSource() {return source;}
    public double getProb() {return pr;}
}
