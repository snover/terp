
package com.bbn.mt.terp;

import java.util.Arrays;

public class Statistics {
     public static double sum(double[] vals) {
	double t = 0.0;
	for (int i = 0; i < vals.length; i++) t += vals[i];
	return t;
    }

    public static double mean(double[] vals) {
	if (vals.length == 0) return 0.0;
	return sum(vals) / vals.length;
    }

    public static double weighted_mean(double[] vals, double[] wgt) {
 	if (vals.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted mean: " + vals.length + " vs " + wgt.length);
	}

	double totwgt = 0.0;
	double totval = 0.0;
	for (int i = 0; i < vals.length; i++) {
	    totwgt += wgt[i];
	    totval += wgt[i] * vals[i];
	}
	return totval / totwgt;
    }
    
    public static double variance(double[] vals) { return variance(vals, mean(vals)); }
    private static double variance(double[] vals, double mean) {
	double tot = 0.0;
	for (int i = 0; i < vals.length; i++) {
	    tot += ((vals[i] - mean) * (vals[i] - mean));
	}
	return tot / vals.length;
    }           
    public static double weighted_variance(double[] vals, double[] wgt) {
	return weighted_variance(vals, wgt, weighted_mean(vals, wgt));
    }
   
    private static double weighted_variance(double[] vals, double[] wgt, double mean) {
 	if (vals.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted variance: " + vals.length + " vs " + wgt.length);
	}
	double totval = 0.0;
	double totwgt = 0.0;
	for (int i = 0; i < vals.length; i++) {
	    totwgt += wgt[i];
	    totval += (wgt[i] * ((vals[i] - mean) * (vals[i] - mean)));
	}
	return totval / totwgt;
    }

    public static double std_dev(double[] vals) {
	return Math.sqrt(variance(vals));
    }
    private static double std_dev(double[] vals, double mean) {
	return Math.sqrt(variance(vals, mean));
    }

    public static double weighted_std_dev(double[] vals, double[] wgt) { 
 	if (vals.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted std dev: " + vals.length + " vs " + wgt.length);
	}
	return Math.sqrt(weighted_variance(vals, wgt));
    }

    private static double weighted_std_dev(double[] vals, double[] wgt, double mean) { 
 	if (vals.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted std dev: " + vals.length + " vs " + wgt.length);
	}
	return Math.sqrt(weighted_variance(vals, wgt, mean));
    }
    
    public static double[] weighted_zscores(double[] vals, double[] wgt) {
 	if (vals.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted zscores: " + vals.length + " vs " + wgt.length);
	}
	double z[] = new double[vals.length];
	double mean = weighted_mean(vals, wgt);
	double std = weighted_std_dev(vals, wgt, mean);
	if (std == 0) {return z;}
	for (int i = 0; i < vals.length; i++)
	    z[i] = ((vals[i] - mean) / std);
	return z;
    }
    public static double[] zscores(double[] vals) {
	double[] z = new double[vals.length];
	double mean = mean(vals);
	double std = std_dev(vals, mean);
	if (std == 0) {return z;}
	for (int i = 0; i < vals.length; i++)
	    z[i] = ((vals[i] - mean) / std);
	return z;
    }

    private static double[] scores_to_ranks(double[] vals) {
	double[] sorted = new double[vals.length];
	double[] ranks =  new double[vals.length];
	for (int i = 0; i < vals.length; i++) {
	    sorted[i] = vals[i];
	    ranks[i] = 1;
	}

	Arrays.sort(sorted);
	for (int s = 0; s < sorted.length; s++) {
	    for (int i = 0; i < vals.length; i++) {
		if (vals[i] >= sorted[s]) {
		    ranks[i] = s+1;
		}
	    }
	}
	return ranks;	
    }

    public static double weighted_spearman(double[] vals1, double[] vals2, double[] wgt) {
	return weighted_pearson(scores_to_ranks(vals1), scores_to_ranks(vals2), wgt);
    }
    public static double spearman(double[] vals1, double[] vals2) {
	return pearson(scores_to_ranks(vals1), scores_to_ranks(vals2));
    }

    public static double weighted_pearson(double[] vals1, double[] vals2, double[] wgt) {
 	if (vals1.length != vals2.length) {
	    System.err.println("Error.  Differing array lengths in weighted pearson: " + vals1.length + " vs " + vals2.length);
	}
 	if (vals1.length != wgt.length) {
	    System.err.println("Error.  Differing array lengths in weighted pearson: " + vals1.length + " vs " + wgt.length);
	}

	boolean any_nz = false;
	for (int i = 0; (i < vals1.length) && (! any_nz); i++) {
	    if (vals1[i] != 0) any_nz = true;	    
	}
	if (! any_nz) return 0.0;
 	any_nz = false;
	for (int i = 0; (i < vals2.length) && (! any_nz); i++) {
	    if (vals2[i] != 0) any_nz = true;	    
	}
	if (! any_nz) return 0.0;

	double[] z1 = weighted_zscores(vals1, wgt); 
	double[] z2 = weighted_zscores(vals2, wgt);
	double N = sum(wgt);
	double r = 0.0;
	for(int i = 0; i < vals1.length; i++) { 
	    r += (wgt[i] * z1[i] * z2[i]);
	}
	return r / N;
    }

    public static double pearson(double[] vals1, double[] vals2) {
 	if (vals1.length != vals2.length) {
	    System.err.println("Error.  Differing array lengths in pearson: " + vals2.length + " vs " + vals2.length);
	}
	boolean any_nz = false;
	for (int i = 0; (i < vals1.length) && (! any_nz); i++) {
	    if (vals1[i] != 0) any_nz = true;	    
	}
	if (! any_nz) return 0.0;
 	any_nz = false;
	for (int i = 0; (i < vals2.length) && (! any_nz); i++) {
	    if (vals2[i] != 0) any_nz = true;	    
	}
	if (! any_nz) return 0.0;

	double[] z1 = zscores(vals1); 
	double[] z2 = zscores(vals2);
	double r = 0.0;
	for(int i = 0; i < vals1.length; i++) { 
	    r += (z1[i] * z2[i]);
	}
	return r / vals1.length;
    }

//     public static double pearson_cor(double[] vals1, double[] vals2) {
// 	// Compute Means
// 	if (vals1.length != vals2.length) {
// 	    System.err.println("Error.  Differing array lengths in pearson: " + tvals.length + " vs " + cur.length);
// 	}
// 	double v1_mu = mean(vals1);
// 	double v2_mu = mean(vals2);
// 	double v1_var = variance(vals1, v1_mu);
// 	double v1_stddev = Math.sqrt(v1_var);
// 	double r = 0.0;
// 	for (int i = 0; i < vals1.length; i++) {
// 	    r += ((vals[i] - v1_mu) * (vals2[i] - v2_mu)); 
// 	}
// 	r /= ((vals1.length - 1) * v1_stddev * v2_stddev);
	
// 	return Math.abs(r);
//     }
}
    
