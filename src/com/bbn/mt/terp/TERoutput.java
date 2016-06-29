package com.bbn.mt.terp;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.io.*;
import org.w3c.dom.Document;

public class TERoutput {
    public TERoutput() {}
    public TERoutput(String prefix, ArrayList formats) {
	this.out_prefix = prefix;
	this.formats = formats;
    }
    public TERoutput(String prefix, ArrayList formats, String hyp_fn, String ref_fn,
		     String reflen_fn, boolean caseon, TERinput terin) {
	this.out_prefix = prefix;
	this.formats = formats;
	this.hyp_fn = hyp_fn;
	this.ref_fn = ref_fn;
	this.reflen_fn = reflen_fn;
	this.caseon = caseon;
	this.terin = terin;
    }

    public void output_all() { output_all(out_prefix); }
    public void output_all(String prefix) {
	ArrayList saved_formats = formats;
	formats = new ArrayList(1);
	formats.add("all");
	output(prefix);
	formats = saved_formats;
    }

    public void rescore_all_alignments() {
	for (int i = 0; i < results.size(); i++) {
	    TERalignment result = (TERalignment) results.get(i);
	    result.rescore_alignment();
	}
    }

    public void rescore_all_alignments(TERcost costfunc) {
	for (int i = 0; i < results.size(); i++) {
	    TERalignment result = (TERalignment) results.get(i);
	    result.rescore_alignment(costfunc);
	}
    }

    public String getStrippedPrefix() { return getStrippedPrefix(out_prefix); }
    public String getStrippedPrefix(String prefix) {
	String stripped_prefix = prefix;
	while (stripped_prefix.endsWith(".")) {
	    stripped_prefix = stripped_prefix.substring(0, stripped_prefix.length()-1);
	}
	return stripped_prefix;
    }

    public void output() { output(out_prefix); }
    public void output(String prefix) {
	String saved_prefix = out_prefix;

	out_prefix = getStrippedPrefix(prefix) + refname;
	if (out_prefix.equals("") || out_prefix.endsWith("/"))
	    out_prefix = out_prefix + "terp";
	if (formats.contains("all") || formats.contains("pra")) output_pra();
	//	if (formats.contains("all") || formats.contains("pra_more")) output_pra_more();
	if (formats.contains("all") || formats.contains("ter")) output_ter();
	if (formats.contains("all") || formats.contains("sum")) output_sum();
	if (formats.contains("all") || formats.contains("sum_nbest")) output_sum_nbest();
	if (formats.contains("all") || formats.contains("html")) output_html();
	//	if (formats.contains("all") || formats.contains("dot")) output_dot();
	if (formats.contains("all") || formats.contains("param")) output_param();
	if (formats.contains("all") || formats.contains("counts")) output_counts();
       	if (formats.contains("all") || formats.contains("weights")) output_weights();
	if (formats.contains("all") || formats.contains("xml")) output_xml();
	if (formats.contains("all") || formats.contains("align")) output_align();
	if (formats.contains("all") || formats.contains("align_detail")) output_align_detail();

	//	if (formats.contains("all") || formats.contains("phrases")) output_phrases();
	out_prefix = prefix;
	if (formats.contains("all") || formats.contains("nist")) output_nist();

	out_prefix = saved_prefix;
    }

    public void output_xml() { output_xml(out_prefix + ".xml"); };
    public void output_pra() { output_pra(out_prefix + ".pra"); };
    public void output_pra_more() { output_pra_more(out_prefix + ".pra_more"); };
    public void output_ter() { output_ter(out_prefix + ".ter"); };
    public void output_sum() { output_sum(out_prefix + ".sum"); };
    public void output_sum_nbest() { output_sum_nbest(out_prefix + ".sum_nbest"); };
    public void output_html() { output_html(out_prefix + ".html"); };
    public void output_dot() { output_dot(out_prefix + ".dot"); };
    public void output_nist() { output_nist(out_prefix); }
    public void output_param() { output_param(out_prefix + ".param"); }
    public void output_counts() { output_counts(out_prefix + ".counts"); }
    public void output_weights() { output_weights(out_prefix + ".weights"); }
    public void output_phrases() { output_phrases(out_prefix + ".phrases"); }
    public void output_align() { output_align(out_prefix + ".align"); }
    public void output_align_detail() { output_align_detail(out_prefix + ".align_detail"); }

    public void output_html(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    out.write("<HTML>\n");
	    out.write("<HEAD><TITLE>TERp Results</TITLE>\n" +
		      "<LINK REL=StyleSheet HREF=\"http://www.desilinguist.org/stylesheets/terp_style.css\" TYPE=\"text/css\" MEDIA=all>" +
		      "</HEAD>");
	    out.write("<BODY>\n");

	    out.write("<H1><A NAME=\"top\">TERp Results</A></H1>");
	    out.write("<ul><li>Hypothesis File: " + hyp_fn +
		      "\n<li>Reference File: " + ref_fn + "\n" +
		      "<li>Ave-Reference File: " + ((reflen_fn == "")?ref_fn:reflen_fn) + "\n</ul>\n");

	    out.write("<TABLE id=sumtable>\n");
	    out.write("<TR id=header><TD>ID<TD>Num Words<TD>TERp Score</TR>\n");
	    for (int i = 0; i < seg_guid_ids.size(); i++) {
		String id = (String) seg_guid_ids.get(i);
		TERalignment result = (TERalignment) seg_guid_results.get(id);
		result.scoreDetails();
		out.write("<TR>");
		out.write("<TD id=sumsegid><A HREF=\"#seg" + i + "\">" + id + "</A></TD>\n");
		out.write("<TD id=sumnumwords>" + result.numWords + "</TD>\n");
		out.write("<TD id=sumterpsc>" + result.score() + "</TD>\n");
	    }
	    out.write("</TABLE>\n");

	    out.write("<BR><hr><BR>");

	    out.write("<TABLE id=detailstable>\n");
	    for (int i = 0; i < seg_guid_ids.size(); i++) {
		String id = (String) seg_guid_ids.get(i);
		out.write("<TR>");
		out.write("<TD id=label>Segment ID<TD id=segid><A NAME=\"seg" + i + "\">" + id + "</A> <A HREF=\"#top\">(back to top)</A></TD></TR>\n");

		TERalignment result = (TERalignment) seg_guid_results.get(id);
		result.scoreDetails();
		out.write("<TR><TD id=label>Number of Edits<TD id=numedits>" + result.numEdits + "</TD></TR>\n");
		out.write("<TR><TD id=label>Number of Words<TD id=numwds>" + result.numWords + "</TD></TR>\n");
		out.write("<TR><TD id=label>TERp Score<TD id=terpscore>" + result.score() + "</TD></TR>\n");
		out.write("<TR><TD id=label>Original Reference</TD><TD id=origtxt>" + result.orig_ref + "</TD></TR>\n");
		out.write("<TR><TD id=label>Original Hypothesis</TD><TD id=origtxt>" + result.orig_hyp + "</TD></TR>\n");
		out.write("<TR><TD id=label>Alignment<TD>\n");

		String[][] allalign = result.getAlignmentStringsHtml();
		if (allalign != null) {
		    String[] rwords = allalign[0];
		    String[] hwords = allalign[1];
		    String[] align = allalign[2];
		    int[] asize = new int[align.length];
		    String[] hwordsM = new String[hwords.length];
		    String[] rwordsM = new String[rwords.length];

		    int lastgood = 0;
		    for (int j = 0; j < align.length; j++) {
			if (align[j].equals(" ")) align[j] = "M";
		    }

		    int osize = 15;
		    int bsize = osize;

		    for (int start = 0; start < hwords.length; start += bsize) {
			out.write("<TABLE id=alignment>");
			String curalign = "";

			out.write("<TR id=refwds>");
			out.write("<TD id=label>Reference</TD>\n");
			for (int j = 0; j < rwords.length; j++) {
			    if (align[j].length() > 0) {
				curalign = align[j];
			    }
			    if (rwords[j].equals("")) {
				out.write("<TD class=align" + align[j] + " id=blank>" + rwords[j] + "</TD>");
			    } else {
				out.write("<TD class=align" + align[j] + ">" + rwords[j] + "</TD>");
			    }
			}

			out.write("</TR>\n<TR id=align>");
			out.write("<TD id=label></TD>\n");

			for (int j = 0; j < align.length; j++) {
			    String sym = align[j];
			    if (sym.equals("M")) sym = " ";
			    out.write("<TD class=align" + align[j] + ">" + sym + "</TD>");
			}

			out.write("</TR>\n<TR id=hypwds>");
			out.write("<TD id=label>Hyp After Shifts</TD>\n");
			for (int j = 0; j < hwords.length; j++) {
			    if (hwords[j].equals("")) {
				out.write("<TD class=align" + align[j] + " id=blank>" + hwords[j] + "</TD>");
			    } else {
				out.write("<TD class=align" + align[j] + ">" + hwords[j] + "</TD>");
			    }
			}

			out.write("</TABLE>");
		    }
		}

		out.write("</TR>\n");
		if (result.allshifts != null) {
		    out.write("<TR><TD id=label>Shifts</TD><TD>\n");
		    if (result.allshifts.length > 0) {
			out.write("<TABLE id=shifttable>\n");
			out.write("<TR id=header><TD>Start Pos<TD>End Pos<TD>Moved To<TD>Cost<TD>Hyp String<TD>Ref String</TR>\n");
			for (int j = 0; j < result.allshifts.length; j++) {
			    out.write("<TR><TD>" + result.allshifts[j].start + "</TD>\n");
			    out.write("<TD>" + result.allshifts[j].end + "</TD>\n");
			    out.write("<TD>" + result.allshifts[j].newloc + "</TD>\n");
			    out.write("<TD>" + result.allshifts[j].cost + "</TD>\n");
			    out.write("<TD>" + TERalignment.join(" ", result.allshifts[j].shifted) + "</TD>\n");
			    out.write("<TD>" + TERalignment.join(" ", result.allshifts[j].shiftedto) + "</TD>\n");
			    out.write("</TR>\n");
			}
			out.write("</TABLE>\n");
		    }
		}

		String[][] phrases = result.getSplitPhraseStrings();
		if (phrases != null) {
		    out.write("<TR><TD id=label>Phrase Substitutions</TD><TD>\n");
		    if (phrases.length > 0) {
			out.write("<TABLE id=phrasetable>\n");
			out.write("<TR id=header><TD>Adjusted Cost<TD>Orig Cost<TD>Ref Phrase<TD>Hyp Phrase</TR>\n");
			for (int j = 0; j < phrases.length; j++) {
			    out.write("<TR><TD id=adjustcost>" + phrases[j][0] + "</TD>\n");
			    out.write("<TD id=origcost>" + phrases[j][1] + "</TD>\n");
			    out.write("<TD id=refphrase>" + phrases[j][2] + "</TD>\n");
			    out.write("<TD id=hypphrase>" + phrases[j][3] + "</TD>\n");
			}
			out.write("</TABLE>\n");
		    }

		}
		out.write("<TR><TD colspan=2><hr id=segborder></TD></TR>");
	    }
	    out.write("</TABLE>");
	    out.write("</BODY></HTML>\n");
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output html: " + ioe);
	    System.exit(-1);

	}
	return;
    }

    public void output_dot(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    //	    out.write("<HTML><BODY>\n");
	    //      out.write("<TABLE>\n");
	    for (int i = 0; i < results.size(); i++) {
		TERalignment result = (TERalignment) results.get(i);
		out.write("digraph " + ((TERid) ids.get(i)) + " {\n");
		out.write(result.toDotString());
		out.write("\n}\n\n");
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output_dot: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_param(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    out.write(TERpara.para().outputPara());
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output param: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_nist(String prefix) {
	String rn = refname;
	try {
	    for (String sysid : sys_ids.keySet()) {
		ArrayList<TERid> sids = sys_ids.get(sysid);
		ArrayList<TERalignment> sresults = sys_results.get(sysid);

		BufferedWriter out_seg = new BufferedWriter(new FileWriter(prefix + sysid + rn + ".seg.scr"));

		HashMap<String, TERalignment> syslevel = new HashMap();
		HashMap<String, TERalignment> doclevel = new HashMap();
		ArrayList<String> sysorder = new ArrayList();
		ArrayList<String> docorder = new ArrayList();

		for (int i = 0; i < sresults.size(); i++) {
		    TERalignment result = sresults.get(i);
		    TERid tid = sids.get(i);
		    if (tid == null) {
			System.err.println("WARNING.  Cannot find fields for system: " + sysid + " number: " + i);
		    } else {
			if (doclevel.containsKey(tid.toDocString(sysid))) {
			    TERalignment ta = doclevel.get(tid.toDocString(sysid));
			    ta.numEdits += result.numEdits;
			    ta.numWords += result.numWords;
			} else {
			    TERalignment ta = new TERalignment();
			    ta.numEdits = result.numEdits;
			    ta.numWords = result.numWords;
			    doclevel.put(tid.toDocString(sysid), ta);
			    docorder.add(tid.toDocString(sysid));
			}
			if (syslevel.containsKey(tid.toSysString(sysid))) {
			    TERalignment ta = syslevel.get(tid.toSysString(sysid));
			    ta.numEdits += result.numEdits;
			    ta.numWords += result.numWords;
			} else {
			    TERalignment ta = new TERalignment();
			    ta.numEdits = result.numEdits;
			    ta.numWords = result.numWords;
			    syslevel.put(tid.toSysString(sysid), ta);
			    sysorder.add(tid.toSysString(sysid));
			}
			out_seg.write(tid.toSegString(sysid) + "\t" + result.score() + "\t" + result.numWords + "\n");
		    }
		}
		out_seg.close();

		BufferedWriter out_doc = new BufferedWriter(new FileWriter(prefix + sysid + rn + ".doc.scr"));
		for (int i = 0; i < docorder.size(); i++) {
		    String tid = docorder.get(i);
		    TERalignment result = doclevel.get(tid);
		    out_doc.write(tid + "\t" + result.score() + "\t" + result.numWords + "\n");
		}
		out_doc.close();

		BufferedWriter out_sys = new BufferedWriter(new FileWriter(prefix + sysid + rn + ".sys.scr"));
		for (int i = 0; i < sysorder.size(); i++) {
		    String tid = sysorder.get(i);
		    TERalignment result = syslevel.get(tid);
		    out_sys.write(tid + "\t" + result.score() + "\t" + result.numWords + "\n");
		}
		out_sys.close();
	    }
	} catch (IOException ioe) {
	    System.err.println("NIST Output " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_weights(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    if (results.size() > 0) {
		out.write("Initial Weights:\n");
		out.write(TERalignment.join(" ", ((TERalignment) results.get(0)).current_weights()) + "\n");
	    }
	    out.close();
	} catch (IOException ioe) {
            System.err.println("Weight Output " + ioe);
	    System.exit(-1);
        }
        return;
    }

    public void output_counts(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));

	    for (TERid tid : getIds()) {
		TERalignment ta = getResult(tid);
		double weights[] = ta.norm_weight_vector();
		out.write(tid.toPureSegGuid() + " " + TERalignment.join(" ", weights) + "\n");
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("Count Output " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_phrases(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		out.write("Sentence ID: " + tid + "\n" + getResult(tid).toPhraseString() + "\n\n");
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output phrases: " + ioe);
	    System.exit(-1);
	}
	return;
    }


    public void output_align(String fname) {
	/* outputs to a file an alignment where every line is of the format:
	ID REF_INDEX HYP_INDEX
	where REF_INDEX and HYP_INDEX are 1-based indices (NOT 0-based) of the words in the inputs

	if a word is aligned to nothing, it is NOT output

  	a single line is output for every word/word alignment.
	if there is a multi-word or phrasal alignment of < h_1, ..., h_m > to < r_1, ..., r_n >
           then a line is output for each of the m x n combinations.
	*/
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		TERalignment.AlignDetail[] detail_align = getResult(tid).getAlignDetail();
		if (detail_align != null) {
		    for (int i = 0; i < detail_align.length; i++) {
			TERalignment.AlignDetail ad = detail_align[i];
			if ((ad.ref_ind >= 0) && (ad.hyp_ind >= 0)) {
			    String tr = String.format("%s %d %d",
						      tid.toString(), ad.ref_ind+1, ad.hyp_ind+1);
			    out.write(tr + "\n");
			}
		    }
		}
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output align: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_align_detail(String fname) {
	/* outputs to a file a detailed alignment where every line is of the format:
	ID REF_INDEX HYP_INDEX EDIT_TYPE EDIT_ID EDIT_COST NUM_HYP_SHIFTS <r>REF_WORD</r> <h>HYP_WORD</h>
	where REF_INDEX and HYP_INDEX are 1-based indices (NOT 0-based) of the words in the inputs
	where NUM_HYP_SHIFTS is the number of times the hypotheis word has been shifted
	an index of 0 indicates that it is aligned to nothing (or the NULL word)
  	  a single line is output for every word/word alignment.
	  if there is a multi-word or phrasal alignment of < h_1, ..., h_m > to < r_1, ..., r_n >
             then a line is output for each of the m x n combinations.
	*/
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		TERalignment.AlignDetail[] detail_align = getResult(tid).getAlignDetail();
		if (detail_align != null) {
		    for (int i = 0; i < detail_align.length; i++) {
			TERalignment.AlignDetail ad = detail_align[i];
			String tr = String.format("%s %d %d %s %d %f %d <r>%s</r> <h>%s</h>",
						  tid.toString(), ad.ref_ind+1, ad.hyp_ind+1,
						  ad.edit, ad.edit_num, ad.edit_cost, ad.num_shifts,
						  ad.ref_word, ad.hyp_word);
			out.write(tr + "\n");
		    }
		}
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output align_detail: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_pra(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		out.write("Sentence ID: " + tid + "\n" + getResult(tid).toPhraseString() + "\n\n");
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output pra: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_pra_more(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		out.write("Sentence ID: " + tid + "\n" + getResult(tid).toMoreString() + "\n\n");
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output pra_more: " + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_ter(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    for (TERid tid : getIds()) {
		TERalignment result = getResult(tid);
		out.write(tid.sys_id + "\t" +
			  tid.toStringNoSys() + "\t" +
			  result.hyp.length + "\t" +
			  TERalignment.join(":", "%.2f", result.getHypEdits()) + "\t" +
			  String.format("%.2f\t", result.getHypOthEdits()) +
			  String.format("%.2f\t", result.numEdits) +
			  String.format("%.2f\t", result.numWords) +
			  String.format("%.2f\n", result.score()));
	    }
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output ter" + ioe);
	    System.exit(-1);
	}
	return;
    }

    public void output_xml(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    TERsgml.writeXMLHeader(out, hyp_fn, ref_fn, caseon);
	    for (int i = 0; i < results.size(); i++) {
		TERalignment ta = results.get(i);
		ta.scoreDetails();
		TERsgml.writeXMLAlignment(out, ta, ids.get(i).toString(),
					  (terin.in_ref_format() == 1));
	    }
	    TERsgml.writeXMLFooter(out);
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output_xml: " + ioe);
	    System.exit(-1);
	}
    }

    public void output_sum(String fname) {
	try {
	    int tot_ins = 0;
	    int tot_del = 0;
	    int tot_sub = 0;
	    int tot_stem = 0;
	    int tot_syn = 0;
	    int tot_para = 0;
	    int tot_sft = 0;
	    int tot_wsf = 0;
	    double tot_err = 0.0;
	    double tot_wds = 0.0;

	    int maxIdLen = 1;
	    for (TERid tid : getIds()) {
		int l = tid.toString().length();
		if (l > maxIdLen) maxIdLen = l;
	    }

	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    out.write("Hypothesis File: " + hyp_fn + "\nReference File: " + ref_fn + "\n" +
		      "Ave-Reference File: " + ((reflen_fn == "")?ref_fn:reflen_fn) + "\n");
 	    out.write(String.format("%1$-" + maxIdLen + "s | %2$-6s | %3$-6s | %4$-6s | %5$-6s | %6$-6s | %7$-6s | %8$-6s | %9$-6s | %10$-8s | %11$-8s | %12$-8s\n",
				    "ID", "Ins", "Del", "Sub", "Stem", "Syn", "Phrase", "Shft", "WdSh", "NumEr", "NumWd", "TERp"));
	    out.write("-------------------------------------------------------------------------------------------------------------------------------------------------\n");

	    for (TERid tid : getIds()) {
		TERalignment result = getResult(tid);
		result.scoreDetails();
		out.write(String.format("%1$-" + maxIdLen + "s | %2$6d | %3$6d | %4$6d | %5$6d | %6$6d | %7$6d | %8$6d | %9$6d | %10$8.3f | %11$8.3f | %12$8.3f\n",
					tid.toString(), result.numIns, result.numDel, result.numSub, result.numStem, result.numSyn, result.numPara,
					result.numSft, result.numWsf, result.numEdits, result.numWords, result.score()*100.0));
		tot_ins += result.numIns;
		tot_del += result.numDel;
		tot_sub += result.numSub;
		tot_stem += result.numStem;
		tot_syn += result.numSyn;
		tot_para += result.numPara;
		tot_sft += result.numSft;
		tot_wsf += result.numWsf;
		tot_err += result.numEdits;
		tot_wds += result.numWords;
	    }
	    out.write("-------------------------------------------------------------------------------------------------------------------------------------------------\n");
	    out.write(String.format("%1$-" + maxIdLen + "s | %2$6d | %3$6d | %4$6d | %5$6d | %6$6d | %7$6d | %8$6d | %9$6d | %10$8.3f | %11$8.3f | %12$8.3f\n",
				    "TOTAL", tot_ins, tot_del, tot_sub, tot_stem, tot_syn, tot_para, tot_sft, tot_wsf, tot_err, tot_wds, tot_err*100.0/tot_wds));
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output sum: " + ioe);
	    System.exit(-1);
	}
    }

    public void output_sum_nbest(String fname) {
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname));
	    out.write("Hypothesis File: " + hyp_fn + "\nReference File: " + ref_fn + "\n" +
		      "Ave-Reference File: " + ((reflen_fn == "")?ref_fn:reflen_fn) + "\n");
 	    out.write(String.format("%1$-40s %2$-6s %3$-6s %4$-6s %5$-6s %6$-6s %7$-6s %8$-6s %9$-6s %10$-8s %11$-8s %12$-8s\n",
				    "ID", "Ins", "Del", "Sub", "Stem", "Syn", "Phrase", "Shft", "WdSh", "NumEr", "NumWd", "TERp"));
	    out.write("-------------------------------------------------------------------------------------------------------------------------------------------------\n");

	    Iterator it = sorted_segs.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry me = (Map.Entry) it.next();
		TERid tid = (TERid) me.getKey();
		TERalignment result = (TERalignment) me.getValue();
		result.scoreDetails();
		out.write(String.format("%1$-40s %2$6d %3$6d %4$6d %5$6d %6$6d %7$6d %8$6d %9$6d %10$8.3f %11$8.3f %12$8.3f\n",
					tid.toString(), result.numIns, result.numDel, result.numSub, result.numStem, result.numSyn, result.numPara,
					result.numSft, result.numWsf, result.numEdits, result.numWords, result.score()*100.0));
	    }
	    out.write("-------------------------------------------------------------------------------------------------------------------------------------------------\n");
	    out.close();
	} catch (IOException ioe) {
	    System.err.println("output sum_nbest: " + ioe);
	    System.exit(-1);
	}
    }

    public void add_result(TERid id, TERalignment ta) {
	if (! (sys_results.containsKey(id.sys_id))) {
	    sys_results.put(id.sys_id, new ArrayList());
	    sys_ids.put(id.sys_id, new ArrayList());
	    sorted_sys_segs.put(id.sys_id, new TreeMap());
	}
	allresults.put(id, ta);

	this.results.add(ta);
	this.ids.add(id);
	//	this.idrankmap.put(id, id_nrank);

	sys_results.get(id.sys_id).add(ta);
	//	((ArrayList) sys_results.get(sys_name)).add(ta);
	sys_ids.get(id.sys_id).add(id);
	sorted_sys_segs.get(id.sys_id).put(id, ta);

	seg_guid_ids.add(id.toSegGuid(id.sys_id));
	seg_guid_results.put(id.toSegGuid(id.sys_id), ta);
	sorted_segs.put(id, ta);
    }

    public Set<TERid> get_sorted_ids() {
	return getIds();
    }

    public Set<TERid> getIds() {
	return allresults.keySet();
    }
    public TERalignment getResult(TERid tid) {
	return allresults.get(tid);
    }
    public TERalignment getResult(String id) {
	return (TERalignment) seg_guid_results.get(id);
    }

    public TERalignment getTERidResult(TERid tid) {
	return getResult(tid);
    }

    public TERalignment getSegGuidResult(String seg_guid) {
	return getResult(seg_guid);
    }

    public String refname = "";
    public boolean caseon = false;
    public String ref_fn = "";
    public String reflen_fn = "";
    public String hyp_fn = "";
    public TERinput terin = null;

    private HashMap<String, ArrayList>  sys_results = new HashMap();
    private HashMap<String, ArrayList> sys_ids = new HashMap();

    private HashMap<String, TERalignment> seg_guid_results = new HashMap();
    private ArrayList<String> seg_guid_ids = new ArrayList();

    private TreeMap<TERid, TERalignment> sorted_segs = new TreeMap();

    private HashMap<String, Map> sorted_sys_segs = new HashMap();

    private TreeMap<TERid, TERalignment> allresults = new TreeMap();

    private ArrayList<TERalignment> results = new ArrayList();
    private ArrayList<TERid> ids = new ArrayList();
    //    private HashMap<TERid, String> idrankmap = new HashMap();

    private String out_prefix = "";
    private ArrayList formats = new ArrayList();

    public HashMap idmap = null;
}
