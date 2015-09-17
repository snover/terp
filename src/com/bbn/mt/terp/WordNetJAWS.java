package com.bbn.mt.terp;

// package bbn.mt.ster;

import java.io.*;
import edu.smu.tspell.wordnet.*;

//TODO: Insert the copyright notification in here, suitably modified. 

/** 
 * Provides a simple interface to the JAWS Java WordNet package. 
 * 
 * */

public class WordNetJAWS extends WordNet.Wrapper
{
	private WordNetDatabase database = null;
	
	public WordNetJAWS (String wordNetDir) 
	{
		if (!new File(wordNetDir).exists()) {
			System.out.println("WordNet directory does not exist; this will cause problems! " + wordNetDir);
			System.exit(1);
		}
		System.setProperty("wordnet.database.dir",wordNetDir);
		this.database = WordNetDatabase.getFileInstance();
	}
	
	/** Returns true if the two words are synonymous; that is if they have at least one 
	 *  Synset in common. */
	public boolean areSynonyms (String word1, String word2)
	{
		// TODO: We could consider hypernyms at some point.  But that would require a new
		// metric in which hypernyms were weighted differently than synonyms.  
		for (Synset s1 : database.getSynsets(word1)) 
		{
			for (Synset s2 : database.getSynsets(word2))
			{
				if (s1 == s2)
					return true;
			}
		}
		return false;
	}
	
	public void test ()
	{
		// println("%s",areSynonyms("bomb","explosive"));
		Synset[] sets = database.getSynsets("fly",SynsetType.VERB);
		for (Synset s : sets)
		{
			if (s instanceof VerbSynset) {
				VerbSynset[] hypers = ((VerbSynset)s).getHypernyms();
				for (Synset h : hypers)
					System.out.printf("HYPER %s",h);
			}
		}
		
	}
	
	/** Just here for testing. */
	public static void main (String[] args)
	{
		try 
		{
			WordNetJAWS w = new WordNetJAWS("C:/Program Files/WordNet/2.1/dict");
			w.test();
		}
		catch (Exception e) {e.printStackTrace();}
		
	}
}
