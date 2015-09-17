package com.bbn.mt.terp;

import java.util.List;

public class BLEUalignment {
    public BLEU calc = null;

    public String[] hyp;
    public String[][] refs;

    public double hyp_len = 0.0;
    public double ref_len = 0.0;
    
    public double coverage_cnt[] = null;
    public int coverage_len[] = null;
    public int max_ngram = 0;

    private double[] part_clip = null;
    private double[] part_unclip = null;


    public BLEUalignment() {}

    public BLEUalignment(BLEU calc) {
	this.calc = calc;
    }

    public static BLEUalignment merge(List<BLEUalignment> ls) {
	BLEUalignment tr = new BLEUalignment();
	boolean first = true;
	for (BLEUalignment ba : ls) {
	    if (first) {
		first = false;
		tr.calc = ba.calc;
		tr.max_ngram = ba.max_ngram;
		tr.part_clip = new double[tr.max_ngram];
		tr.part_unclip = new double[tr.max_ngram];
		tr.coverage_cnt = null;
		tr.coverage_len = null;
		tr.hyp_len = 0.0;
		tr.ref_len = 0.0;
	    }
	    for (int i = 0; i < tr.max_ngram; i++) {
		tr.part_clip[i] += ba.part_clip[i];
		tr.part_unclip[i] += ba.part_unclip[i];
		tr.hyp_len += ba.hyp_len;
		tr.ref_len += ba.ref_len;
	    }
	}
	return tr;
    }

    public void init() {
	if (calc == null) return;
	max_ngram = calc.max_ngram_size();
	part_clip = new double[max_ngram];
	part_unclip = new double[max_ngram];	
	coverage_cnt = new double[hyp.length];	
	coverage_len = new int[hyp.length];	
	hyp_len = hyp.length;
	ref_len = 0.0;
	for (int i = 0; i < refs.length; i++) {
	    ref_len += refs[i].length;
	}
	ref_len /= refs.length;
    }
    
    public double brevity_penalty() {
	if (hyp_len > ref_len) return 1.0;
	return Math.exp(1-(ref_len / hyp_len));
    }

    public double precision() {	
	double sum = 0.0;
	for (int i = 1; i <= max_ngram; i++) {
	    double w = 1.0 / (double) max_ngram;
	    sum += w * Math.log(precision(i));
	}
	return Math.exp(sum);
    }

    public void set_count(int len, double clip, double unclip) {
	if ((len > 0) && (len <= part_clip.length) &&
	    (len <= part_unclip.length)) {
	    part_clip[len-1] = clip;
	    part_unclip[len-1] = unclip;
	}
    }

    public double precision(int len) {
	if ((len > 0) && (len <= max_ngram)) {
	    return part_clip[len-1] / part_unclip[len-1];
	} else {
	    return 0.0;
	}
    }

    public double score() {return brevity_penalty() * precision();}
}
