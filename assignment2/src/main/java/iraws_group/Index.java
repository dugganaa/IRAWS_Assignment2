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
            Analyzer analyzer = new ModifiableTokenizer();
            IndexWriter iw;
            if (args.length > 1) {
                iw = Utilities.GetIndexWriter(analyzer, Constants.SimilarityClasses.values()[Integer.parseInt(args[1])], Float.parseFloat(args[2]));
            }
            else {
                iw = Utilities.GetIndexWriter(analyzer);
            }

            System.out.println("Indexing LAT...");
            File latDir = new File(Constants.LAT_LOC);
            File[] latFiles = latDir.listFiles();
            for (File latFile : latFiles) {
                IndexFile(latFile, Utilities.GetLATTags(), iw, Constants.Corpora.LAT);
            }

            System.out.println("Indexing FBIS...");
            File fbisDir = new File(Constants.FBIS_LOC);
            File[] fbisFiles = fbisDir.listFiles();
            for (File fbisFile : fbisFiles) {
                IndexFile(fbisFile, Utilities.GetFBISTags(), iw, Constants.Corpora.FBIS);
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
                            IndexFile(ftlFile, Utilities.GetFTLTags(), iw, Constants.Corpora.FTL);
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
                            IndexFile(frFile, Utilities.GetFRTags(), iw, Constants.Corpora.FR);
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

    private static void IndexFile(File file, HashMap<String, Constants.DocTag> tags, IndexWriter iwc, Constants.Corpora corpus) {
        try {
            if (!file.getName().contains("read")) {
                org.jsoup.nodes.Document docFile = Jsoup.parse(getFileContentsAsString(file.getAbsolutePath()));
                Elements docs = docFile.select("DOC");
                for (Element doc : docs) {
                    Document indexDoc = new Document();
                    for (String tag : tags.keySet()) {
                        Elements es = doc.select(tag);
                        for(Element e : es) {
                            Field f = new TextField(tags.get(tag).toString(), e.text(), Field.Store.YES);
                            indexDoc.add(f);
                        }
                    }
                    iwc.addDocument(indexDoc);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

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
