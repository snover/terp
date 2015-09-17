package com.bbn.mt.terp;

public class TERsegment implements Comparable<TERsegment> {
    public static enum SEGTYPE {
	REF, LEN, HYP;
    }
    
    public boolean equals(TERsegment tseg) {
	return (this.compareTo(tseg) == 0);
    }

    public int compareTo(TERsegment tseg) {
	if (this == tseg) return 0;
	int typecomp = this.segtype.compareTo(tseg.segtype);
	if (typecomp != 0) return typecomp;
	int idcomp = this.id.compareTo(tseg.id);
	if (idcomp != 0) return idcomp;
	return this.seg.compareTo(tseg.seg);
    }

    public TERid id;
    public String seg;
    public SEGTYPE segtype;
}