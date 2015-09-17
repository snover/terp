package com.bbn.mt.terp;

public class TERpara {
    private static Parameter _para = new Parameter();
    public static Parameter para() { return _para; }

    public static enum OPTIONS {
	REF, HYP, REFLEN,
	NORMALIZE, CASEON, NOPUNCTUATION, FORMATS, OUTPFX, BEAMWIDTH, 
	SHIFTDIST, SHIFTSIZE,
	DELETE_COST, INSERT_COST, SUBSTITUTE_COST, MATCH_COST, SHIFT_COST, 
	STEM_COST, SYN_COST, CAP_TER, 
	// PHRASE_TABLE, 
	ADJUST_PHRASETABLE_FUNC, ADJUST_PHRASETABLE_PARAMS, 
	ADJUST_PHRASETABLE_MIN, ADJUST_PHRASETABLE_MAX,
	USE_PORTER, USE_WORDNET, 
	//	MULTI_REF, 
	WORD_CLASS_FNAME,
	PARAM_FILE, FILTER_PHRASE_TABLE, DUMP_PHRASETABLE,
	SHIFT_CONSTRAINT, SHIFT_STOP_LIST,
	USE_AVE_LEN, SHOW_ALL_REFS, WORDNET_DB_DIR,
	WEIGHT_FILE, GENERALIZE_NUMBERS,
	NORM_NUMS, NORM_FILE, NEWNORM,
	NORM_PHRASE_TABLE, SUM_DUP_PHRASES,
	PHRASE_DB, IGNORE_MISSING_HYP,
	    IGNORE_SETID, USE_MULTI_WORDCLASS,
	    VERBOSE
    }
        
    static {
    	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.VERBOSE, 
		      "Verbose (boolean)", new Boolean(false));
    	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.NORMALIZE, 
		      "Normalize (boolean)", new Boolean(false));
    	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.NEWNORM, 
		      "New Normalization (boolean)", new Boolean(false));
    	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.USE_AVE_LEN, 
		      "Use Average Length (boolean)", new Boolean(true));
    	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.SHOW_ALL_REFS, 
		      "Show All References (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.CASEON, 
		      "Case Sensitive (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.NOPUNCTUATION, 
		      "Strip Punctuation (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.REF, 
		      "Reference File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.HYP, 
		      "Hypothesis File (filename)", "");	
	_para.add_opt(Parameter.OPT_TYPES.STRINGLIST, OPTIONS.FORMATS, 
		      "Output Formats (list)", Parameter.parse_stringlist("all"));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.OUTPFX, 
		      "Output Prefix (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.BEAMWIDTH, 
		      "Beam Width (integer)", new Integer(20));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.REFLEN, 
		      "Reference Length File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.SHIFTDIST, 
		      "Shift Distance (integer)", new Integer(50));
	_para.add_opt(Parameter.OPT_TYPES.INTEGER, OPTIONS.SHIFTSIZE, 
		      "Shift Size (integer)", new Integer(10));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.CAP_TER, 
		      "Cap Maximum TER (boolean)", new Boolean(false));
	//	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.PHRASE_TABLE, 
	//		      "Phrase Table (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.STRING, OPTIONS.ADJUST_PHRASETABLE_FUNC, 
		      "Adjust Phrase Table Func (string)", "");	
	_para.add_opt(Parameter.OPT_TYPES.DOUBLELIST, OPTIONS.ADJUST_PHRASETABLE_PARAMS, 
		      "Adjust Phrase Table Params (float list)", new double[0]);
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.ADJUST_PHRASETABLE_MIN, 
		      "Adjust Phrase Table Min (float)", Double.NEGATIVE_INFINITY);
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.ADJUST_PHRASETABLE_MAX, 
		      "Adjust Phrase Table Max (float)", Double.POSITIVE_INFINITY);
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.USE_PORTER, 
		      "Use Porter Stemming (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.USE_WORDNET, 
		      "Use WordNet Synonymy (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.WORDNET_DB_DIR, 
		      "WordNet Database Directory (filename)", "");
	//	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.MULTI_REF, 
	//	"Merge References (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.WORD_CLASS_FNAME, 
		      "Word Class File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.DELETE_COST, 
		      "Default Deletion Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.STEM_COST, 
		      "Default Stem Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.SYN_COST, 
		      "Default Synonym Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.INSERT_COST, 
		      "Default Insertion Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.SUBSTITUTE_COST, 
		      "Default Substitution Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.MATCH_COST, 
		      "Default Match Cost (float)", new Double(0.0));
	_para.add_opt(Parameter.OPT_TYPES.DOUBLE, OPTIONS.SHIFT_COST, 
		      "Default Shift Cost (float)", new Double(1.0));
	_para.add_opt(Parameter.OPT_TYPES.PARAMFILE, OPTIONS.PARAM_FILE, 
		      "", "");
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.FILTER_PHRASE_TABLE, 
		      "Filter Phrase Table (boolean)", new Boolean(true));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.DUMP_PHRASETABLE, 
		      "Dump Phrase Table (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.STRING, OPTIONS.SHIFT_CONSTRAINT, 
		      "Shift Constraint (string)", "exact");
	_para.add_opt(Parameter.OPT_TYPES.STRING, OPTIONS.SHIFT_STOP_LIST, 
		      "Shift Stop Word List (string)", "");
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.WEIGHT_FILE, 
		      "Weight File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.GENERALIZE_NUMBERS, 
		      "Generalize Numbers (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.NORM_NUMS, 
		      "Normalize Numbers (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.NORM_FILE, 
		      "Normalization File (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.NORM_PHRASE_TABLE, 
		      "Normalize Phrase Table (boolean)", new Boolean(false));	
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.SUM_DUP_PHRASES, 
		      "Sum Duplicate Phrases (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.FILENAME, OPTIONS.PHRASE_DB,
		      "Phrase Database (filename)", "");
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.IGNORE_MISSING_HYP,
		      "Ignore Missing Hypothesis (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.IGNORE_SETID,
		      "Ignore Test Set ID (boolean)", new Boolean(false));
	_para.add_opt(Parameter.OPT_TYPES.BOOLEAN, OPTIONS.USE_MULTI_WORDCLASS,
		      "Use Multi Word Class (boolean)", new Boolean(false));

	// Set command-line flags
	_para.addCmdBoolFlag(OPTIONS.NORMALIZE, 'N', "true");
	_para.addCmdBoolFlag(OPTIONS.CASEON, 's', "true");
	_para.addCmdBoolFlag(OPTIONS.CAP_TER, 'c', "true");
	_para.addCmdBoolFlag(OPTIONS.USE_PORTER, 't', "true");
	_para.addCmdBoolFlag(OPTIONS.VERBOSE, 'v', "true");
	_para.addCmdBoolFlag(OPTIONS.IGNORE_MISSING_HYP, 'm', "true");

	_para.addCmdArgFlag(OPTIONS.REF, 'r');
	_para.addCmdArgFlag(OPTIONS.HYP, 'h');
	_para.addCmdArgFlag(OPTIONS.WORD_CLASS_FNAME, 'w');
	_para.addCmdArgFlag(OPTIONS.WEIGHT_FILE, 'W');
	_para.addCmdArgFlag(OPTIONS.FORMATS, 'o');
	_para.addCmdArgFlag(OPTIONS.OUTPFX, 'n');
	_para.addCmdArgFlag(OPTIONS.BEAMWIDTH, 'b');
	_para.addCmdArgFlag(OPTIONS.REFLEN, 'a');
	_para.addCmdArgFlag(OPTIONS.PARAM_FILE, 'p');
	_para.addCmdArgFlag(OPTIONS.WORDNET_DB_DIR, 'd');
	_para.addCmdArgFlag(OPTIONS.SHIFT_STOP_LIST, 'S');
	_para.addCmdArgFlag(OPTIONS.PHRASE_DB, 'P');
    }


    static {
	String[] usage_ar = 
	    {"java -jar terp.jar [-p parameter_file] -r <ref_file> -h <hyp_file> [-Nstcvm] ",
	     " [-a alter_ref] [-b beam_width] [-o out_format -n out_pefix] [-w word_cost_file]",
	     " [-P phrase_table_db] [-W weight_file] [-d WordNet_dict_dir] [-S shift_stopword_list]",
	     " [ parameter_file1 parameter_file2 ... ]",
	     " ---------------------------------------------------------------------------------",
	     "  -r <ref-file> (required field if not specified in parameter file)", 
	     "    reference file in either TRANS or SGML format",
	     "  -h <hyp-file> (required field if not specified in parameter file)",
	     "    hypothesis file to score in either TRANS or SGML format",
	     "  -p <parameter_file>",
	     "    specifies parameters.  Command line arguments after the -p will override values",
	     "    in the parameter file",
	     "    Command line arguments before the -p will be overriden by values in parameter file",
	     "    parameter file for this run can be output by specifying 'param' as an output format",
	     "    many parameters can only be set using a parameter",
	     "    Any additional arguments to TERp will be treated as parameter files and evaluated",
	     "    after other command line arguments.",
	     "  -N", "    Normalize and Tokenize ref and hyp",
	     "  -s", "    use case sensitivity",
	     "  -c", "    cap ter at 100%",
	     "  -t", "    use porter stemmer to determine shift equivilence",
	     "  -v", "    use verbose output when running",
	     "  -m", "    ignore missing hypothesis segments (useful when doing parallelization)",
	     "  -a <alter-ref>",
	     "    reference file, in either TRANS or SGML format, to use for calculating number of words in reference",
	     "  -b <beam-width>",
	     "    beam width to use for min edit distance calculations",
	     "  -o <out_format>",
	     "    set output formats:  all,sum,pra,xml,ter,sum_nbest,nist,html,param,weights,counts,align,align_detail",
	     "  -n <out_prefix",
	     "    set prefix for output files",
	     "  -P <phrase_table_db>",
	     "    directory that contains phrase table database",
	     "  -W <weight_file>",
	     "    file that contains edit weights.",
	     "  -d <WordNet_dictionary_dir>",
	     "    set the path to the WordNet Dictionary Directory (of the form /opt/WordNet-3.0/dict/)",
	     "  -S <shift-stop-word-list>",
	     "    specify a file that contains a list of words that cannot be shifted without a non-stop word",
	     "\nUsage for phrase table adjustment functions:",
	     PhraseTable.valid_adjust_funcs(),
	    };
	_para.set_usage_statement(TERalignment.join("\n", usage_ar), true);	
    }
    
  public static void getOpts(String[] args) {
      String[] excess = _para.parse_args(args, true);

      for (String fn : excess) {
	  System.out.println("Loading " + fn + " as parameter file");
	  _para.readPara(fn);
      }

      if(_para.get_string(OPTIONS.REF).equals("") || _para.get_string(OPTIONS.HYP).equals("")) {
	  System.out.println("** Please specify both reference and hypothesis inputs");
	  _para.usage();
      }
      return;
  }
}
