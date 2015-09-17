package com.bbn.mt.terp;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class BLEU {
    private int max_ngram = 4;
    private PhraseTable pt = null;

    public BLEU() {}

    public BLEU(int max_ngram) {
	this.max_ngram = max_ngram;
    }

    public BLEU(int max_ngram, PhraseTable pt) {
	this.max_ngram = max_ngram;
	this.pt = pt;
    }

    public int max_ngram_size() { return this.max_ngram; }

    public BLEUalignment calc(String hyp, String[] refs) {
	// Normalize
	String[] hyparr = NormalizeText.process(hyp);
	String[][] refarrs = new String[refs.length][];
	for (int i = 0; i < refs.length; i++)
	    refarrs[i] = NormalizeText.process(refs[i]);
	return calc(hyparr, refarrs);
    }

    public BLEUalignment calc(String[] hyp, String[][] refs) {
	BLEUalignment tr = new BLEUalignment(this);
	tr.hyp = hyp;
	tr.refs = refs;	

	List<String> hyp_ls = Arrays.asList(hyp);	
	List< List<String>> ref_lsar = new ArrayList<List<String>>(refs.length);
	
	for (int i = 0; i < refs.length; i++)
	    ref_lsar.set(i, Arrays.asList(refs[i]));

	tr.init();
	for (int l = 0; l < max_ngram; l++) {
	    HashMap< List<String>, Double > HypCounts = count_ngram(hyp_ls, l);
	    HashMap< List<String>, Double > RefCounts = count_ngram_m(ref_lsar, l);

	    HashMap< List<String>, Double > ClipCounts = new HashMap();
	    double clip_sum = 0.0;
	    double unclip_sum = 0.0;
	    for (List<String> ls : HypCounts.keySet()) {
		double hyp_cnt = HypCounts.get(ls);
		unclip_sum += hyp_cnt;
		if (RefCounts.containsKey(ls)) {
		    double clip_cnt = RefCounts.get(ls);
		    if (clip_cnt > hyp_cnt) {
			clip_cnt = hyp_cnt;
		    }		    
		    clip_sum += clip_cnt;
		    ClipCounts.put(ls, clip_cnt);		    
		}
	    }
	    tr.set_count(l+1, clip_sum, unclip_sum);

	    for (int i = 0; i < (hyp_ls.size()-l); i++) {
		List<String> sl = hyp_ls.subList(i, i+l+1);
		Double cct = ClipCounts.get(sl);
		Double hct = HypCounts.get(sl);
		if (cct != null) {
		    double cnt = cct.doubleValue() / hct.doubleValue();		    
		    for (int j = 0; j <= l; j++) {
			tr.coverage_cnt[i+j] = cnt;
			tr.coverage_len[i+j] = l+1;
		    }
		}
	    }	    
	}	
	return tr;
    }
    
   
    private HashMap< List<String>, Double > count_ngram(List<String> sen, int len) {
	HashMap< List<String>, Double > cnts = new HashMap();
	for (int i = 0; i < (sen.size()-len); i++) {
	    List<String> sl = sen.subList(i, i+len+1);
	    Double ct = cnts.get(sl);
	    if (ct == null) { ct = 0.0; }
	    cnts.put(sl, ct+1.0);
	}
	return cnts;
    }	          

    private HashMap< List<String>, Double > count_ngram_m(List<List<String>> sens, int len) {
	HashMap< List<String>, Double > cnts = null;
	for (int i = 0; i < sens.size(); i++) {
	    HashMap< List<String>, Double> rcnt = count_ngram(sens.get(i), len);
	    if (cnts == null) { 
		cnts = rcnt; 
	    } else {
		for (List<String> ls : rcnt.keySet()) {
		    double r_num = rcnt.get(ls);
		    if (cnts.containsKey(ls)) {
			double m_num = cnts.get(ls);
			if (r_num > m_num) 
			    cnts.put(ls, m_num);
		    } else {
			cnts.put(ls, r_num);
		    }
		}
	    }
	}
	return cnts;
    }	          
}
