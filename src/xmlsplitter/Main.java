/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlsplitter;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import xmlsplitter.impl.Splitter;
import xmlsplitter.utils.Commons;

/**
 *
 * @author jvanek
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static final Map<String, String> commandLineArgs = new HashMap<String, String>();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("-minSize=? maxSize=? divSize=? -config(=?) headerXpath=? -contentXpath=? -i=? -o(=?) -L=? -delimiter1=? -delimiter2=? ignoreUri(=??)");
            System.out.println("-minSize - half-optional. min xor max xor divSize must be set. Min size od output file-chunk (default kilobytes(k), b,m,g can be used)");
            System.out.println("-maxSize - half-optional. min xor max xor divSize must be set. Max size od output file-chunk (eg 100k is same as 100, 1m equals 1024k, 1024b is equals to 1 or to 1k )");
            System.out.println("-divSize - half-optional. min xor max xor divSize must be set. Number of chunks file will be splitted ");
            System.out.println("   Atleast one content element is always included.");
            System.out.println("   only one of ^^ should be declared (in fact, last one is used)");
            System.out.println("-config - config file with preset params. default is default.conf, commandline params owerwrite in-file ones. If not set, then hardcoded are used");
            System.out.println("-headerXpath - xpath expression defining header elements. Default is nothing");
            System.out.println("-contentXpath - xpath expression defining body of chunk. Default is /*");
            System.out.println("-footerXpath - xpath expression defining footer of chunk. Default is nothing");
            System.out.println("-o - optional. output directory for chunks. Default is working directory. When not set, output is std-out");
            System.out.println("-i - optional. inputfile. Defouult is std-in.");
            System.out.println("-L - optional. loging level. 0-(defoult) all messages go to error Stream. 1 - messages go to stdout, 2- no messages");
            System.out.println("-delimiter1 - optional. output files have name: inputFile_without_sufixDELIMITER1chunkNumberDEIMITER2.suffix default _part_");
            System.out.println("-delimiter2 - optional. output files have name: inputFile_without_sufixDELIMITER1chunkNumberDEIMITER2.suffix default is none");
            System.out.println("-ignoreUri -this is mechanism should help to use namespaces in XPaths. -ignoreuri or with arg ALL (-ignoreUri=ALL) will cause program to explicitly map all nmespaces in documetn. However - default namespaces are NOT mapped by this way.");
            System.out.println("            When you need to declare more namespaces (eg to map default namespaces) you must use possibility to write down as arg spcaes splitted text whic is describing prefixes and URIs. eg -ignoreUri=ss http://uri1/x pp fftp://uri1.xy .... (dont forget to protect spaces efor bash and to use same mappings in your xpaths)");
            System.out.println("             ALL cnan be used with ^^ eg.: \"-igonreURI=ALL prfix1 uri1 prefix2 uri2....\"");
            System.out.println("            please note, that the only possible way to proceed namespaces (and default ones) in xpath are prefixes in xpath. thing to work with namespaces is to write them to xpath PREFIX1:ELEMENT2/PREFIX3:ELEMENT4.... Or URI1:ELEMENT2/URI3:ELEMENT4 or mixture of uris/prefixes ");
            System.out.println("            namespaces is declared as prefix:uri in xml files in attribute xmlns) Default uri is without prefix and ust be mapped explicitly");
            System.out.println("-version - prints out program version");
            System.out.println("");
            System.out.println("Program by Vaněk Jiří, judovana@email.cz");
            System.exit(0);
        }

        Splitter splitter = new Splitter();

        for (String arg : args) {
            if (arg.replaceAll("-", "").equalsIgnoreCase("version")) {
                System.out.println("XmlSplitter version " + Splitter.VERSION);
                System.exit(0);
            }
            Entry<String, String> e = Commons.paarseCommandLineArg(arg);
            commandLineArgs.put(e.getKey(), e.getValue());
        }

        if (commandLineArgs.containsKey("l")) {
            if (commandLineArgs.get("l") == null) {
                splitter.setLogging(new Integer("0"));
            } else {
                splitter.setLogging(new Integer(commandLineArgs.get("l")));
            }
        }

        if (commandLineArgs.containsKey("config")) {
            if (commandLineArgs.get("config") == null) {
                //extracts full path to about.jnlp
                ClassLoader cl = Main.class.getClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                String s = cl.getResource("xmlsplitter/Main.class").toString();
                if (s.contains("build/classes")) {
                    s = s.substring(s.indexOf(":") + 1);
                    s = s.substring(0, s.indexOf("build/classes") - 1);
                    s = s + "/XMLsplitter.jar";
                } else {
                    s = s.substring(0, s.indexOf("!"));
                    s = s.substring(s.indexOf(":") + 1);
                    s = s.substring(s.indexOf(":") + 1);
                }
                s = "file://" + s.replace("XMLsplitter.jar", "default.conf");

                splitter.loadConfig(new URL(s));
            } else {
                splitter.loadConfig(new File(commandLineArgs.get("config")));
            }
        }
        Commons.proceedArgs(splitter, commandLineArgs);

        splitter.checkIntegrity();
        splitter.proceed();
    }
}
