package com.bbn.mt.terp;

/*
                                                                
Copyright 2006 by BBN Technologies and University of Maryland (UMD)

BBN and UMD grant a nonexclusive, source code, royalty-free right to
use this Software known as Translation Error Rate COMpute (the
"Software") solely for research purposes. Provided, you must agree
to abide by the license and terms stated herein. Title to the
Software and its documentation and all applicable copyrights, trade
secrets, patents and other intellectual rights in it are and remain
with BBN and UMD and shall not be used, revealed, disclosed in
marketing or advertisement or any other activity not explicitly
permitted in writing.

BBN and UMD make no representation about suitability of this
Software for any purposes.  It is provided "AS IS" without express
or implied warranties including (but not limited to) all implied
warranties of merchantability or fitness for a particular purpose.
In no event shall BBN or UMD be liable for any special, indirect or
consequential damages whatsoever resulting from loss of use, data or
profits, whether in an action of contract, negligence or other
tortuous action, arising out of or in connection with the use or
performance of this Software.

Without limitation of the foregoing, user agrees to commit no act
which, directly or indirectly, would violate any U.S. law,
regulation, or treaty, or any other international treaty or
agreement to which the United States adheres or with which the
United States complies, relating to the export or re-export of any
commodities, software, or technical data.  This Software is licensed
to you on the condition that upon completion you will cease to use
the Software and, on request of BBN and UMD, will destroy copies of
the Software in your possession.                                                

TERshift.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

import java.util.List;

/* This is just a useful class for containing shift information */
public class TERshift implements Comparable {
  TERshift () {
	start = 0;
	end = 0;
	moveto = 0;
	newloc = 0;
  }
  TERshift (int _start, int _end, int _moveto, int _newloc) {
	start = _start;
	end = _end;
	moveto = _moveto;
	newloc = _newloc;
  }
  TERshift (int _start, int _end, int _moveto, int _newloc, List _shifted) {
	start = _start;
	end = _end;
	moveto = _moveto;
	newloc = _newloc;
	shifted = _shifted;
  }
  TERshift (int _start, int _end, int _moveto, int _newloc, List _shifted, List _shiftedto) {
	start = _start;
	end = _end;
	moveto = _moveto;
	newloc = _newloc;
	shifted = _shifted;
	shiftedto = _shiftedto;
  }

    public boolean equals(Comparable c) {
	return (this.compareTo(c) == 0);
    }

    public int compareTo(Object o) {
	TERshift ts = (TERshift) o;
	if (ts.start != this.start) return (this.start - ts.start);
	if (ts.end != this.end) return (this.end - ts.end);
	return (this.newloc - ts.newloc);
    }

  public String toString() {
	String s = "[" + start + ", " + end + ", " + moveto + "/" + newloc + "]";
	if (shifted != null) s += " (" + TERalignment.join(" ", shifted) + ")";
	else s += " ( )";
	s += " -> ";
	if (shiftedto != null) s += "(" + TERalignment.join(" ", shiftedto) + ")";
	else s += "( )";

	return s;
  }

  /* The distance of the shift. */
  public int distance() {       
	if (moveto < start) {
      return start - moveto;
	} else if (moveto > end) {
      return moveto - end;
	} else {
      return moveto - start;
	}
  }

  public boolean leftShift() {
	return (moveto < start);
  }

  public int size() {
	return (end - start) + 1;
  }

  public int start = 0;
  public int end = 0;
  public int moveto = 0;
  public int newloc = 0;
  public List shifted = null; // The words we shifted
  public List shiftedto = null; // The words we shifted to

  public char[] alignment = null; // for pra_more output
  public Comparable[] aftershift = null; // for pra_more output
  // This is used to store the cost of a shift, so we don't have to
  // calculate it multiple times.
  public double cost = 1.0; 
}
