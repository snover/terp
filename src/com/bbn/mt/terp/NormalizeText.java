package com.bbn.mt.terp;

import java.io.*;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

public class NormalizeText {
    private static boolean remove_punc = false;
    private static boolean lower_case = true;
    private static boolean std_normalize = false;
    private static boolean number_normalize = false;
    private static boolean new_norm = false;
    private static final ArrayList equivStrings = new ArrayList(200);
    private static final HashMap equivlookup = new HashMap(500);
    private static final Pattern isNumpat = Pattern.compile("^[0-9]+$");
    private static final Pattern equivpat = Pattern.compile("^\\s*\\<p\\>(.*)<\\/p\\>\\s*$");
    private static final HashMap NumHash = new HashMap(200);

    private static boolean _initialized = false;

    private static int num_processed = 0;

    public static void setRemPunc(boolean b) { remove_punc = b; }
    public static void setLowerCase(boolean b) { lower_case = b; }
    public static void setStdNormalize(boolean b) { std_normalize = b; }
    public static void setNumberNormalize(boolean b) { number_normalize = b; }
    public static void setNewNorm(boolean b) { new_norm = b; }

    public static String get_info() {
	String s = ("Text Normalization\n" +
		    "  Remove Punctuation:      " + remove_punc + "\n" +
		    "  Lower Case Text:         " + lower_case + "\n" + 
		    "  Standard Normalization:  " + std_normalize + "\n" +
		    "  New Normalization:       " + new_norm + "\n" +
		    "  Normalize Numbers:       " + number_normalize + "\n" +
		    "  Number of Equiv Strings: " + equivStrings.size() + "\n" + 
		    "  Num Strings Processed:   " + num_processed + "\n");       
	return s;
    }

    public static void reinit() {
	_initialized = false;
	equivStrings.clear();
	init();
    }

    public static void init() {
	if (! _initialized) {
	    setStdNormalize((Boolean) TERpara.para().get(TERpara.OPTIONS.NORMALIZE));
	    setNewNorm((Boolean) TERpara.para().get(TERpara.OPTIONS.NEWNORM));
	    setLowerCase(! (Boolean) TERpara.para().get(TERpara.OPTIONS.CASEON));
	    setRemPunc((Boolean) TERpara.para().get(TERpara.OPTIONS.NOPUNCTUATION));
	    setNumberNormalize((Boolean) TERpara.para().get(TERpara.OPTIONS.NORM_NUMS));
	    load_norm_file((String) TERpara.para().get(TERpara.OPTIONS.NORM_FILE));
	    _initialized = true;
	}
    }

    public static void load_norm_file(String fname) {
	if ((fname == null) || fname.equals("")) return;
	System.out.println("Loading normalization equivalences from " + fname);
	int num_sets = 0;
	int num_words = 0;
	try {
	    BufferedReader fh = new BufferedReader(new FileReader(fname));
	    String line;
	    while ((line = fh.readLine()) != null) {
		line = line.trim();
		if (line.length() == 0) continue;
		Matcher im = equivpat.matcher(line);
		if (im.matches()) {
		    String [] phrases = im.group(1).split("\\<\\/p\\>\\s+\\<p\\>");
		    if (phrases.length > 1) {
			String[][] ta = new String[phrases.length][];
			for (int i = 0; i < phrases.length; i++) {
			    String[] psplt = phrases[i].split("\\s+");
			    for (int j = 0; j < psplt.length; j++) {
				psplt[j] = psplt[j].intern();
			    }
			    ta[i] = psplt;
			}
			for (int i = 1; i < ta.length; i++) {
			    for (int j = 0; j < ta[i].length; j++) {
				ArrayList al = (ArrayList) equivlookup.get(ta[i][j]);
				if (al == null) {
				    al = new ArrayList(3);
				    al.add(equivStrings.size());
				    equivlookup.put(ta[i][j], al);
				} else {
				    int last = (Integer) al.get(al.size() - 1);
				    if (last != equivStrings.size()) {
					al.add(equivStrings.size());
				    }
				}
			    }
			}
			num_sets++;
			num_words += phrases.length;			
			equivStrings.add(ta);
		    }
		} else {
		    System.err.println("Invalid line in " + fname + ": " + line);
		    System.exit(-1);
		}
	    }
	    fh.close();
	} catch (IOException ioe) {
	    System.out.println("Loading normalization file from " + 
			       fname + ": " + ioe);
	    System.exit(-1);
	}

	System.out.println("  " + num_sets + " sets loaded with a total of " + num_words + " words");
	return;
    }

    public static String[] process(String s) {
	num_processed++;

	s = simple_norm(s);

	if (lower_case) s = s.toLowerCase();
	if (std_normalize) s = standard_normalize(s);
	if (new_norm) s = newnorm(s);
	if(remove_punc) s = removePunctuation(s);
	
	s = simple_norm(s);

	String[] tr = s.split("\\s+");
	if(number_normalize) {
	    for (int i = 0; i < tr.length; i++)
		tr[i] = parseNumber(tr[i]);
	}

	if (equivStrings.size() > 0) tr = norm_equivstrings(tr);
	
	int numgood = 0;
	for (int i = 0; i < tr.length; i++) {
	    if (! (tr[i].equals(""))) numgood++;	    
	}

	if (numgood == 0) {
	    return new String[0];
	}

	if (numgood != tr.length) {
	    String[] ntr = tr;
	    ntr = new String[numgood];	   
	    int cur = 0;
	    for (int i = 0; i < tr.length; i++) {
		if (! (tr[i].equals(""))) ntr[cur++] = tr[i];
	    }
	    tr = ntr;
	}

	for (int i = 0; i < tr.length; i++) {
	    tr[i] = tr[i].intern();
	}

	

	return tr;
    }

    private static void find_poss_equiv(String[] s, TreeSet eqvind, int minind) {
	if (eqvind == null) { eqvind = new TreeSet(); }
	for (int i = 0; i < s.length; i++) {
	    ArrayList al = (ArrayList) equivlookup.get(s[i]);
	    if (al != null) {
		for (int j = 0; j < al.size(); j++) {
		    int ind = (Integer) al.get(j);
		    if ((ind > minind) && (! eqvind.contains(ind))) {
			eqvind.add(ind);
			String[][] eqv = (String[][]) equivStrings.get(ind);
			find_poss_equiv(eqv[0], eqvind, ind);
		    }
		}
	    }
	}
	return;
    }
    private static TreeSet find_poss_equiv(String[] s) {
	TreeSet tr = new TreeSet();
	find_poss_equiv(s, tr, -1);
	return tr;
    }

    public static String[] norm_equivstrings(String[] s) {
	TreeSet eqvtc = find_poss_equiv(s);
	Iterator it = eqvtc.iterator();
	while (it.hasNext()) {
	    int eqind = (Integer) it.next();
	    String[][] eqv = (String[][]) equivStrings.get(eqind);
	    String[] to = eqv[0];
	    for (int j = 1; j < eqv.length; j++) {
		String[] from = eqv[j];
		s = replace_str(s, from, to);
	    }
	}
	return s;
    }

    private static String[] replace_str(String[] ps, String[] from, String[] to) {
	for (int i = 0; i < (ps.length-(from.length-1)); i++) {
	    boolean ok = true;
	    for (int j = 0; ok && (j < from.length); j++) {
		if (! (ps[i+j].equals(from[j]))) ok = false;
	    }
	    if (ok) {
		if (from.length != to.length) {
		    String nps[] = new String[(ps.length + to.length) - from.length];
		    for (int j = 0; j < i; j++) nps[j] = ps[j];
		    for (int j = 0; j < to.length; j++) nps[i+j] = to[j];
		    int startps = (i + from.length);
		    int startnps = (i + to.length);
		    for (int j = 0; j < (ps.length - startps); j++) {
			nps[j+startnps] = ps[j+startps];
		    }
		    ps = nps;
		} else {
		    for (int j = 0; j < to.length; j++) {
			ps[i+j] = to[j];			
		    }
		}
		i += (to.length - 1);
	    }
	}
	return ps;
    }

    public static String simple_norm(String s) {
	s = s.trim();
	s = s.replaceAll("\\s+"," "); // one space only between words
	return s;
    }
    
    public static String newnorm(String s) {
	s = s.replaceAll("\\B-\\B", " ");
	s = s.replaceAll("'s ", " 's "); // handle possesives
	s = s.replaceAll("'s$", " 's"); // handle possesives
	s = s.replaceAll("\\Bs' ", "s 's "); // handle possesives
	s = s.replaceAll("\\Bs'$", "s 's"); // handle possesives
	return s;
    }
    
    public static String standard_normalize(String s) {
      // language-independent part:
      s = s.replaceAll("<skipped>", ""); // strip "skipped" tags
      s = s.replaceAll("-\n", ""); // strip end-of-line hyphenation and join lines
      s = s.replaceAll("\n", " "); // join lines
      s = s.replaceAll("&quot;", "\""); // convert SGML tag for quote to " 
      s = s.replaceAll("&amp;", "&"); // convert SGML tag for ampersand to &
      s = s.replaceAll("&lt;", "<"); // convert SGML tag for less-than to >
      s = s.replaceAll("&gt;", ">"); // convert SGML tag for greater-than to <
	    
      // language-dependent part (assuming Western languages):
      s = " " + s + " ";
      s = s.replaceAll("([\\{-\\~\\[-\\` -\\&\\(-\\+\\:-\\@\\/])", " $1 ");   // tokenize punctuation
      s = s.replaceAll("'s ", " 's "); // handle possesives
      s = s.replaceAll("'s$", " 's"); // handle possesives

      s = s.replaceAll("\\s([^,\\. \\t]+)([\\.,])\\s", "$1 $2 "); // tokenize period and comma at end of word, unless there is an earlier period or comma
      //      s = s.replaceAll("\\s([^,\\. \\t]+)([\\.,])$", "$1 $2"); // tokenize period and comma at end of word, unless there is an earlier period or comma
      //      s = s.replaceAll("^([^,\\. \\t]+)([\\.,])\\s", "$1 $2 "); // tokenize period and comma at end of word, unless there is an earlier period or comma
      //      s = s.replaceAll("^([^,\\. \\t]+)([\\.,])$", "$1 $2"); // tokenize period and comma at end of word, unless there is an earlier period or comma

      //      s = s.replaceAll("([^0-9])([\\.,])", "$1 $2 "); // tokenize period and comma unless preceded by a digit
      //      s = s.replaceAll("([\\.,])([^0-9])", " $1 $2"); // tokenize period and comma unless followed by a digit
      s = s.replaceAll("([0-9])(-)", "$1 $2 "); // tokenize dash when preceded by a digit


      s = simple_norm(s);

      return s;
    }

    private static String removePunctuation(String str) {
	String s = str.replaceAll("[\\.,\\?:;!\"\\(\\)]", "");
	s = s.replaceAll("\\s+", " ");
	return s;
    }

    public static boolean isNum(String wd) {
	Matcher np = isNumpat.matcher(wd);
	return np.matches();
    }
    
    public static String parseNumber(String wd) {
	String pn = wd;
	pn = pn.replaceAll("[\\-,.$%]", "");
	if (isNum(pn)) return pn;
	String n = (String) NumHash.get(pn.toLowerCase());
	if (n != null) return n;
	return wd;
    }

    static {
	NumHash.put("zero", "0");
	NumHash.put("one", "1");
	NumHash.put("two", "2");
	NumHash.put("three", "3");
	NumHash.put("four", "4");
	NumHash.put("five", "5");
	NumHash.put("six", "6");
	NumHash.put("seven", "7");
	NumHash.put("eight", "8");
	NumHash.put("nine", "9");
	NumHash.put("ten", "10");
	NumHash.put("eleven", "11");
	NumHash.put("twelve", "12");
	NumHash.put("thirteen", "13");
	NumHash.put("fourteen", "14");
	NumHash.put("fifteen", "15");
	NumHash.put("sixteen", "16");
	NumHash.put("seventeen", "17");
	NumHash.put("eighteen", "18");
	NumHash.put("nineteen", "19");
	NumHash.put("twenty", "20");
	NumHash.put("thirty", "30");
	NumHash.put("forty", "40");
	NumHash.put("fifty", "50");
	NumHash.put("sixty", "60");
	NumHash.put("seventy", "70");
	NumHash.put("eighty", "80");
	NumHash.put("ninety", "90");
	NumHash.put("hundred",  "100");
	NumHash.put("thousand", "1000");
	NumHash.put("million",  "1000000");
	NumHash.put("billion",  "1000000000");	    
    }
}
