package com.bbn.mt.terp.phrasedb;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import static com.sleepycat.persist.model.Relationship.*;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class WordInfo {
    public WordInfo() {
	id = null; word = null; maxlen = 0;
    }
    public WordInfo(int i, String w, int m) {
	id = i; word = w; maxlen = m;
    }
    public WordInfo(String w) {
	id = null; word = w; maxlen = 0;
    }

    @PrimaryKey(sequence="WORDIDS")
    public Integer id = null;
    
    @SecondaryKey(relate=ONE_TO_ONE)
    public String word = null;

    public int maxlen = 0;
}
