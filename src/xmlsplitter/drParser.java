/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsplitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

public class drParser {

    private static String cutEmail(String orig) {
        int i = orig.indexOf("@");
        if (i == -1) {
            throw new RuntimeException("no email in" + orig);
        }
        int ats = orig.length() - orig.replaceAll("@", "").length();
        if (ats > 1) {
            throw new RuntimeException("to much @ " + orig);
        }
        int b = 0;
        int e = orig.length();
        for (int x = i; x >= 0; x--) {
            if (orig.charAt(x) == ' ') {
                b = x;
                break;
            }
        }
        for (int x = i; x < orig.length(); x++) {
            if (orig.charAt(x) == ' ') {
                e = x;
                break;
            }
        }
        String r = orig.substring(b, e).trim();
        while (r.endsWith(".")) {
            r = r.substring(0, r.length() - 1);
        }
        String[] rr = r.split("@");
        if (rr.length != 2 || rr[0].trim().equals("") || rr[1].trim().equals("")) {
            throw new RuntimeException("invlaid host or user " + r);
        }
        if (!rr[1].contains(".")) {
            throw new RuntimeException("invlaid host");
        }
        return r;
    }

    private static String cerateName(String orig, String email) {
        String s = orig.replace(email, "");
        if (s.equals(orig)) {
            throw new RuntimeException("email not found in " + orig);
        }
        int spaces = s.length() - s.replaceAll(" ", "").length();
        if (spaces > 4) {
            throw new RuntimeException("considered sentence, ignoring " + orig);
        }
        return s.trim();
    }

    private static ComparableAddress found(ComparableAddress ca, List<ComparableAddress> addressL) {

        for (ComparableAddress comparableAddress : addressL) {
            if (comparableAddress.equals(ca)) {
                return comparableAddress;
            }
        }
        return null;
    }

    private static void logO(String s, FileOutputStream log) throws IOException {
        System.out.println(s);
        log.write((s + "\n").getBytes());
    }

    private static void logE(String s, FileOutputStream log) throws IOException {
        System.err.println(s);
        log.write((s + "\n").getBytes());
    }

    private static class ComparableAddress implements Comparable<ComparableAddress> {

        private final String orig;
        private final String email;
        private final String name;

        public ComparableAddress(String orig) {
            this.orig = orig;
            if (orig.length() - orig.replaceAll("\\d", "").length() > 8) {
                throw new RuntimeException("To muc digits in " + orig);
            }
            this.email = cutEmail(orig);
            if (email.toLowerCase().contains("mailer-daemon@")) {
                throw new RuntimeException("from daemon " + orig);
            }
            this.name = cerateName(orig, email);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ComparableAddress) {
                return email.equalsIgnoreCase(((ComparableAddress) obj).email);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return email.hashCode();
        }

        @Override
        public String toString() {
            if (name == null || name.trim().equals("")) {
                return email;
            }
            return "\"" + name + "\" <" + email + ">";
        }

        public int compareTo(ComparableAddress o) {
            return this.email.compareTo(o.email);
        }

    }

    public static void main(String[] args) throws Exception {
        FileOutputStream allLines = new FileOutputStream("/home/jvanek/Desktop/allInes.txt");
        FileOutputStream filteredLines = new FileOutputStream("/home/jvanek/Desktop/filteredLines.txt");
        FileOutputStream possibleKeys = new FileOutputStream("/home/jvanek/Desktop/possibleKeywords.txt");
        FileOutputStream addresses = new FileOutputStream("/home/jvanek/Desktop/adresses.txt");
        FileOutputStream logs = new FileOutputStream("/home/jvanek/Desktop/logs.txt");
        FileOutputStream table = new FileOutputStream("/home/jvanek/Desktop/table.xml");
        drParser p = new drParser();
        List<String> all = p.getAtChars();
        List<String> filteredL = new ArrayList<String>();
        List<ComparableAddress> addressL = new ArrayList<ComparableAddress>();
        Set<String> possibleKeywords = new HashSet<String>();
        for (String s : all) {
            allLines.write((s + "\n").getBytes());
            String filtered = s.replace(":", " ");
            filtered = filtered.replace(",", " ");
            filtered = filtered.replace("\"", " ");
            filtered = filtered.replace("'", " ");
            filtered = filtered.replace("#", " ");
            filtered = filtered.replace("!", " ");
            filtered = filtered.replace("(", " ");
            filtered = filtered.replace(")", " ");
            filtered = filtered.replace(">", " ");
            filtered = filtered.replace("<", " ");
            filtered = filtered.replace("]", " ");
            filtered = filtered.replace("[", " ");
            filtered = filtered.replace("&", " ");
            filtered = filtered.replace("*", " ");
            filtered = filtered.replace("From ", " ");
            filtered = filtered.replace("To ", " ");
            filtered = filtered.replace("Komu ", " ");
            filtered = filtered.replace("Od ", " ");
            filtered = filtered.replace("mailto", " ");
            filtered = filtered.replace("Mailto", " ");
            filtered = filtered.replace("MailTo", " ");
            filtered = filtered.replace("Mail To", " ");
            filtered = filtered.replace("Mail to", " ");
            filtered = filtered.replace("mail to", " ");
            filtered = filtered.replace("Normální", " ");
            filtered = filtered.replace("e-mail ", "");
            filtered = filtered.replace("Příjemce ", "");
            filtered = filtered.replaceAll("\\s+", " ");
            String[] interFilter = filtered.split("\\s*SMTP\\s*");
            filteredL.addAll(Arrays.asList(interFilter));
            String[] ss = filtered.split("\\s+");
            for (String string : ss) {
                if (!string.contains("@")) {
                    possibleKeywords.add(string);
                }
            }
        }

        for (String s : filteredL) {
            filteredLines.write((s + "\n").getBytes());
            try {
                ComparableAddress ca = new ComparableAddress(s);
                ComparableAddress ccaa = found(ca, addressL);
                if (ccaa == null) {
                    addressL.add(ca);
                } else {
                    if (ca.name.length() > ccaa.name.length()) {
                        addressL.remove(ccaa);
                        addressL.add(ca);
                        logO(ccaa + " replaced by " + ca, logs);
                    }
                }
            } catch (RuntimeException r) {
                logE(r.toString(), logs);
            }
        }
        for (String string : possibleKeywords) {
            possibleKeys.write((string + "\n").getBytes());
        }

        Collections.sort(addressL);
        for (ComparableAddress s : addressL) {
            addresses.write((s.toString() + "\n").getBytes());
            table.write((getCalcXml(s) + "\n").getBytes());
        }
        logs.close();
        addresses.close();
        table.close();
        allLines.close();
        filteredLines.close();
        possibleKeys.close();
    }

    public drParser() {
    }

    private List<String> getAtChars() throws XMLStreamException, FileNotFoundException, IOException {
        InputStream fis = new FileInputStream("/home/jvanek/Desktop/dta/dorucena.xml");
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(fis);

//create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);

//get the root element (in this case the envelope)
        //OMDocument doc = builder.getDocument();
        OMElement root = builder.getDocumentElement();
        List<String> l = new ArrayList<String>();
        walThroughFor(root, l);
        fis.close();
        return l;

    }

    private void walThroughFor(OMElement root, List<String> l) throws XMLStreamException {
        Iterator<Object> r = root.getChildElements();
        int childs = 0;
        while (r.hasNext()) {
            childs++;
            OMNode node = (OMNode) r.next();
            if (node instanceof OMElement) {
                walThroughFor((OMElement) node, l);
            } else {
                System.err.println("worning " + node.getClass() + " found insteaad  of element");
            }

        }
        if (childs == 0) {
            String s = root.getText();
            if (s.contains("@")) {
                l.add(s);
            }
        }
    }

    private static String getCalcXml(ComparableAddress ca) {
        return ""
                + "<table:table-row table:style-name=\"ro1\">"
                + "  <table:table-cell office:value-type=\"string\">"
                + "    <text:p><![CDATA[" + ca.name + "]]></text:p>"
                + "  </table:table-cell>"
                + "  <table:table-cell office:value-type=\"string\">"
                + "    <text:p><![CDATA[" + ca.email + "]]></text:p>"
                + "  </table:table-cell>"
                + "</table:table-row>";
    }
}
