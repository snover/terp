package com.bbn.mt.terp.phrasedb;

import java.util.regex.*;
import java.io.*;
import java.util.Arrays;

public class LoadPhraseTable {
    private LoadPhraseTable() {}

    private static final Pattern iPat = 
	Pattern.compile("^([0-9.eE\\-]+)\\s+\\<p\\>(.*)\\<\\/p\\>\\s+\\<p\\>(.*)\\<\\/p\\>$");
    private static final Pattern iPats = 
	Pattern.compile("^([0-9.eE\\-]+)\\s+([0-9.eE\\-]+)\\s+\\<p\\>(.*)\\<\\/p\\>\\s+\\<p\\>(.*)\\<\\/p\\>$");
   
    public static int load(PhraseDB db, String fn, int sourceId) {
	return load(db,fn,sourceId,false);
    }

    public static int load(PhraseDB db, String fn, int sourceId, boolean norm_phrases) {
	db.openDBwrite();

	BufferedReader infh;
	int c = 0;
	int tc = 0;
        try {
            infh = new BufferedReader(new FileReader(fn));
        } catch (IOException ioe) {
	    db.closeDB();	
            System.err.println("LoadPhraseTable pt=" + fn + ": " + ioe);
	    System.exit(-1);
	    return 0;
        }
	try {	    
            String line;
            while ((line = infh.readLine()) != null) {
		line = line.trim();
		tc++; c++;
		if (c == 10000) {
		    System.out.println("Processed " + tc + " lines from " + fn);
		    c = 0;
		}

		String p1 = null;
		String p2 = null;
		boolean tryrev = false;
		double d1 = 0.0;
		double d2 = 0.0;

		Matcher im = iPat.matcher(line);
		if (im.matches()) {
		    d1 = Double.valueOf(im.group(1));
		    p1 = im.group(2);
		    p2 = im.group(3);
		} else {
		    Matcher im2 = iPats.matcher(line);
		    if (im2.matches()) {
			d1 = Double.valueOf(im2.group(1));
			d2 = Double.valueOf(im2.group(2));
			p1 = im2.group(3);
			p2 = im2.group(4);
			tryrev = true;
		    } else {
			continue;
		    }
		}	
			
		if ((p1 == null) || (p2 == null)) continue;

		p1 = p1.trim();
		p2 = p2.trim();
		String[] seq_a = null;
		String[] seq_b = null;
		//		    if (norm_phrases) {
		//			seq_a = NormalizeText.process(p1);
		//			seq_b = NormalizeText.process(p2);
		//		      } else {
		seq_a = p1.split("\\s+");
		seq_b = p2.split("\\s+");		
		//		    }
		
		boolean basic_ok = true;
		if ((seq_a.length <= 0) || (seq_b.length <= 0) 
		    || (Arrays.equals(seq_a, seq_b)))
		    continue;

		db.add(seq_a, seq_b, d1, sourceId);
		if (tryrev) db.add(seq_a, seq_b, d2, sourceId);
	    }
	    infh.close();
	} catch(IOException ioe) {
	    db.closeDB();	
	    System.err.println("LoadPhraseTable load from " + fn + 
			       ": " + ioe);
	    System.exit(-1);
	}
	db.closeDB();	
	return tc;
    }
}