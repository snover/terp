package com.bbn.mt.terp;

public class TERnumcost extends TERcost {
    //    public Comparable[] process_input_hyp(String[] in) { return process_input(in); }
    //    public Comparable[] process_input_ref(String[] in) { return process_input(in); }
    protected Comparable[] process_input(String[] in) {
	NumberGen[] nin = new NumberGen[in.length];
	for (int i = 0; i < in.length; i++) {
	    nin[i] = new NumberGen(in[i]);
	}
	return (Comparable[]) nin;
    }

    static {
	COST_NAME = "Generalized Number TERcost";
    }
}
