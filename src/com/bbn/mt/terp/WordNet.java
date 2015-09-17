package com.bbn.mt.terp;

// package bbn.mt.ster;

import java.util.*;
import java.io.*;

/** The static interface to WordNet.  Included so we can try different Java WordNet 
 *  interfaces if desired, without disturbing client code. */
public abstract class WordNet
{

	/** The cache to keep previously-computed values in.  Not sure we need this! */
	private static final Cache cache = new Cache();
	
	private static final Cache pseudoSynonyms = new Cache();
	
	/** The instance of the concrete sub-class that does the interfacing. */
	private static Wrapper wrapper;

	
	/** Returns the wrapper, setting if it hasn't already been set. */
	private static Wrapper getWrapper ()
	{
		if (wrapper == null) {
			if (WORDNET_DB_DIR != null)
			    wrapper = new WordNetJAWS(WORDNET_DB_DIR);
			else
			    wrapper = new EmptyWrapper();
		}
		return wrapper;
	}
	
	public static void clearCache ()
	{
		cache.clear();
	}
	
	public static void markAsSynonyms (String word1, String word2)
	{
		pseudoSynonyms.setValue(word1, word2, true);
	}
	
	/** Returns true if the argument words are synonymous. */
	private static boolean areSynonyms0 (String word1, String word2)
	{
		if (pseudoSynonyms.isTrue(word1,word2))
			return true;
		else
			return getWrapper().areSynonyms(word1, word2);
	}
	
	/** Returns true if the argument words are synonymous. */
    public static boolean areSynonyms (Comparable word1, Comparable word2) {
	return areSynonyms(word1.toString(), word2.toString());
    }

	/** Returns true if the argument words are synonymous. */
	public static boolean areSynonyms (String word1, String word2)
	{
		if (cache.hasValue(word1,word2))
			return cache.isTrue(word1,word2);
		else
		{
			word1 = word1.intern();
			word2 = word2.intern();
			boolean value = areSynonyms0(word1,word2);
			cache.setValue(word1,word2,value);
			return value;
		}
	}
	
	/** Reads the pseudo-synonyms from either the external file given or classpath resource
	 *  if this file is null. */
	public static void loadPseudoSyns (String file)
	{
		if (file == null)
		{
			String resource = "pseudo_synonyms.txt";
			Class c = WordNet.class;
			InputStream is = c.getResourceAsStream(resource);
			if (is != null) {
				System.out.println("Reading pseudo-synonyms from classpath resource " + resource+"...");
				readPseudoSyns(is);
			}
			else
				System.out.println("No pseudo-synonym classpath resource named: " + resource);
		}
		else if (file.equals("0"))
			System.out.println("No pseudo-synonyms being used.");
		else 
		{
			System.out.println("Reading pseudo-synonyms from file: " + file + "...");
			try {readPseudoSyns(new FileInputStream(file));}
			catch (FileNotFoundException e) {throw new RuntimeException("Can't find file "+ file);}
		}
		System.out.println("Number pseudo-synonyms read: " + pseudoSynonyms.numEntries());
	}
	
	/** Reads the pseudo-synonyms from a stream which can either be a file or onboard resource. */
	private static void readPseudoSyns (InputStream is)
	{
		LineNumberReader r;
		try 
		{
			r = new LineNumberReader(new InputStreamReader(is));
			String line = null;
			while (nonNull(line = r.readLine()))
			{
				line = line.trim();
				List<String> tokens = new ArrayList();
				// Man, I'm dying to use my own utilities here
				for (StringTokenizer st = new StringTokenizer(line);st.hasMoreTokens();)
					tokens.add(st.nextToken());
				if (tokens.size() == 2)
					markAsSynonyms(tokens.get(0),tokens.get(1));
				else if (!tokens.isEmpty())
					throw new RuntimeException("Line " + r.getLineNumber() + " is bad.");
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Problem reading stop words file " + e);
		}
	}
	
	private static boolean nonNull (Object obj)
	{
		return (obj != null);
	}
	
	/** Returns the set of words which are WordNet-equivalent to the argument. */
	public static Set getEquivalents (String word)
	{
		Set set =  cache.getEquivalents(word);
		set.add(word);
		return set;
	}
	
	/** The abstract super-class of WordNet interfaces. */
	public abstract static class Wrapper
	{
		public abstract boolean areSynonyms (String word1, String word2);
	}
	
	/** This is a do-nothing null version if system is not using WordNet. */
	private static class EmptyWrapper extends Wrapper
	{
		public boolean areSynonyms (String word1, String word2) {return false;}
	}
	
	public static void main (String[] args)
	{
	    if ((args.length < 2) || (args.length > 3)) {
		System.err.println("usage: <word1> <word2> [<wordnet_db_dir>]");
		System.exit(-1);
	    }
	    String w1 = args[0];
	    String w2 = args[1];

	    if (args.length > 2) {
		setWordNetDB(args[2]);
	    } else {
		setWordNetDB("/opt/WordNet-3.0/dict/");
	    }
	    
	    System.out.println("SYN VALUE(" + w1+ ", " + w2+"): " + WordNet.getWrapper().areSynonyms(w1,w2));
	}

    public static String getWordNetDB() { return WORDNET_DB_DIR; }
    public static void setWordNetDB(String dir) { 
	if ((dir == null) || (dir.equals(""))) WORDNET_DB_DIR = null;
	else WORDNET_DB_DIR = dir; 
    }
    private static String WORDNET_DB_DIR = null;        
}
