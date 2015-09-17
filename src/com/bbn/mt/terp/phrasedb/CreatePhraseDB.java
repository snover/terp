package com.bbn.mt.terp.phrasedb;

import java.io.*;
import java.util.Set;

public class CreatePhraseDB {
    public static void main(String[] argv) {
	if (argv.length != 2) {
	    System.err.println("usage: CreatePhraseDB <text_file> <db>");
	    System.exit(-1);
	}
	String command = "convert";
	String db = argv[0];

	if (command.equals("read")) {
	    read(db);
	} else if (command.equals("convert")) {	    
	    String ptfn = argv[0];
	    db = argv[1];
	    System.out.println("Converting Phrase Table from " + ptfn);
	    System.out.println("Storing Database in " + db);
	    convert(db, ptfn);
	} else if (command.equals("test")) {
	    if (argv.length != 3) {
		System.err.println("Command 'test' requires an arg");
		System.exit(-1);
	    }
	    String tfn = argv[2];
	    System.out.println("Testing text from " + tfn);
	    test(db, tfn);
	} else {
	    System.err.println("Invalid command: " + command + "\n" +
			       " valid commands are: 'read', 'test', or 'convert'\n");
	    System.exit(-1);
	}
	return;
    }

    public static void read(String fn) {
	PhraseDB ph = new PhraseDB(fn);
	ph.dump_pt_file(System.out);
    }
    
    public static void convert(String db, String fn) {
	PhraseDB pdb = new PhraseDB(db);
	LoadPhraseTable.load(pdb, fn, 0);
	System.out.println("Done adding phrases to " + db);
    }

    public static void test(String db, String tfn) {
	PhraseDB ph = new PhraseDB(db);
	ph.openDB();
	try {
	    BufferedReader bfrd = new BufferedReader(new FileReader(tfn));
	    String l1;
	    String l2;
	    while (((l1 = bfrd.readLine()) != null) &&
		   ((l2 = bfrd.readLine()) != null)) {
		l1 = l1.trim();
		l2 = l2.trim();
		String[] l1ar = l1.split("\\s+");
		String[] l2ar = l2.split("\\s+");
		System.out.println("Ref: " + l1);
		System.out.println("Hyp: " + l2);
		Set<PhraseEntry> pes = ph.get_phrases(l1ar, l2ar);
		for (PhraseEntry pe : pes)
		    System.out.println("  " + pe);
	    }
	    bfrd.close();
	} catch (IOException ioe) {
	    ph.closeDB();
	    System.err.println("Error in test. " + ioe.toString());
	    System.exit(-1);
	}
	ph.closeDB();
    }
    
    
}

