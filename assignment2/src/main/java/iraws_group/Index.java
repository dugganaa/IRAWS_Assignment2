package iraws_group;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import iraws_group.Constants;
import iraws_group.ModifiableTokenizer;
import org.apache.lucene.analysis.Analyzer;

import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Index {

    public ArrayList topics;

    public static void main(String[] args) {
        try {
            //Instantiate the custom analyzer which will be used by the IndexWriter
            Analyzer analyzer = new ModifiableTokenizer();
            IndexWriter iw;

            //In general, there will only be one cl arg specifiying whether the code should index or search. 
            //Passing more arguments manually specifies the ranking function, weighting value and output file where appropriate.
            if (args.length > 1) {
                iw = Utilities.GetIndexWriter(analyzer, Constants.SimilarityClasses.values()[Integer.parseInt(args[1])], Float.parseFloat(args[2]));
            }
            else {
                iw = Utilities.GetIndexWriter(analyzer);
            }

            //Each of the corpora are parsed and indexed using the same IndexWriter
            System.out.println("Indexing LAT...");
            File latDir = new File(Constants.LAT_LOC);
            File[] latFiles = latDir.listFiles();
            for (File latFile : latFiles) {
                IndexFile(latFile, Utilities.GetLATTags(), iw);
            }

            System.out.println("Indexing FBIS...");
            File fbisDir = new File(Constants.FBIS_LOC);
            File[] fbisFiles = fbisDir.listFiles();
            for (File fbisFile : fbisFiles) {
                IndexFile(fbisFile, Utilities.GetFBISTags(), iw);
            }

            System.out.println("Indexing FT...");
            File ftlDir = new File(Constants.FT_LOC);
            File[] ftlFolders = ftlDir.listFiles();
            if (ftlFolders != null) {
                for (File ftlFolder : ftlFolders)
                {
                    File[] ftlFiles = ftlFolder.listFiles();
                    if (ftlFiles != null) {
                        for (File ftlFile : ftlFiles) {
                            IndexFile(ftlFile, Utilities.GetFTLTags(), iw);
                        }
                    }
                }
            }

            System.out.println("Indexing FR...");
            File frDir = new File(Constants.FR_LOC);
            File[] frFolders = frDir.listFiles();
            if (frFolders != null) {
                for (File frFolder : frFolders)
                {
                    File[] frFiles = frFolder.listFiles();
                    if (frFiles != null) {
                        for (File frFile : frFiles) {
                            IndexFile(frFile, Utilities.GetFRTags(), iw);
                        }
                    }
                }
            }
            iw.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param file The file of documents to be indexed
     * @param tags The mappings from corpus document tags to a tagging convention
     * @param iwc  The IndexWriter to write each document to the index
     */
    private static void IndexFile(File file, HashMap<String, Constants.DocTag> tags, IndexWriter iwc) {
        try {
            //Excludes README files in the directory
            if (!file.getName().contains("read")) {
                //Each file is read directly into a String, then parsed into a Jsoup Document. 
                org.jsoup.nodes.Document docFile = Jsoup.parse(getFileContentsAsString(file.getAbsolutePath()));
                Elements docs = docFile.select("DOC");
                //Iterate through each document, which is always enclosed in DOC tags
                for (Element doc : docs) {
                    Document indexDoc = new Document();
                    //While each document may have a title, body etc, documents in different corpora have different tags representing them
                    //The hashmap 'tags' maps the tag from each corpus to a tag continous across all corpora
                    //eg: 
                    // SIGNER -> AUTHOR
                    // BYLINE -> AUTHOR
                    for (String tag : tags.keySet()) {
                        Elements es = doc.select(tag);
                        //The text within each tag is parsed as a textfield and added to the document
                        for(Element e : es) {
                            Field f = new TextField(tags.get(tag).toString(), e.text(), Field.Store.YES);
                            indexDoc.add(f);
                        }
                    }
                    //Index the document
                    iwc.addDocument(indexDoc);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileiPathString The absolute path to a file
     * @return                The entire contents of the file as a String
     */
    private static String getFileContentsAsString(String filePathString) {
        try {
            return Files.readString(Path.of(filePathString), StandardCharsets.ISO_8859_1);
        }
        catch(Exception e) {
            System.out.println("Exception thrown while getting file contents as string.\nFile path: " + filePathString + "\nException: " + e.toString());
            return "";
        }
    }
}
