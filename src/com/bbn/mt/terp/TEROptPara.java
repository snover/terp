package com.bbn.mt.terp;

public class TEROptPara {    
    private static Parameter _para = new Parameter();
    public static Parameter para() { return _para; }

    public static enum OPTIONS {
	TERP_PARAM, HJ_FILE, SEGLEN_FILE, 
	OPT_FUNC, HJ_TYPE,
	INIT_COUNT_FILE, NUM_ITER, NUM_STEPS,
	OUTPUT_WEIGHTS_FILE, RUN_TERP, INIT_WEIGHTS_FILE, 
	MIN_WGTS, MAX_WGTS, CHANGE_FLAGS, 
	PERTURB_SEED, PERTURB_RANGE, 
	OUTPUT_MULTI_PERTURBS, OUTPUT_MULTI_PERTURB_PFX,
	NUM_RAND_SEARCH_STEPS, NUM_SEARCH_START
    }

    static {
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.TERP_PARAM, 
		      "TERp Parameter File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.HJ_FILE, 
		      "Human Judgments File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.STRING, OPTIONS.HJ_TYPE,
		      "Human Judgment Type (string)", "score");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.SEGLEN_FILE, 
		      "Segment Length File (filename)", "");
 	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.INIT_COUNT_FILE, 
		      "Initial Counts File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.INIT_WEIGHTS_FILE, 
		      "Initial Weights File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.OUTPUT_WEIGHTS_FILE, 
		      "Output Weights File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.STRING, OPTIONS.OPT_FUNC, 
		      "Optimization Function (string)", "");
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.NUM_ITER, 
		      "Number of Iterations (integer)", new Integer(20));
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.NUM_STEPS, 
		      "Number of Steps (integer)", new Integer(5));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.RUN_TERP, 
		      "Run TERp (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.PERTURB_SEED, 
		      "Perturb Seed (integer)", new Integer(0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLELIST, OPTIONS.MIN_WGTS, 
		      "Minimum Weights (double list)", null);
	_para.add_opt(Parameter.OPT_TYPES.DOUBLELIST, OPTIONS.MAX_WGTS, 
		      "Maximum Weights (double list)", null);
	_para.add_opt(Parameter.OPT_TYPES.DOUBLELIST, OPTIONS.PERTURB_RANGE, 
		      "Perturb Range (double list)", null);
	_para.add_opt(Parameter.OPT_TYPES.INTLIST, OPTIONS.CHANGE_FLAGS, 
		      "Change Flags (int list)", null);
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.OUTPUT_MULTI_PERTURBS, 
		      "Output Multiple Perturbs (integer)", new Integer(0));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.OUTPUT_MULTI_PERTURB_PFX, 
		      "Multiple Perturb Prefix (filename)", null);
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.NUM_RAND_SEARCH_STEPS,
		      "Number of Random Search Steps (integer)", new Integer(0));
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.NUM_SEARCH_START,
		      "Number of Search Starting Points (integer)", new Integer(1));
    }
       
}
