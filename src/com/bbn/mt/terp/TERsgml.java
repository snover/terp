package com.bbn.mt.terp;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.regex.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public class TERsgml {
  public static enum TAGNAME {
	TSTSET, REFSET, DOC, AUDIOFILE, SEG, HYP, WORD, HL, P, UNKNOWN;
	static TAGNAME type(String name) {
      Object val = sgm_names.get(name.toLowerCase());
      if(val == null)
		return UNKNOWN;
      else
		return ((TAGNAME) val);
	}
  }

  public static enum ATTRNAME {
	SETID, SRCLANG, TRGLANG, DOCID, FILEID, SYSID, ID, SEGID, START, END, RANK, UNKNOWN;
	static ATTRNAME type(String name) {
      Object val = sgm_names.get(name.toLowerCase());
      if(val == null)
		return UNKNOWN;
      else
		return ((ATTRNAME) val);
	}
  }
    
  public TERsgml() {
	factory = null;
	builder = null;
  }

    
  /********************************************************
   * Name: parse
   * Desc: It tries to parse the file with the name given
   *       first, as XML
   *       then, as SGML if we failed to parse it as XML
   *       last, return document if successful or null.
   ********************************************************/
  public Document parse(String fn) {
	document = null;
	factory = DocumentBuilderFactory.newInstance();
	factory.setValidating(false);
	factory.setNamespaceAware(true);

	try {
      builder = factory.newDocumentBuilder();
	    	    
      builder.setErrorHandler(
                              new org.xml.sax.ErrorHandler() {
                                // ignore fatal errors (an exception is guaranteed)
                                public void fatalError(SAXParseException sxe)
                                  throws SAXException {
                                  /*
                                    System.out.println("** SAXException"
                                    + ", line " + sxe.getLineNumber()
                                    + ", uri " + sxe.getSystemId());
                                    System.out.println("   " + sxe.getMessage());
                                  */
                                }
					
                                // treat validation errors as fatal
                                public void error(SAXParseException spe)
                                  throws SAXParseException
                                {
                                  System.out.println("** SAXParseException"
                                                     + ", line " + spe.getLineNumber()
                                                     + ", uri " + spe.getSystemId());
                                  System.out.println("   " + spe.getMessage());
                                  //					throw e;
                                }
					
                                // dump warnings too
                                public void warning(SAXParseException warn)
                                  throws SAXParseException
                                {
                                  System.out.println("** Warning"
                                                     + ", line " + warn.getLineNumber()
                                                     + ", uri " + warn.getSystemId());
                                  System.out.println("   " + warn.getMessage());
                                }
                              }
                              ); 
	    
	    
      document = builder.parse(new File(fn));
      System.out.println("\"" + fn + "\" was successfully parsed as XML");
	} catch (SAXParseException spe) {
      buildDom(fn);
      if(document != null)
		System.out.println("\"" + fn + "\" was successfully parsed as SGML");
	} catch (SAXException sxe) {
      System.out.println("** SAXExceptionError ");
      sxe.printStackTrace();
	} catch (ParserConfigurationException pce) {
	    System.out.println("** PCError ");
	    pce.printStackTrace();
	} catch (IOException ioe) {
	    System.err.println("IOError TERsgml: " + ioe);
	    System.exit(-1);
	}
	return document;
  }

  /********************************************************
   * Name: loadSegs
   * Desc: Loads the segments from the document objects.
   *       This document object assumes the existence of sysid
   *       field, which is ususally in reference file.
   *       Segment text and sysids are put into the given hashmaps.
   ********************************************************/
    public static void loadSegs(Node node, Map<TERid,List<String> > segs) {
	int type = node.getNodeType();
	is_ref = true;

	switch(type) {
      case Node.DOCUMENT_NODE:
	    loadSegs(((Document) node).getDocumentElement(), segs);
	    break;
      case Node.ELEMENT_NODE:
	    loadSegsElement(node);
	    //	    loadSegsElement(node, segs);
	    NodeList children = node.getChildNodes();
	    if(children != null) {
          int len = children.getLength();
          for(int i = 0; i < len; ++i)
		    loadSegs(children.item(i), segs);
	    }
	    break;
      case Node.ENTITY_REFERENCE_NODE:
	    break;
      case Node.CDATA_SECTION_NODE:
	    break;
      case Node.TEXT_NODE:
	    String text = node.getNodeValue().trim();
	    if(id != null) {
		if (text == null) {
		    text = "";
		}		
		TERid tid = new TERid(id, (cur_set == null ? "" : cur_set),
				      (sys_id == null) ? "" : sys_id, 
				      (doc_id == null) ? "" : doc_id,
				      (seg_id == null) ? "" : seg_id);
		List<String> lst = segs.get(tid);

		if(lst == null) {
		    lst = new ArrayList();
		    segs.put(tid, lst);
		}
		lst.add(text);
	    }
	    id = null;
	    break;
      case Node.PROCESSING_INSTRUCTION_NODE:
	    break;
	}
  }
      
  /********************************************************
   * Name: writeXMLHeader
   * Desc: Writes the header information to the given XML file.
   ********************************************************/
  public static void writeXMLHeader(BufferedWriter xml,
                                    String hyp_fn,
                                    String ref_fn,
                                    boolean caseon) {

	Calendar now = Calendar.getInstance();
	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss, E, MM dd yyyy");

	try {
      xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      xml.newLine();
      xml.write("<system title=\"Byblos\" hyp_fname=\"" + hyp_fn +
                "\" ref_fname=\"" + ref_fn +
                "\" creation_date=\"" + formatter.format(now.getTime()));
      if(caseon)
		xml.write("\" case_sense=\"1\">");
      else
		xml.write("\" case_sense=\"0\">");
      xml.newLine();

      xml.write("<tstset setid=\"" + ((hyp_set == null)?"":hyp_set) +
                "\" srclang=\"" + ((src_lang == null)?"":src_lang) +
                "\" trglang=\"" + ((trg_lang == null)?"":trg_lang) +
                "\">");
      xml.newLine();
	} catch (IOException ioe) {
	    System.err.println("writeXMLheader: " + ioe);
	    System.exit(-1);
	}
  }

  /********************************************************
   * Name: writeXMLAlignment
   * Desc: Writes the detailed alignment for each word of the
   *       segments.
   ********************************************************/
  public static void writeXMLAlignment(BufferedWriter xml,
                                       TERalignment result,
                                       String id,
                                       boolean istrans) {
	HashMap cur_ids = parseID(id, istrans);
	if(istrans && is_first_doc)
      try {
		xml.write("  <doc docid=\"\">");
		is_first_doc = false;
      } catch (IOException ioe) {
		System.err.println("writeXMLAlignment: " + ioe);
		System.exit(-1);
      }
	if(cur_ids != null) {
	    String cur_rank = (String) cur_ids.get(ATTRNAME.RANK);
	    if(newDoc(cur_ids)) writeXMLDoc(xml, cur_ids);
	    else if(newSeg(cur_ids)) writeXMLSeg(xml, cur_ids);
	    try {
		xml.write("      <hyp id=\"" + cur_rank + 
			  "\" refid=\"" + result.bestRef +
			  "\" wrd_cnt=\"" + result.numWords +
			  "\" num_errs=\"" + result.numEdits +
			  "\">");
		writeXMLAlignmentDetails(xml, result.ref, result.aftershift, result.alignment, 
					 result.alignment_h, result.alignment_r, result.allshifts);
		xml.write("      </hyp>");
	    } catch (IOException ioe) {
		System.err.println("writeXMLAlignment: " + ioe);
		System.exit(-1);
	    }
	}
  }

  /********************************************************
   * Name: writeXMLFooter
   * Desc: Closes the tags for the given XML file.
   ********************************************************/
  public static void writeXMLFooter(BufferedWriter xml) {
	try {
      xml.newLine();
      xml.write("    </seg>");
      xml.newLine();
      xml.write("  </doc>");
      xml.newLine();
      xml.write("</tstset>");
      xml.newLine();
      xml.write("</system>");
	} catch (IOException ioe) {
	    System.err.println("writeXMLFooter: " + ioe);
	    System.exit(-1);
	}
  }

  /********************************************************
   * Name: join
   * Desc: Joins the elements of the given array list with
   *       the given delimeter.
   ********************************************************/
  public static String join(String delim,
                            ArrayList al) {
	String ret = "";
	if(al != null) {
      for(int i = 0; i < al.size() - 1; ++i)
		ret += al.get(i) + delim;
      ret += al.get(al.size() - 1);
	}
	return ret;	
  }

  /**********************************
   * Private Attributes & Functions *
   **********************************/
  private static String cur_set;
  private static String src_lang;
  private static String trg_lang;
  private static String doc_id;
  private static String sys_id;
  private static String seg_id;
  private static String hyp_rank;
  private static String id;
  private static HashMap sgm_names;
  private static boolean is_ref;
  private static String last_doc;
  private static String last_seg;
  private static String hyp_set;
  private static boolean is_first_doc;
  private static boolean is_first_seg;

  // patterns to pre-process sgml
  private Pattern start_tag_i;
  private Pattern end_tag_i;
  private Pattern start_end_tag_i;
  private Pattern no_tag_i;
  private Pattern attr_i;
  private boolean is_sgml;
  private Document document;
  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;
  private Element cur_element;

  /********************************************************
   * Name: static
   * Desc: Initializes the hashmaps for the tags and attributes.
   ********************************************************/
  static {
	last_doc = "";
	last_seg = "";
	is_first_doc = true;
	is_first_seg = true;

	sgm_names = new HashMap();
	sgm_names.put("tstset", TAGNAME.TSTSET);
	sgm_names.put("refset", TAGNAME.REFSET);
	sgm_names.put("doc", TAGNAME.DOC);
	sgm_names.put("audiofile", TAGNAME.AUDIOFILE);
	sgm_names.put("hyp", TAGNAME.HYP);
	sgm_names.put("seg", TAGNAME.SEG);
	sgm_names.put("word", TAGNAME.WORD);
	sgm_names.put("p", TAGNAME.P);
	sgm_names.put("hl", TAGNAME.HL);
	
	sgm_names.put("setid", ATTRNAME.SETID);
	sgm_names.put("srclang", ATTRNAME.SRCLANG);
	sgm_names.put("trglang", ATTRNAME.TRGLANG);
	sgm_names.put("docid", ATTRNAME.DOCID);
	sgm_names.put("fileid", ATTRNAME.FILEID);
	sgm_names.put("sysid", ATTRNAME.SYSID);
	sgm_names.put("id", ATTRNAME.ID);
	sgm_names.put("segid", ATTRNAME.SEGID);
	sgm_names.put("start", ATTRNAME.START);
	sgm_names.put("end", ATTRNAME.END);
  }

  /********************************************************
   * Name: newDoc
   * Desc: Test if the given id is from a new document.
   ********************************************************/
  private static boolean newDoc(HashMap cur_ids) {
	Object val = cur_ids.get(ATTRNAME.DOCID);
	String cur_doc = (String) val;
	if(cur_doc.equalsIgnoreCase(last_doc)) 
      return false;
	else {
      last_doc = cur_doc;
      return true;
	}
  }

  /********************************************************
   * Name: newSeg
   * Desc: Test if the given id is from a new Segment.
   ********************************************************/
  private static boolean newSeg(HashMap cur_ids) {
	Object val = cur_ids.get(ATTRNAME.SEGID);
	String cur_seg = (String) val;
	if(cur_seg.equalsIgnoreCase(last_seg)) 
      return false;
	else {
      last_seg = cur_seg;
      return true;
	}
  }

  /********************************************************
   * Name: parseID
   * Desc: Extracts the docid, segid and rank from the given id.
   ********************************************************/
  private static HashMap parseID(String id,
                                 boolean istrans) {
      HashMap ret = new HashMap();
      TERid tid = TERid.parseIDnf(id);
	
      String[] sl = id.split(":");
      ret.put(ATTRNAME.RANK, tid.nbest);
      ret.put(ATTRNAME.DOCID, tid.doc_id);
      
      if (tid.seg_id.equals("") && tid.doc_id.equals("")) {
	  try {
	      ret.put(ATTRNAME.SEGID, "" + Integer.valueOf(tid.id));
	  } catch (NumberFormatException nfe) {
	      ret.put(ATTRNAME.SEGID, tid.id);
	  }
      } else {
	  try {
	      ret.put(ATTRNAME.SEGID, "" + Integer.valueOf(tid.seg_id));
	  } catch (NumberFormatException nfe) {
	      ret.put(ATTRNAME.SEGID, tid.seg_id);
	  }
	}
      
      return ret;
  }

  /********************************************************
   * Name: writeXMLDoc
   * Desc: Writes the tags for the new document.
   ********************************************************/
  private static void writeXMLDoc(BufferedWriter xml,
                                  HashMap cur_ids) {
	try {
      if(!is_first_doc) {
		xml.newLine();
		xml.write("    </seg>");
		xml.newLine();
		xml.write("  </doc>");
		xml.newLine();
      }
      is_first_doc = false;
      is_first_seg = false;
      String cur_doc = (String) cur_ids.get(ATTRNAME.DOCID);
      String cur_seg = (String) cur_ids.get(ATTRNAME.SEGID);
      xml.write("  <doc docid=\"" + cur_doc + "\">");
      xml.newLine();
      xml.write("    <seg segid=\"" + cur_seg + "\">");
      xml.newLine();
	} catch (IOException ioe) {
	    System.err.println("writeXMLDoc: " + ioe);
	    System.exit(-1);
	}
  }

  /********************************************************
   * Name: writeXMLSeg
   * Desc: Writes the tags for the new segment.
   ********************************************************/
  private static void writeXMLSeg(BufferedWriter xml,
                                  HashMap cur_ids) {
	try {
      if(!is_first_seg) {
		xml.newLine();
		xml.write("    </seg>");
      }
      xml.newLine();
      is_first_seg = false;
      String cur_seg = (String) cur_ids.get(ATTRNAME.SEGID);
      xml.write("    <seg segid=\"" + cur_seg + "\">");
      xml.newLine();
	} catch (IOException ioe) {
	    System.err.println("writeXMLSeg: " + ioe);
	    System.exit(-1);
	    return;
	}
  }

  /********************************************************
   * Name: writeXMLAlignmentDetails
   * Desc: Writes the alignment information for the given
   *       segments. They can be I, S, D, P, Y, T, and M.
   ********************************************************/
  private static void writeXMLAlignmentDetails(BufferedWriter xml,
                                               Comparable[] ref,
                                               Comparable[] hyp,
                                               char[] alignment,
					       int[] alignment_h,
					       int[] alignment_r,
                                               TERshift[] allshifts) {
      HashMap align_info = new HashMap();
      ArrayList shift_dists = new ArrayList();
      int anum = 1;
      int ind_start = 0;
      int ind_end = 1;
      int ind_from = 2;
      int ind_in = 3;
      int ostart, oend, odest;
      int slen = 0;
      int nstart, nend, nfrom, dist;
      int non_inserr = 0;
      
      if(allshifts != null) {
	  for(int i = 0; i < allshifts.length; ++i) {
	      ostart = allshifts[i].start;
	      oend = allshifts[i].end;
	      odest = allshifts[i].newloc;
	      slen = allshifts[i].size();
	      
	      if(odest >= oend) {
		  // right
		  nstart = odest + 1 - slen;
		  nend = nstart + slen - 1;
		  nfrom = ostart;
		  dist = odest - oend;
	      } else {
		  // left
		  nstart = odest + 1;
		  nend = nstart + slen - 1;
		  nfrom = ostart + slen;
		  dist = (ostart - odest -1) * -1;
	      }
	      
	      
	      shift_dists.add(dist);
	      
	      Object val = align_info.get(nstart + "-" + ind_start);
	      if(val == null) {
		  ArrayList al = new ArrayList();
		  al.add(anum);
		  align_info.put(nstart + "-" + ind_start, al);
	      } else {
		  ArrayList al = (ArrayList) val;
		  al.add(anum);
	      }
	      
	      val = align_info.get(nend + "-" + ind_end);
	      if(val == null) {
		  ArrayList al = new ArrayList();
		  al.add(anum);
		  align_info.put(nend + "-" + ind_end, al);
	      } else {
		  ArrayList al = (ArrayList) val;
		  al.add(anum);
	      }
	      
	      val = align_info.get(nfrom + "-" + ind_from);
	      if(val == null) {
		  ArrayList al = new ArrayList();
		  al.add(anum);
		  align_info.put(nfrom + "-" + ind_from, al);
	      } else {
		  ArrayList al = (ArrayList) val;
		  al.add(anum);
	      }
	      //	System.out.println("nstart: " + nstart + ", nend:" + nend);
	      if(slen > 0) {
		  for(int j = nstart; j <= nend; ++j) {
		      val = align_info.get(j + "-" + ind_in);
		      if(val == null) {
			  ArrayList al = new ArrayList();
			  al.add(anum);
			  align_info.put(j + "-" + ind_in, al);
		      } else {
			  ArrayList al = (ArrayList) val;
			  al.add(anum);
		      }
		  }
	      }
	      anum++;
	  }
      }
      
      int hyp_idx = 0;
      int ref_idx = 0;
      if(alignment != null) {
	  for(int i = 0; i < alignment.length; ++i) {
	      String shift_in_str = "";
	      if(alignment[i] != 'D') {
		  if(slen > 0) {
		      Object val = align_info.get(hyp_idx + "-" + ind_in);
		      if(val != null)
			  shift_in_str = join(",", (ArrayList) val);
		      //	if(val != null) System.out.println("shiftstr: " + ref_idx + "," + hyp_idx + "-" + ind_in + ":" + shift_in_str);
		  } 
	      }
	      switch (alignment[i]) {
	      case ' ':
		  try {
		      if(i == 0) xml.newLine();
		      if(!ref[ref_idx].equals(hyp[hyp_idx]))
			  System.out.println("* Error: unmatch found, " + ref[ref_idx] +
					     " vs. " + hyp[hyp_idx]);
		      xml.write("        ");
		      xml.write("\"" + ref[ref_idx] + "\",\"" +
				hyp[hyp_idx] + "\"," + "M,");
		      //	System.out.println("hyp[" + hyp_idx + "] ref[" + ref_idx + "]:"+shift_in_str + ","+ ref[ref_idx] + "," + hyp[hyp_idx]);
		      if(shift_in_str.equalsIgnoreCase(""))
			  xml.write("0");
		      else {
			  Object val = align_info.get(hyp_idx + "-" + ind_in);
			  if(val != null) {
			      ArrayList al = (ArrayList) val;
			      //		System.out.println(hyp_idx + "," + ref_idx + ":" + al.get(0) + "-" + shift_dists.get((Integer)al.get(0) - 1)); 
			      xml.write(""+shift_dists.get((Integer)al.get(0) - 1));
			  }
		      }
		      xml.newLine();
		      hyp_idx++;
		      ref_idx++;
		      non_inserr++;
		  } catch (IOException ioe) {
		      System.err.println(ioe);
		      System.exit(-1);
		      return;
		  }
		  break;
	      case 'P':
		  try {
		      if(i == 0) xml.newLine();
		      for (int k = 0; 
			   k < ((alignment_h[i] > alignment_r[i]) ? alignment_r[i] : alignment_h[i]); 
			   k++) {
			  
			  xml.write("        ");
			  xml.write("\"" + ref[ref_idx] + "\",\"" +
				    hyp[hyp_idx] + "\",P,");
			  if(shift_in_str.equalsIgnoreCase(""))
			      xml.write("0");
			  else {
			      Object val = align_info.get(hyp_idx + "-" + ind_in);
			      if(val != null) {
				  ArrayList al = (ArrayList) val;
				  //		System.out.println(hyp_idx + "," + ref_idx + ":" + al.get(0) + "-" + shift_dists.get((Integer)al.get(0) - 1)); 
				  xml.write(""+shift_dists.get((Integer)al.get(0) - 1));
			      }
			  }
			  xml.newLine();
			  ref_idx++;
			  hyp_idx++;
			  non_inserr++;
		      }
		      if (alignment_h[i] > alignment_r[i]) {
			  for (int k = alignment_r[i]; 
			       k < alignment_h[i]; 
			       k++) {
			      if(i == 0) xml.newLine();
			      xml.write("        ");
			      xml.write("\"\",\"" + hyp[hyp_idx] + 
					"\",P,");
			  if(shift_in_str.equalsIgnoreCase(""))
			      xml.write("0");
			  else {
			      Object val = align_info.get(hyp_idx + "-" + ind_in);
			      if(val != null) {
				  ArrayList al = (ArrayList) val;
				  //		System.out.println(hyp_idx + "," + ref_idx + ":" + al.get(0) + "-" + shift_dists.get((Integer)al.get(0) - 1)); 
				  xml.write(""+shift_dists.get((Integer)al.get(0) - 1));
			      }
			  }
			  xml.newLine();
			  hyp_idx++;
			  }
		      } else if (alignment_r[i] > alignment_h[i]) {
			  for (int k = alignment_h[i]; 
			       k < alignment_r[i]; 
			       k++) {
			      if(i == 0) xml.newLine();
			      xml.write("        ");
			      xml.write("\"" + ref[ref_idx] + 
					"\",\"\",P,");
			  if(shift_in_str.equalsIgnoreCase(""))
			      xml.write("0");
			  else {
			      Object val = align_info.get(hyp_idx + "-" + ind_in);
			      if(val != null) {
				  ArrayList al = (ArrayList) val;
				  //		System.out.println(hyp_idx + "," + ref_idx + ":" + al.get(0) + "-" + shift_dists.get((Integer)al.get(0) - 1)); 
				  xml.write(""+shift_dists.get((Integer)al.get(0) - 1));
			      }
			  }
			      xml.newLine();
			      ref_idx++;
			      non_inserr++;			      
			  }			  
		      }
		  } catch (IOException ioe) {
		      System.err.println("writeAlignmentDetails: " + ioe);
		      System.exit(-1);
		  }
		  break;
	      case 'S': case 'Y': case 'T':
		  try {
		      char sym = alignment[i];
		      if(i == 0) xml.newLine();
		      xml.write("        ");
		      xml.write("\"" + ref[ref_idx] + "\",\"" +
				hyp[hyp_idx] + "\"," + sym + ",0");
		      xml.newLine();
		      ref_idx++;
		      hyp_idx++;
		      non_inserr++;
		  } catch (IOException ioe) {
		      System.err.println("writeAlignmentDetails: " + ioe);
		      System.exit(-1);
		  }
		  break;
	      case 'D':
		  try {
		      if(i == 0) xml.newLine();
		      xml.write("        ");
		      xml.write("\"" + ref[ref_idx] + 
				"\",\"\",D,0");
		      xml.newLine();
		      ref_idx++;
		      non_inserr++;
		  } catch (IOException ioe) {
		      System.err.println("writeAlignmentDetails: " + ioe);
		      System.exit(-1);
		  }
		  break;
	      case 'I':
		  try {
		      //			System.out.println("hyp[hyp_idx-1]: " + ((hyp_idx>0)?hyp[hyp_idx-1]:"") + "," + hyp_idx + ", "+ hyp.length);
		      if(i == 0) xml.newLine();
		      xml.write("        ");
		      xml.write("\"\",\"" + hyp[hyp_idx] + 
				"\",I,0");
		      xml.newLine();
		      hyp_idx++;
		  } catch (IOException ioe) {
		      System.out.println("writeAlignmentDetails: " + ioe);
		      System.exit(-1);
		  }
		  break;
	      }
	  }
      }
      if(non_inserr != ref.length && ref.length > 1)
	  System.out.println("** Error, unmatch non-insertion erros " + non_inserr + 
			     " and reference length " + ref.length );
  }
    

  /********************************************************
   * Name: addDomElement
   * Desc: Adds a new element into the document.
   ********************************************************/
  private void addDomElement(Element parent, 
                             Element child,
                             HashMap content) {
	Iterator attrs = (content.keySet()).iterator();
	while(attrs.hasNext()) {
      String attr_name = (String) attrs.next();
      String attr_val = (String) content.get(attr_name);
      attr_val = attr_val.replaceAll("\"", "");
      child.setAttribute(attr_name, attr_val);
	}
	parent.appendChild(child);	
  }

  /********************************************************
   * Name: getAttr
   * Desc: Extracts the attributes from the string.
   ********************************************************/
  private void getAttr(String inner,
                       HashMap content) {
	while(inner != "") {
      Matcher attr_m = attr_i.matcher(inner);
      if(attr_m.matches()) {
		String attr_name = attr_m.group(1);
		String attr_val = attr_m.group(2);
		content.put(attr_name, attr_val);
		inner = attr_m.group(3);
      } else
		inner = "";
	}
  }

  /********************************************************
   * Name: getNTag
   * Desc: Parse the line to extract the properties of the tag.
   *       It returns int to indicate different type of tags found:
   *       0 - nothing is found.
   *       1 - start tag in the form of <tag> is found.
   *       2 - end tag in the form of </tag> is found.
   *       3 - both start and end tags are found <tag/> is found.
   ********************************************************/
  private int getNTag(String line,
                      TERtag tag) {

	Matcher start_tag_m = start_tag_i.matcher(line);
	Matcher end_tag_m = end_tag_i.matcher(line);
	Matcher start_end_tag_m = start_end_tag_i.matcher(line);
	Matcher no_tag_m = no_tag_i.matcher(line);

	if(start_end_tag_m.matches()) {
      tag.name = start_end_tag_m.group(1);
      String inner = start_end_tag_m.group(2);
      getAttr(inner, tag.content);
      tag.rest = start_end_tag_m.group(3);

      return 3;
	} else if (end_tag_m.matches()) {
      tag.name = end_tag_m.group(1);
      tag.rest = end_tag_m.group(3);

      return 2;
	} else if (start_tag_m.matches()) {
      tag.name = start_tag_m.group(1);
      String inner = start_tag_m.group(2);
      getAttr(inner, tag.content);
      tag.rest = start_tag_m.group(3);

      return 1;
	} else if (no_tag_m.matches()) {
      tag.name = "";
      tag.rest = no_tag_m.group(2);
      tag.content.put("text", no_tag_m.group(1));

      return 1;
	} else
      return 0;
  }

  /********************************************************
   * Name: buildDomLine
   * Desc: Builds the document elements from a given line is possible.
   ********************************************************/
  private void buildDomLine(String line) {
	TERtag tag = new TERtag();

	if(line.trim().equals("")) return;
	else if(document == null) return;

	int found = getNTag(line, tag);

	if(cur_element == null && (found == 1 || found == 3) && !tag.name.equalsIgnoreCase("")) {
      if(tag.name.equalsIgnoreCase("tstset") ||
         tag.name.equalsIgnoreCase("refset")) {
		is_sgml = true;
		Element root = (Element) document.createElement(tag.name);
		document.appendChild(root);
		cur_element = root;
		Iterator attrs = (tag.content.keySet()).iterator();
		while(attrs.hasNext()) {
          String attr_name = (String) attrs.next();
          String attr_val = (String) tag.content.get(attr_name);
          attr_val = attr_val.replaceAll("\"", "");
          cur_element.setAttribute(attr_name, attr_val);
		}
		buildDomLine(tag.rest);
      } else return;
	} else if(cur_element != null) {
      switch(found) {
	    case 1:
          // text node or start tag
          if(tag.name.equalsIgnoreCase("")) {
		    String text = (String) tag.content.get("text");
		    if(text != null)
              cur_element.appendChild(document.createTextNode(text));
		    buildDomLine(tag.rest);
          } else {
		    Element child = document.createElement(tag.name);
		    addDomElement(cur_element, child, tag.content);
		    cur_element = child;

		    buildDomLine(tag.rest);
          }
          break;
	    case 2:
          // end tag
          if(tag.name.equalsIgnoreCase(cur_element.getTagName())) {
		    if(cur_element.getParentNode().getNodeType() != Node.DOCUMENT_NODE)
              cur_element = (Element) cur_element.getParentNode();
          } else {
		    System.out.println("** Warning: found mis-matching tags in line: " + line + " - " + 
                               tag.name + " vs " + cur_element.getTagName());
		    document = null;
		    return;
          }
          break;
	    case 3:
          // < />
          if(!tag.name.equalsIgnoreCase("")) {
		    Element child = document.createElement(tag.name);
		    addDomElement(cur_element, child, tag.content);
		    buildDomLine(tag.rest);
          } else {
		    System.out.println("** Warning: found empty name tags " +
                               tag.name);
		    document = null;
		    return;
          }
          break;
	    default:
          ;
      }
	}
  }

  /********************************************************
   * Name: buildDom
   * Desc: Constructs the document from a text file if possible.
   ********************************************************/
  private void buildDom(String fn) {
	is_sgml = false;
	start_tag_i = Pattern.compile("^\\s*\\<([^> ]*)\\s*([^>]*)\\>(.*)$", Pattern.CASE_INSENSITIVE);
	end_tag_i = Pattern.compile("^\\s*\\</([^> ]*)\\s*([^>]*)\\>(.*)$", Pattern.CASE_INSENSITIVE);
	start_end_tag_i = Pattern.compile("^\\s*\\<([^> ]*)\\s*([^>]*)\\\\>(.*)$", Pattern.CASE_INSENSITIVE);
	attr_i = Pattern.compile("^\\s*(\\S+)=(\\S+)\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	no_tag_i = Pattern.compile("^\\s*([^\\<]*)(.*)\\s*$", Pattern.CASE_INSENSITIVE);

	// parse the input with java pattern
	BufferedReader stream = null;
	try {
	    stream = new BufferedReader(new FileReader(fn));
	} catch (IOException ioe) {
	    System.err.println("buildDom: " + ioe);
	    System.exit(-1);
	}
	
	try {
	    String line;
	    cur_element = null;
	    builder = factory.newDocumentBuilder();
	    document = builder.newDocument(); 
	    while ((line = stream.readLine()) != null) {
		if(line.matches("^\\s*$"))
		    continue;
		buildDomLine(line);
	    }
	    if(!is_sgml)
		document = null;
	    
	} catch (ParserConfigurationException pce) {
	    System.out.println("** PCError ");
	    pce.printStackTrace();
	    document = null;
	    return;
	} catch (IOException ioe) {
	    System.err.println("buildDom: " + ioe);
	    System.exit(-1);
	}

  }


  /********************************************************
   * Name: loadSegsElement
   * Desc: Load segs from element nodes of the document. This
   *       function decides how to deal with different tags.
   ********************************************************/
  private static void loadSegsElement(Node node) {
	String name = node.getNodeName().trim();
	NamedNodeMap attrs = node.getAttributes();

	switch(TAGNAME.type(name)) {
      case TSTSET:
      case REFSET:
	    if(attrs != null) {
          for(int i = 0; i < attrs.getLength(); ++i) {
		    Node attr = attrs.item(i);
		    String attrName = attr.getNodeName().trim();
		    switch(ATTRNAME.type(attrName)) {
              case SETID:
                cur_set = attr.getNodeValue().trim();
                if(!is_ref) hyp_set = cur_set;
                break;
              case SRCLANG:
                src_lang = attr.getNodeValue().trim();
                break;
              case TRGLANG:
                trg_lang = attr.getNodeValue().trim();
                break;
              default:
                ;
		    }
          }
	    }
	    break;
      case DOC:
	    if(attrs != null) {
          for(int i = 0; i < attrs.getLength(); ++i) {
		    Node attr = attrs.item(i);
		    String attrName = attr.getNodeName().trim().toLowerCase();
		    switch(ATTRNAME.type(attrName)) {
              case DOCID:
                doc_id = attr.getNodeValue().trim();
                break;
              case SYSID:
                sys_id = attr.getNodeValue().trim();
                break;
              default:
                ;
		    }
          }
	    }
	    break;
      case AUDIOFILE:
	    if(attrs != null) {
          for(int i = 0; i < attrs.getLength(); ++i) {
		    Node attr = attrs.item(i);
		    String attrName = attr.getNodeName().trim().toLowerCase();
		    switch(ATTRNAME.type(attrName)) {
              case FILEID:
                doc_id = attr.getNodeValue().trim();
                break;
              default:
                ;
		    }
          }
	    }
	    break;
      case SEG:
	    if(attrs != null) {
          for(int i = 0; i < attrs.getLength(); ++i) {
		    Node attr = attrs.item(i);
		    String attrName = attr.getNodeName().trim();
		    switch(ATTRNAME.type(attrName)) {
              case ID:
                seg_id = attr.getNodeValue().trim();
		try {
		    id = "[" + doc_id + "][" + String.format("%1$04d", Integer.valueOf(seg_id)) + "]";
		} catch (NumberFormatException e) {
		    id = "[" + doc_id + "][" + seg_id + "]";
		}
                break;
              case SEGID:
                seg_id = attr.getNodeValue().trim();
                break;
              default:
                ;
		    }
          }
	    }
	    break;
      case HYP:
	    if(attrs != null) {
          for(int i = 0; i < attrs.getLength(); ++i) {
		    Node attr = attrs.item(i);
		    String attrName = attr.getNodeName().trim();
		    switch(ATTRNAME.type(attrName)) {
              case ID:
                String rank = attr.getNodeValue().trim();
		try {
		    id = "[" + doc_id + "][" + String.format("%1$04d", Integer.valueOf(seg_id)) + "]:" + rank;
		} catch (NumberFormatException e) {
		    id = "[" + doc_id + "][" + seg_id + "]:" + rank;
		}
                break;
              default:
                ;
		    }
          }
	    }
      case WORD:
	    break;
      default:
	    ;
	}
  }

}

