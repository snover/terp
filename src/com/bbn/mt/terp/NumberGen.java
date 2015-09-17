package com.bbn.mt.terp;

import java.util.regex.*;
import java.util.HashMap;


public class NumberGen implements Comparable {
    private String general_form = "";
    private String canon_form = "";
    private String orig_form = "";
    private boolean is_num = false;

    static final String NUM_GEN_FORM = "[NUMBER]";

    public NumberGen(String wd) {
	this.orig_form = wd;
	this.canon_form = NormalizeText.parseNumber(wd);
	if (NormalizeText.isNum(this.canon_form)) {
	    this.general_form = NUM_GEN_FORM;
	    this.is_num = true;
	} else {
	    this.general_form = wd;
	    this.is_num = false;
	}
    }

    public String toString() {
	// return this.general_form;
	return this.orig_form;
    }

    public int compareTo(Object c) {
	NumberGen n = (NumberGen) c;
	if (n.is_num && this.is_num) return 0;
	return this.orig_form.compareTo(n.orig_form);
    }

    public boolean equals(Object o) {
	NumberGen n = (NumberGen) o;
	boolean tr = false;
	if (n.is_num && this.is_num) tr = true;
	else tr = (n.orig_form.equals(this.orig_form));
	//	System.out.println("Comparing " + this + " to " + n + " equals=" + tr);
	return tr;
    }
}
