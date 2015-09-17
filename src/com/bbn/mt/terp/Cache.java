package com.bbn.mt.terp;

import java.util.*;

/** A 2-dimensional symmetric associative cache with boolean values. */
public class Cache
{
	private Map map = new HashMap();

	/** Returns true if the cache has a value for <s1,s2>. */
	public boolean hasValue (Comparable s1, Comparable s2)
	{
		return (getValue(s1,s2) != null);
	}
	
	public Set getEquivalents (Comparable s1)
	{
		HashMap sub = (HashMap)map.get(s1);
		if (sub == null)
			return new HashSet();
		else
		{
			Set result = new HashSet();
			for (Object obj : sub.entrySet()) 
			{
				Map.Entry e = (Map.Entry)obj;
				if (e.getValue().equals(Boolean.TRUE))
					result.add(e.getKey());
			}
			return result;
		}
		
	}

	/** Returns true if the cache has value true for <s1,s2>. */
	public boolean isTrue (Comparable s1, Comparable s2)
	{
		Boolean val = getValue(s1,s2);
		return (val==null)?false:val.booleanValue();	  
	}
	
	/** 
	 * Returns true if the cache has value true for <s1,s2>. 
	 * Note that !isFalse(x,y) does  NOT equal isTrue(x,y), since x,y can be !isFalse 
	 * simply there's no value in the cache. 
	 *  */
	public boolean isFalse (Comparable s1, Comparable s2)
	{
		Boolean val = getValue(s1,s2);
		return (val==null)?false:!val.booleanValue();	  
	}

	/** Returns the value (a Boolean) if there is one, and null otherwise. */
	public Boolean getValue (Comparable s1, Comparable s2)
	{
		// Standardize order so we don't need to store the same value twice.
		if (s1.compareTo(s2) < 0) 
		{
			Comparable tmp = s1;
			s1 = s2;
			s2 = tmp;
		}
		Map sub = (Map)map.get(s1);
		if (sub == null)
			return null;
		else
			return (Boolean)sub.get(s2);
	}
	
	public int numEntries () {return map.entrySet().size();}

	/** Puts the value into the 2-d boolean cache for <s1,s2>. */
	public void setValue (Comparable s1, Comparable s2, boolean value)
	{
		setValue0(s1,s2,value);
		setValue0(s2,s1,value);
	}
	
	private void setValue0 (Comparable s1, Comparable s2, boolean value)
	{
		Map sub = (Map)map.get(s1);
		if (sub == null)
			map.put(s1,(sub = new HashMap()));
		sub.put(s2, value);
	}
	
	/** Clears the cache of all values. */
	public void clear ()
	{
		map.clear();
	}

}
