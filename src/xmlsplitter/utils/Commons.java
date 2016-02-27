/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsplitter.utils;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import xmlsplitter.impl.Splitter;

/**
 *
 * @author jvanek
 */
public class Commons {

    public static Entry<String, String> paarseCommandLineArg(String s) {

        s = s.replaceAll("^-*", "");
        String[] ss = s.split(" *= *");
        String key = ss[0].toLowerCase();
        String value = null;
        if (ss.length > 1) {
            value = s.substring(key.length() + 1);
            value = value.replaceAll("^ *= *", "");
        }
        return new EntryImpl(key, value);
    }

    public static void log(String string, int level) {
        if (level == 0) {
            System.err.println(string);
        } else if (level == 1) {
            System.out.println(string);
        }
    }

    public static String spacing(int chNumber) {
        String r = String.valueOf(chNumber);
        while (r.length() < 5) {
            r = "0" + r;
        }

        return r;
    }

    private static class EntryImpl implements Entry<String, String> {

        private final String key;
        private String value;

        private EntryImpl(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String setValue(String v) {
            String q = value;
            value = v;
            return q;
        }
    }

    public static void proceedArgs(Splitter splitter, Map<String, String> params) {
        Set<Map.Entry<String, String>> cmds = params.entrySet();
        for (Map.Entry<String, String> entry : cmds) {
            proceedArg(splitter, entry);
        }
    }

    public static void proceedArg(Splitter splitter, Entry<String, String> entry) {

        String key = entry.getKey();
        String value = entry.getValue();
        if (key.equals("ignoreuri")) {
            if (value == null) {
                Commons.log("ignoreUri  without arg. Used ALL", splitter.getLogging());
                value = "ALL";
            }
            splitter.setIgnoreUri(value);

        } else if (key.equals("minsize")) {
            if (value == null) {
                Commons.log("minSize  without arg. ignored", splitter.getLogging());
            } else {
                splitter.setMinSize(value);
            }
        } else if (key.equals("maxsize")) {
            if (value == null) {
                Commons.log("maxSize  without arg. ignored", splitter.getLogging());
            } else {
                splitter.setMaxSize(value);
            }
        } else if (key.equals("i")) {
            if (value == null) {
                Commons.log("input file without arg. ignored", splitter.getLogging());
            } else {
                splitter.setInput(new File(value));
            }
        } else if (key.equals("divsize")) {
            if (value == null) {
                Commons.log("divSize  without arg. ignored", splitter.getLogging());
            } else {
                splitter.setDivSize(value);
            }
        } else if (key.equals("o")) {
            if (value == null) {
                Commons.log("output file without arg. used working directory", splitter.getLogging());
                value = "./";
            }
            splitter.setOutputDir(new File(value));
        } else if (key.equals("headerxpath")) {
            if (value == null) {
                Commons.log("headerXpath without arg. ignored", splitter.getLogging());
            } else {
                splitter.setHeaderXpath(value);
            }
        } else if (key.equals("contentxpath")) {
            if (value == null) {
                Commons.log("contentXpath without arg. ignored", splitter.getLogging());
            } else {
                splitter.setContentXpath(value);
            }
        } else if (key.equals("footerxpath")) {
            if (value == null) {
                Commons.log("footerXpath without arg. ignored", splitter.getLogging());
            } else {
                splitter.setFooterXpath(value);
            }
        } else if (key.equals("delimiter1")) {
            if (value == null) {
                Commons.log("delimiter1 without arg. ignored", splitter.getLogging());
            } else {
                splitter.setDelimiter1(value);
            }
        } else if (key.equals("delimiter2")) {
            if (value == null) {
                Commons.log("delimiter2 without arg. ignored", splitter.getLogging());
            } else {
                splitter.setDelimiter2(value);
            }
        } else if (key.equals("config") || key.equals("l")) {/*procesed before everything*/

        } else {
            Commons.log("unknown param: " + key, splitter.getLogging());
        }
    }

}
