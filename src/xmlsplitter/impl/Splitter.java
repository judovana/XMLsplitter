/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsplitter.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.collections.map.HashedMap;
import org.jaxen.JaxenException;

import xmlsplitter.utils.Commons;

/**
 *
 * @author jvanek
 */
public class Splitter {

    public static final String VERSION = "2.1";
    private int logging = 0;
    private String headerXpath = null;
    private String contentXpath = "/*";
    private String footerXpath = null;
    private File input = null;
    private File output = null;
    private Integer minSize = null;
    private Integer maxSize = null;//in bytes!
    private Integer divSize = null;
    private static final int SIZER = 1;
    private String delimiter1 = "_part_";
    private String delimiter2 = "";
    private String ignoreUri = null;

    public Splitter() {
    }

    public void openEleemtnsList(List<OMElement> currentParents, StringBuilder sb) {
        for (int j = currentParents.size() - 1; j >= 0; j--) {
            OMElement oMElement = currentParents.get(j);
            sb.append(writeOpeningElement(oMElement)).append("\n");
            //  loclalLog("opening: "+oMElement.getLocalName());
        }
    }

    public void closeEleemtnsList(List<OMElement> currentParents, StringBuilder sb) {
        for (OMElement oMElement : currentParents) {
            sb.append(writeClosingElement(oMElement)).append("\n");
            //   loclalLog("closing: "+oMElement.getLocalName());
        }
    }

    public void proceed() throws Exception {
        loclalLog("=======PROCESSING=======");
        if (input == null) {
            proceed(System.in);
        } else {
            loclalLog("parsing " + input.getAbsolutePath());
            proceed(new FileInputStream(input));
        }

    }

    public void proceed(InputStream is) throws XMLStreamException, JaxenException, UnsupportedEncodingException, IOException {
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(is);

//create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);

//get the root element (in this case the envelope)
        OMDocument doc = builder.getDocument();
        OMElement root = builder.getDocumentElement();

        long headerSize = 0;
        long footerSize = 0;
        long contentSize = 0;
        List<OMElement> headerList = new ArrayList<OMElement>(0);
        List<OMElement> footerList = new ArrayList<OMElement>(0);
        List<OMElement> contentList = new ArrayList<OMElement>(0);

        if (headerXpath != null) {
            AXIOMXPath xpathExpression = new AXIOMXPath(headerXpath);
            if (ignoreUri != null) {
                if (ignoreUri.contains("ALL")) {
                    applyAllnamespaces(xpathExpression, root);
                }
                if (ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim().length() > 2) {
                    applySelectedNamespaces(xpathExpression, ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim(), root, doc.getOMFactory());
                }
            }

            List nodeList = xpathExpression.selectNodes(root);

            if (nodeList.isEmpty()) {
                loclalLog("warning: header list have 0 nodes!");
            } else {
                loclalLog("header list have " + nodeList.size() + " nodes!");
                headerList = new ArrayList<OMElement>(nodeList.size());
                for (Iterator it = nodeList.iterator(); it.hasNext();) {
                    Object object = it.next();
                    if (object instanceof OMElement) {
                        headerSize += SIZER * ((OMElement) object).toString().length();
                        headerList.add((OMElement) object);
                    }

                }
                loclalLog("header size is " + headerSize + " bytes!");
            }
        }
        if (footerXpath != null) {
            AXIOMXPath xpathExpression = new AXIOMXPath(footerXpath);
            if (ignoreUri != null) {
                if (ignoreUri.contains("ALL")) {
                    applyAllnamespaces(xpathExpression, root);
                }
                if (ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim().length() > 2) {
                    applySelectedNamespaces(xpathExpression, ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim(), root, doc.getOMFactory());
                }
            }

            List nodeList = xpathExpression.selectNodes(root);
            if (nodeList.isEmpty()) {
                loclalLog("warning: footer list have 0 nodes!");
            } else {
                loclalLog("footer list have " + nodeList.size() + " nodes!");
                footerList = new ArrayList<OMElement>(nodeList.size());
                for (Iterator it = nodeList.iterator(); it.hasNext();) {
                    Object object = it.next();
                    if (object instanceof OMElement) {
                        footerSize += SIZER * ((OMElement) object).toString().length();
                        footerList.add((OMElement) object);
                    }

                }
                loclalLog("footer size is " + footerSize + " bytes!");
            }
        }

        if (contentXpath != null) {
            AXIOMXPath xpathExpression = new AXIOMXPath(contentXpath);

            if (ignoreUri != null) {
                if (ignoreUri.contains("ALL")) {
                    applyAllnamespaces(xpathExpression, root);
                }
                if (ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim().length() > 2) {
                    applySelectedNamespaces(xpathExpression, ignoreUri.replaceAll("\\s*ALL\\s*", " ").trim(), root, doc.getOMFactory());
                }
            }

            List nodeList = xpathExpression.selectNodes(root);
            if (nodeList.isEmpty()) {
                loclalLog("warning: content list have 0 nodes!");
            } else {
                loclalLog("content list have " + nodeList.size() + " nodes!");
                contentList = new ArrayList<OMElement>(nodeList.size());
                for (Iterator it = nodeList.iterator(); it.hasNext();) {
                    Object object = it.next();
                    if (object instanceof OMElement) {
                        contentSize += SIZER * ((OMElement) object).toString().length();
                        contentList.add((OMElement) object);
                    }

                }
                loclalLog("content size is " + contentSize + " bytes! (~" + contentSize / 1024 / 1024 + "m)");
            }
        }

//list and sizes calculated; now walk inteligently through contents.
//add root, add headers, add content with all parents, close parentsadd foters, close root
        long controlFlush = 0;
        long currentFileSize = 0;
        int chNumber = 0;
        List<OMElement> buffer = null;

        for (int i = 0; i < contentList.size(); i++) {

            //fill buffer with content until "size"
            OMElement oMElement = contentList.get(i);
            long currentChunkSize = oMElement.toString().length();

            if (currentFileSize == 0) {
                currentFileSize = footerSize + headerSize;
                buffer = new ArrayList<OMElement>(0);
            }

            //when size is reached  flush
            if (divSize != null && buffer.isEmpty()) {

                if (divSize != 0) {//this is really rare and will cause very small last file instead off div by zerro eror.
                    long nextSize = (contentSize - controlFlush) / divSize;
                    divSize--;
                    minSize = (int) nextSize;
                }
            }

            if (minSize != null) {

                if (currentFileSize < minSize || buffer.isEmpty()) {
                    currentFileSize += currentChunkSize;
                    controlFlush += currentChunkSize;
                    buffer.add(oMElement);
                } else {
                    i--;
                    loclalLog("saving chunk " + chNumber + " size " + currentFileSize + "bytes in " + buffer.size() + " chunks. Total: " + controlFlush + " " + (100 * controlFlush / contentSize) + "%");
                    currentFileSize = 0;
                    chNumber++;
                    flush(headerList, footerList, buffer, doc, root, chNumber);

                }
            }

            if (maxSize != null) {

                if (currentFileSize + currentChunkSize < maxSize || buffer.isEmpty()) {
                    currentFileSize += currentChunkSize;
                    controlFlush += currentChunkSize;
                    buffer.add(oMElement);
                } else {
                    i--;
                    loclalLog("saving chunk " + chNumber + " size " + currentFileSize + "bytes in " + buffer.size() + " chunks Total: " + controlFlush + " " + (100 * controlFlush / contentSize) + "%");
                    currentFileSize = 0;
                    chNumber++;
                    flush(headerList, footerList, buffer, doc, root, chNumber);

                }
            }

        }
//save rest

        if (buffer != null && buffer.size() > 0) {

            loclalLog("saving chunk " + chNumber + " size " + currentFileSize + "bytes in " + buffer.size() + " chunks Total: " + controlFlush + " " + (100 * controlFlush / contentSize) + "%");
            currentFileSize = 0;
            chNumber++;
            flush(headerList, footerList, buffer, doc, root, chNumber);

        }

    }

    public void loadConfig(URL uRL) {
        InputStream is = null;
        try {
            is = uRL.openStream();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (is == null) {
            loclalLog("config file do not exists: " + uRL.toString());
            return;
        }
        try {
            loclalLog("reading: " + uRL.toString());
            loadConfig(is);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public void loadConfig(File file) {
        if (!file.exists()) {
            loclalLog("config file do not exists: " + file.getAbsolutePath());
        }
        try {
            loclalLog("reading: " + file.getAbsolutePath());
            loadConfig(new FileInputStream(file));
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public void loadConfig(InputStream is) throws IOException {

        loadConfig(new InputStreamReader(is, "utf-8"));
    }

    public void loadConfig(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        try {
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                try {
                    Commons.proceedArg(this, Commons.paarseCommandLineArg(s));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            br.close();
        }

    }

    public void setMinSize(String value) {
        this.maxSize = null;
        divSize = null;
        this.minSize = measuredValue(value);
        loclalLog("Minsize setted: " + minSize);
    }

    //default unit is kilos
    private Integer measuredValue(String s) {
        if (s == null) {
            return null;
        }
        if (s.trim().length() == 0) {
            return null;
        }

        if (Character.isDigit(s.charAt(s.length() - 1))) {
            return 1024 * (Integer.parseInt(s));
        }

        switch (s.toLowerCase().charAt(s.length() - 1)) {
            case 'b':
                return new Integer(s.substring(0, s.length() - 1));
            case 'k':
                return 1024 * Integer.parseInt(s.substring(0, s.length() - 1));
            case 'm':
                return 1024 * 1024 * new Integer(s.substring(0, s.length() - 1));
            case 'g':
                return 1024 * 024 * 1024 * new Integer(s.substring(0, s.length() - 1));
            default:

                loclalLog("unknown unit [b,k,m,g] : " + s.charAt(s.length() - 1));
                return null;

        }

    }

    public void setMaxSize(String value) {
        this.minSize = null;
        divSize = null;
        this.maxSize = measuredValue(value);
        loclalLog("Maxsize setted: " + maxSize);
    }

    public void setDivSize(String value) {
        minSize = null;
        maxSize = null;
        divSize = new Integer(value);
        loclalLog("divSze setted: " + divSize);
    }

    public void setInput(File file) {
        this.input = file.getAbsoluteFile();
        loclalLog("input file setted: " + file.getAbsolutePath());
    }

    public void setOutputDir(File file) {
        this.output = file.getAbsoluteFile();
        loclalLog("output dir setted: " + file.getAbsolutePath());
    }

    public void setHeaderXpath(String string) {
        this.headerXpath = string;
        loclalLog("header xpath setted: " + headerXpath);
    }

    public void setContentXpath(String string) {
        this.contentXpath = string;
        loclalLog("content xpath setted: " + contentXpath);
    }

    public void setFooterXpath(String string) {
        this.footerXpath = string;
        loclalLog("footer xpath setted: " + footerXpath);
    }

    public void checkIntegrity() {

        switch (logging) {
            case 0:
                loclalLog("logging: errorstream");
                break;
            case 1:
                loclalLog("logging: std-out");
                break;
            default:
                loclalLog("logging: silent");
                break;
        }
        loclalLog("delimiter1 set " + delimiter1);
        loclalLog("delimiter2 set " + delimiter2);
        if (headerXpath == null) {
            loclalLog("header xpath not set");
        } else {
            loclalLog("header xpath: " + headerXpath);
        }
        if (contentXpath == null) {
            loclalLog("content xpath not set");
        } else {
            loclalLog("content xpath: " + contentXpath);
        }
        if (footerXpath == null) {
            loclalLog("footer xpath not set");
        } else {
            loclalLog("footer xpath: " + footerXpath);
        }
        if (input == null) {
            loclalLog("input file not set. Used std-in");
        } else {
            loclalLog("input file: " + input.getAbsolutePath());
            if (!input.exists()) {
                throw new IllegalArgumentException("input file: " + input.getAbsolutePath() + "does NOT exists!");
            } else {
                loclalLog("exists");
            }
        }
        if (output == null) {
            loclalLog("output not set, used std-out");
        } else {
            loclalLog("output directory: " + output);
            if (!output.exists()) {
                throw new IllegalArgumentException("output dir: " + output.getAbsolutePath() + "does NOT exists!");
            } else {
                loclalLog("exists");
            }
        }
        if (minSize == null && maxSize == null && divSize == null) {
            throw new IllegalArgumentException("at least one of min,max,div sizes must be set (better exactly one)");
        }
        if (minSize == null) {
            loclalLog("minsize is not set");
        } else {
            loclalLog("minsize is set to : " + minSize + " bytes");
        }
        if (maxSize == null) {
            loclalLog("maxsize is not set");
        } else {
            loclalLog("max size is set to: " + maxSize + " bytes");
        }
        if (divSize == null) {
            loclalLog("divsize is not set");
        } else {
            loclalLog("file will be splitted to: " + divSize + " chunks");
        }

    }

    public void setLogging(int integer) {
        this.logging = integer;
        loclalLog("logging set to: " + logging);
    }

    public int getLogging() {
        return logging;
    }

    private void loclalLog(String string) {
        Commons.log(string, logging);
    }

    private void flush(List<OMElement> headerList, List<OMElement> footerList, List<OMElement> buffer, OMDocument doc, OMElement root, int chNumber) throws IOException {
        //Determine possible name

        String file = "splitted_" + System.currentTimeMillis() + ".xml";
        String suffix = file.substring(file.lastIndexOf("."));
        String pureName = file.substring(0, file.lastIndexOf("."));

        if (input != null) {
            file = input.getName();

        }
        suffix = file.substring(file.lastIndexOf("."));
        pureName = file.substring(0, file.lastIndexOf("."));

        OutputStreamWriter os = new OutputStreamWriter(System.out, "UTF-8");
        //determine output
        if (output != null) {
            File f = new File(output, pureName + delimiter1 + Commons.spacing(chNumber) + delimiter2 + suffix);
            os = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        }

        //flush processing instructions of document
        // os.write(writeEncodig(doc.getCharsetEncoding()));
        os.write(writeVersion(doc.getXMLVersion()));
        //os.write(writeEncodigAndVersion(doc.getCharsetEncoding(),doc.getXMLVersion()));

        Iterator children = doc.getChildren();
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();

            if (node instanceof OMProcessingInstruction) {
                os.write(writeProcessingInstruction((OMProcessingInstruction) node));
            }
        }

//        //root
//        os.write(writeOpeningElement(root));
//        //header with parents
//        os.write(writeBufferWithParents(headerList, root));
//        //parents of contents
//        //contents
//        os.write(writeBufferWithParents(buffer, root));
//        //close parents
//        //footer with parents
//        os.write(writeBufferWithParents(footerList, root));
//        //close root/
//        os.write(writeClosingElement(root));
        List<OMElement> all = new ArrayList<OMElement>(headerList.size() + buffer.size() + footerList.size());
        all.addAll(headerList);
        all.addAll(buffer);
        all.addAll(footerList);
        os.write(writeBufferWithParents(all));

        os.flush();
        if (output != null) {
            os.close();
        }

    }

    public void setDelimiter1(String value) {
        this.delimiter1 = value;
    }

    public void setDelimiter2(String value) {
        this.delimiter2 = value;
    }

    public String writeOpeningElement(OMElement root) {
        StringBuilder sb = new StringBuilder("<");

        {
            String nmsc = root.getQName().getPrefix();
//        if (nmsc==null || nmsc.trim().equals("")){
//            nmsc=root.getQName().getNamespaceURI();
//        }
            if (nmsc == null || nmsc.trim().equals("")) {
                nmsc = "";
            } else {
                nmsc += ":";
            }
            sb.append(nmsc).append(root.getQName().getLocalPart()).append(" ");
        }

        OMNamespace def = root.getDefaultNamespace();
        if (def != null) {
            Object parent = root.getParent();
            if (!(parent instanceof OMElement)) {
                parent = null;
            }
            if (parent != null && ((OMElement) parent).getDefaultNamespace() != null && def.getNamespaceURI().equals(((OMElement) parent).getDefaultNamespace().getNamespaceURI())) {
                //parent have same default uri
            } else {
                sb.append("xmlns='").append(def.getNamespaceURI()).append("'");
            }
        }

        Iterator ii = root.getAllDeclaredNamespaces();
        for (; ii.hasNext();) {
            OMNamespace o = (OMNamespace) ii.next();
            if (o.getPrefix() == null || o.getPrefix().trim().equals("")) {
                continue;
            }
            boolean foriden = false;
            if (root.getParent() != null && root.getParent() instanceof OMElement) {
                foriden = isInParent(o, ((OMElement) root.getParent()));
            }
            if (!foriden) {
                sb.append(" xmlns:").append(o.getPrefix());
                String ch = "'";
                if (o.getNamespaceURI().contains("'")) {
                    ch = "\"";
                }
                sb.append("=").append(ch);
                sb.append(o.getNamespaceURI());
                sb.append(ch);
            }

        }

        Iterator i = root.getAllAttributes();
        for (; i.hasNext();) {
            OMAttribute o = (OMAttribute) i.next();

            String nmsc = o.getQName().getPrefix();
//        if (nmsc==null || nmsc.trim().equals("")){
//            nmsc=root.getQName().getNamespaceURI();
//        }
            if (nmsc == null || nmsc.trim().equals("")) {
                nmsc = "";
            } else {
                nmsc += ":";
            }

            sb.append(nmsc).append(o.getQName().getLocalPart());
            String ch = "'";
            if (o.getAttributeValue().contains("'")) {
                ch = "\"";
            }
            sb.append("=").append(ch);
            sb.append(o.getAttributeValue());
            sb.append(ch).append(" ");

        }

        sb.append(">");

        return sb.toString();
    }

    private boolean isInParent(OMNamespace n1, OMElement oMElement) {
        Iterator ii = oMElement.getAllDeclaredNamespaces();
        for (; ii.hasNext();) {
            OMNamespace n2 = (OMNamespace) ii.next();
            if (n1.getPrefix().equals(n2.getPrefix()) && n1.getNamespaceURI().equals(n2.getNamespaceURI())) {
                return true;
            }
        }
        return false;
    }

    public String writeClosingElement(OMElement root) {
        StringBuilder sb = new StringBuilder("</");
        String p = root.getQName().getPrefix();
        if (p != null && !p.trim().equals("")) {
            sb.append(p).append(":");
        }

        sb.append(root.getQName().getLocalPart());
        sb.append(">");

        return sb.toString();
    }

    private String writeBufferWithParents(List<OMElement> elist) {
        return writeBufferWithParents(elist, null);
    }

    private String writeBufferWithParents(List<OMElement> elist, OMElement root) {
        List<OMElement> parentList = null;

        StringBuilder sb = new StringBuilder();

        for (OMElement el : elist) {
            List<OMElement> currentParents = getParents(el/*, root*/);
//            String s="";
//            for (int j = 0; j < currentParents.size(); j++) {
//                OMElement oMElement = currentParents.get(j);
//                s=s+j+": "+oMElement.getLocalName()+" ";
//
//            }
//            loclalLog(s);

            if (parentList == null) {
                openEleemtnsList(currentParents, sb);
                parentList = currentParents;
            } else {
//                boolean compared = compareElementListsByName(currentParents, parentList);
//                if (!compared)
                {
                    List<OMElement> discarded = new LinkedList<OMElement>();
//                     System.out.println("XXXXXXXXXXXXXX");
//                     for (int j = 0; j < parentList.list.size(); j++) {
//                        OMElement oMElement = parentList.list.get(j);
//                         System.out.println("pl"+j+" "+oMElement.getLocalName());
//                    }
//                         for (int j = 0; j < currentParents.size(); j++) {
//                        OMElement oMElement = currentParents.get(j);
//                         System.out.println("cl"+j+" "+oMElement.getLocalName());
//                    }
                    int j = parentList.size() - 1;
                    int jj = currentParents.size() - 1;
                    while (true) {
                        if (j < 0) {
                            break;
                        }
                        if (jj < 0) {
                            break;
                        }
                        if (parentList.get(j).getQName().toString().equals(currentParents.get(jj).getQName().toString())) {
                            discarded.add(currentParents.get(jj));
                            parentList.remove(j);
                            currentParents.remove(jj);
                        //j++;
                            //jj++;
                        } else {
                            break;
                        }
                        j--;
                        jj--;

                    }
//                     for (int w = 0; w < discarded.size(); w++) {
//                        OMElement oMElement = discarded.get(w);
//                        System.out.println("dl"+w+" "+oMElement.getLocalName());
//                    }
                    closeEleemtnsList(parentList, sb);
                    parentList = currentParents;
                    openEleemtnsList(parentList, sb);

                    for (int k = discarded.size() - 1; k >= 0; k--) {
                        OMElement e = discarded.get(k);
                        parentList.add(e);
                    }
//                    for (int w = 0; w < parentList.list.size(); w++) {
//                        OMElement oMElement = parentList.list.get(w);
//                         System.out.println("pl"+w+" "+oMElement.getLocalName());
//                    }
                }
            }
            //          loclalLog("writeing "+el.getLocalName());
            sb.append(el.toString());
        }

        if (parentList != null) {
            closeEleemtnsList(parentList, sb);
        }
        return sb.toString();
    }
//from element to root, both exclused

    private List<OMElement> getParents(OMElement el, OMElement root) {
        List<OMElement> result = new LinkedList<OMElement>();

        OMElement parent = el;
        while (true) {
            try {
                parent = (OMElement) parent.getParent();
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
            if (parent.getQName().toString().equals(root.getQName().toString())) {
                break;
            }
            result.add(parent);

        }

        return result;
    }

    //from element to root, root included, eleemnt excluded
    private List<OMElement> getParents(OMElement el) {
        List<OMElement> result = new LinkedList<OMElement>();

        OMElement parent = el;
        while (true) {

            Object oparent = parent.getParent();
            if (oparent instanceof OMElement) {
                parent = (OMElement) oparent;
            } else {
                break;
            }

            result.add(parent);

        }

        return result;
    }

    private boolean compareElementListsByName(List<OMElement> currentParents, List<OMElement> parentList) {
        if (currentParents.size() != parentList.size()) {
            return false;
        }
        for (int i = 0; i < parentList.size(); i++) {
            OMElement e1 = parentList.get(i);
            OMElement e2 = currentParents.get(i);
            if (!e1.getQName().getLocalPart().equals(e2.getQName().getLocalPart())) {
                return false;
            }

        }

        return true;
    }

    private String writeEncodig(String charsetEncoding) {
        return "<?xml encoding='" + charsetEncoding + "' ?>";
    }

    private String writeVersion(String V) {
        return "<?xml version='" + V + "' ?>";
    }

    private String writeEncodigAndVersion(String charsetEncoding, String V) {
        return "<?xml encoding='" + charsetEncoding + "'  version='" + V + "' ?>";
    }

    private String writeProcessingInstruction(OMProcessingInstruction i) {
        return "<?" + i.getTarget() + " " + i.getValue() + "?>";
    }

    public void setIgnoreUri(String value) {
        ignoreUri = value;
    }

    public void applyAllnamespaces(AXIOMXPath xpathExpression, OMElement root) throws JaxenException {
        loclalLog("gathering namespaces");
        List<OMElement> l = gatherNamespaces(root);

        for (OMElement ee : l) {
            xpathExpression.addNamespaces(ee);

        }
        try {
            Map a = xpathExpression.getNamespaces();
            Set s = a.entrySet();
            for (Object object : s) {
                HashedMap.Entry he = (Entry) object;
                String key = (String) he.getKey();
                String value = (String) he.getValue();
                loclalLog(key + ":" + value);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private List<OMElement> gatherNamespaces(OMElement e) {

        List<OMElement> r = new LinkedList();
        Iterator children = e.getChildElements();

        while (children.hasNext()) {
            Object node = children.next();
            if (node instanceof OMComment) {
            } else if (node instanceof OMElement) {
                r.add(((OMElement) node));
                r.addAll(gatherNamespaces((OMElement) node));
            }
        }
        return r;

    }

    public void applySelectedNamespaces(AXIOMXPath xpathExpression, String ns, OMContainer root, OMFactory factory) throws JaxenException {
        String[] nss = ns.split("\\s+");
        for (int i = 0; i < nss.length; i += 2) {
            String prefix = nss[i];
            String uri = nss[i + 1];

            OMElement e = new OMElementImpl(new QName(uri, "foo", prefix), root, factory) {
            };
            loclalLog("applying namespace " + e.getQName().getPrefix() + ":" + e.getQName().getNamespaceURI());
            xpathExpression.addNamespaces(e);

        }
    }
}
