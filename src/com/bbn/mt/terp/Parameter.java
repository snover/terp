package com.bbn.mt.terp;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.*;
import java.util.Formatter;
import java.io.*;

public class Parameter {
    private static Pattern opts_p = Pattern.compile("^\\s*-(\\S+)\\s*$");    
    private static Pattern para_pat = Pattern.compile("^([^:]+):(.*)$");
    public static enum OPT_TYPES {
	FILENAME, STRING, INTEGER, DOUBLE, BOOLEAN, 
	DOUBLELIST, INTLIST, STRINGLIST,
	PARAMFILE, SHOWUSAGE
    }

    private HashMap<Object, Object> paras = new HashMap();
    private HashMap<Object, String> param_names = new HashMap();
    private HashMap<String, Object> revName = new HashMap();
    private HashMap<Object, OPT_TYPES> opt_type = new HashMap();
    private ArrayList<Object> options = new ArrayList();
    
    private HashMap command_flags = new HashMap();
    private HashMap command_arg_flags = new HashMap();

    private String usage_statement = "";
    private boolean usage_quit = true;
    
    public void set_usage_statement(String s) { usage_statement = s; }

    public void set_usage_statement(String s, boolean quit_on_usage) {
	usage_statement = s; usage_quit = quit_on_usage;
    }

    public void addCmdBoolFlag(Object opt, char flag, String flagval) {
	if (opt_type.containsKey(opt)) {
	    if (command_flags.containsKey(flag) || command_arg_flags.containsKey(flag)) {
		System.err.println("WARNING: Flag '" + flag + "' already registered");
	    } else {
		Object[] ar = new Object[2];
		ar[0] = opt;
		ar[1] = flagval;
		command_flags.put(flag, ar);
	    }	    
	} else {
	    System.err.println("WARNING:  Cannot add command line flag for unregister option: " + opt);
	}	
    }

    public void addCmdArgFlag(Object opt, char flag) {
	if (opt_type.containsKey(opt)) {
	    if (command_flags.containsKey(flag) || command_arg_flags.containsKey(flag)) {
		System.err.println("WARNING: Flag '" + flag + "' already registered");
	    } else {
		command_arg_flags.put(flag, opt);
	    }	    
	} else {
	    System.err.println("WARNING:  Cannot add command line flag for unregister option: " + opt);
	}	
    }

    public void add_opt(OPT_TYPES ot, Object op, String name, Object initval) {
	if (! opt_type.containsKey(op)) {
	    options.add(op);
	    param_names.put(op, name);
	    paras.put(op, initval);
	    revName.put(name, op);
	    opt_type.put(op, ot);
	} else {
	    System.err.println("WARNING: option " + op + " already defined");
	}
    }

    public void usage() { usage(null); }
    public void usage(String message) {	
	if ((message != null) && (message.length() > 0))
	    System.err.println(message);
	
	if ((this.usage_statement != null) && (this.usage_statement.length() > 0))
	    System.out.println(this.usage_statement);
	
	if (usage_quit) {
	    if ((message == null) || (message.length() == 0))
		System.exit(0);
	    else
		System.exit(-1);
	}
    }

    public String[] parse_args(String[] args, boolean allow_other_args) {
	ArrayList excess = null;
	if (allow_other_args)
	    excess = new ArrayList();

	for(int i = 0; i < args.length; ++i) {
	    Matcher m = opts_p.matcher(args[i]);	    
	    if(m.matches()) {
		String flags = m.group(1);
		boolean need_arg = false;
		for (int j = 0; j < flags.length(); j++) {
		    char optfl = flags.charAt(j);
		    if (command_flags.containsKey(optfl)) {
			Object[] ar = (Object[]) command_flags.get(optfl);
			process_op_input(ar[0], (String) ar[1]);			
		    } else if (command_arg_flags.containsKey(optfl)) {
			Object opt = command_arg_flags.get(optfl);
			if (need_arg) {
			    usage("Multiple flags requiring arguments used together: " + args[i]);
			} else if (i == (args.length - 1)) {
			    usage("Argument required when using flag: " + optfl);
			} else {
			    process_op_input(opt, args[++i]);
			    need_arg = true;
			}
		    } else {
			usage("Invalid parameter flag: " + optfl);
		    }
		}		
	    } else {
		if (allow_other_args) {
		    excess.add(args[i]);
		} else {
		    usage("Unknown argument " + args[i]);
		}
	    }
	}
	if (excess == null) return null;
	return (String[]) excess.toArray(new String[0]);
    }

    public void readPara(String fname) {
	try {
	    BufferedReader fh = new BufferedReader(new FileReader(fname));
	    String line;
	    while ((line = fh.readLine()) != null) {
		Matcher mat = para_pat.matcher(line);
		if (mat.matches()) {
		    String field = mat.group(1);
		    String val = mat.group(2);
		    field = field.trim();
		    val = val.trim();
		    Object op = revName.get(field);
		    if (op != null) {
			process_op_input(op, val);
		    }
		}
	    }
	    fh.close();
	} catch (IOException ioe) {
	    System.out.println(ioe);
	    return;
	}
	return;
    }

    
    public String outputPara() { return this.toString(); }
    public String toString() {
	String tr = "";
	int max_len = 0;
	for (Object op : options) {	    
	    String m = (String) param_names.get(op);
	    if ((m == null) || (m.length() == 0)) continue;	    
	    if (max_len < m.length()) max_len = m.length();
	}
	String outformat = "%-" + max_len  + "s  : %s\n";
	Formatter fr = new Formatter(); 
	for (Object op : options) {	    
	    String s = null;
	    String name = (String) param_names.get(op);	    
	    if ((name == null) || (name.length() == 0)) continue;
	    Object o = paras.get(op);
	    OPT_TYPES otype = (OPT_TYPES) opt_type.get(op);
	    if (o == null) {
		s = "";    
	    } else if (otype == OPT_TYPES.DOUBLELIST) {
		s = doublelist_toString((double[]) o);
	    } else if (otype == OPT_TYPES.STRINGLIST) {
		s = stringlist_toString((String[]) o);
	    } else if (otype == OPT_TYPES.INTLIST) {
		s = integerlist_toString((int[]) o);
	    } else {
		s = o.toString();
	    }
	    fr.format(outformat, (String) param_names.get(op), s);
	}
	return fr.toString();
    }
    
    public void process_op_input(Object opt, String val) {
	Object oval = null;
	switch((OPT_TYPES) opt_type.get(opt)) {
	case SHOWUSAGE:
	    usage(); return;
	case PARAMFILE:
	    if ((val == null) || val.equals("")) return;	    
	    System.out.println("Loading parameters from " + val);	    
	    readPara(val); return;	    
	case BOOLEAN:
	    if (val.equals("")) return;
	    oval = Boolean.valueOf(val); break;
	case STRING: case FILENAME:
	    oval = val; break;
	case DOUBLE:
	    if (val.equals("")) return;
	    oval = Double.valueOf(val); break;
	case INTEGER:
	    if (val.equals("")) return;
	    oval = Integer.valueOf(val); break;
	case DOUBLELIST:
	    if (val.equals("")) return;
	    oval = parse_doublelist(val); break;
	case STRINGLIST:
	    if (val.equals("")) return;	    
	    oval = parse_stringlist(val); break;
	case INTLIST:
	    if (val.equals("")) return;
	    oval = parse_integerlist(val); break;
	default:
	    System.err.println("ERROR.  Can't figure out how to load value for " + opt + " with value " + val);	    
	    System.exit(-1);
	    return;
	}
	paras.put(opt, oval);
    }   

    public static String doublelist_toString(double[] d) {
	String tr = "";
	for (int i = 0; i < d.length; i++) {
	    if (i == 0) tr += d[i];
	    else tr += " " + d[i];
	}
	return tr;
    }

    public static String integerlist_toString(int[] d) {
	String tr = "";
	for (int i = 0; i < d.length; i++) {
	    if (i == 0) tr += d[i];
	    else tr += " " + d[i];
	}
	return tr;
    }

    public static String stringlist_toString(String[] d) {
	String tr = "";
	for (int i = 0; i < d.length; i++) {
	    if (i == 0) tr += d[i];
	    else tr += " " + d[i];
	}
	return tr;
    }

    public static double[] parse_doublelist(String lst) {
	lst = lst.trim();
	String[] vals = lst.split("[, \\t]+");
	double[] tr = new double[vals.length];
	for (int i = 0; i < tr.length; i++) {
	    tr[i] = Double.parseDouble(vals[i]);
	}
	return tr;
    }

    public static int[] parse_integerlist(String lst) {
	lst = lst.trim();
	String[] vals = lst.split("[, \\t]+");
	int[] tr = new int[vals.length];
	for (int i = 0; i < tr.length; i++) {
	    tr[i] = Integer.parseInt(vals[i]);
	}
	return tr;
    }

    public static String[] parse_stringlist(String lst) {
	lst = lst.trim();
	String[] vals = lst.split("[, \\t]+");
	return vals;
    }

    public void set(Object opt, Object val) {
	paras.put(opt, val);
    }

    public Object get(Object o) {      
	return paras.get(o);
    }

    public String get_string(Object o) {
	return (String) paras.get(o);
    }

    public boolean get_boolean(Object o) {
	return (Boolean) paras.get(o);
    }

    public double get_double(Object o) {
	return (Double) paras.get(o);
    }

    public int get_integer(Object o) {
	return (Integer) paras.get(o);
    }

    public double[] get_doublelist(Object o) {
	double[] d = (double[]) paras.get(o);
	if (d == null) return null;
	double[] tr = new double[d.length];
	for (int k = 0; k < d.length; k++) tr[k] = d[k];
	return tr;
    }

    public int[] get_integerlist(Object o) {
	int[] d = (int[]) paras.get(o);
	if (d == null) return null;
	int[] tr = new int[d.length];
	for (int k = 0; k < d.length; k++) tr[k] = d[k];
	return tr;
    }

    public String[] get_stringlist(Object o) {
	String[] d = (String[]) paras.get(o);
	if (d == null) return null;
	String[] tr = new String[d.length];
	for (int k = 0; k < d.length; k++) tr[k] = d[k];
	return tr;
    }

}
