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

public class Index {

    public ArrayList topics;

    public static void main(String[] args) {
        try {
            Analyzer analyzer = new ModifiableTokenizer();
            IndexWriter iw = Utilities.GetIndexWriter(analyzer);
            parseLAT(iw);
            parseFBIS(iw);
            parseFTL(iw);
            parseFR(iw);
            System.out.println("Committing index....");
            iw.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * DOCNO
     * DATE
     * HEADLINE
     * BYLINE
     * TEXT 
     */
    private static void parseLAT(IndexWriter iw) {
        File dir = new File(Constants.LAT_LOC);
        File[] latFiles = dir.listFiles();
        if (latFiles != null) {
            for (File latFile : latFiles) {
                try {
                    System.out.println("Opening file: " + latFile.getName());
                    Scanner latFileScanner = new Scanner(latFile);
                    Document doc = new Document();
                    Boolean firstDoc = true;
                    String currLine = "";
                    String currFieldEntry = "";
                    while(latFileScanner.hasNextLine()) {
                        currLine = latFileScanner.nextLine();
                        switch(parseTag(currLine)) {
                            case Constants.DOC_OPEN: 
                                if (firstDoc) firstDoc = false;
                                else iw.addDocument(doc);
                                doc = new Document();
                                break;

                            case Constants.DOC_NO_OPEN: 
                                int endIndex = currLine.indexOf(Constants.DOC_NO_CLOSE);
                                String docNo = currLine.substring(Constants.DOC_NO_OPEN.length(), endIndex).trim();
                                Field numField = new TextField(Constants.DocTag.ID.toString(), docNo, Field.Store.YES);
                                doc.add(numField);
                                break;

                            case Constants.DATE_CLOSE: 
                                Field dateField = new TextField(Constants.DocTag.DATE.toString(), currFieldEntry, Field.Store.YES);
                                doc.add(dateField);
                                currFieldEntry = ""; 
                                break;

                            case Constants.HEADLINE_CLOSE:
                                Field headlineField = new TextField(Constants.DocTag.HEADLINE.toString(), currFieldEntry, Field.Store.YES);
                                doc.add(headlineField);
                                break;

                            case Constants.TEXT_CLOSE:
                                Field textField = new TextField(Constants.DocTag.TEXT.toString(), currFieldEntry, Field.Store.YES);
                                doc.add(textField);
                                break;

                            case Constants.DATE_OPEN:
                            case Constants.TEXT_OPEN:
                            case Constants.HEADLINE_OPEN:
                                currFieldEntry = "";
                                break;

                            case "":
                                currFieldEntry += currLine.trim();
                                break;

                            default:

                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

        }
    }

    private static void parseFBIS(IndexWriter iw) {

        File dir = new File(Constants.FBIS_LOC);
        File[] fbisFiles = dir.listFiles();
        if (fbisFiles != null) {
            for (File fbisFile : fbisFiles) {
                try {
                    System.out.println("Opening file: " + fbisFile.getName());
                    Scanner fbisFileScanner = new Scanner(fbisFile);
                    Document doc = new Document();
                    Boolean firstDoc = true;
                    Constants.DocTag currTag = Constants.DocTag.None;
                    String currLine = "";
                    String currFieldEntry = "";
                    while(fbisFileScanner.hasNextLine()) {
                        currLine = fbisFileScanner.nextLine();
                        switch(parseTag(currLine)) {
                            case Constants.DOC_OPEN: 
                                if (firstDoc) firstDoc = false;
                                else iw.addDocument(doc);
                                doc = new Document();
                                break;
                            case Constants.DOC_NO_OPEN: 
                                int endDocNoIndex = currLine.indexOf(Constants.DOC_NO_CLOSE);
                                String docNo = currLine.substring(Constants.DOC_NO_OPEN.length(), endDocNoIndex).trim();
                                Field numField = new TextField(Constants.DocTag.ID.toString(), docNo, Field.Store.YES);
                                doc.add(numField);
                                break;
                            case Constants.FBIS_DATE_OPEN: 
                                int endDocDateIndex = currLine.indexOf(Constants.FBIS_DATE_CLOSE);
                                String date = currLine.substring(Constants.FBIS_DATE_OPEN.length(), endDocDateIndex).trim();
                                Field dateField = new TextField(Constants.DocTag.DATE.toString(), date, Field.Store.YES);
                                doc.add(dateField);
                                currFieldEntry = ""; 
                                break;

                            case Constants.FBIS_HEADLINE_OPEN:
                                int endHeadlineIndex = currLine.indexOf(Constants.FBIS_HEADLINE_CLOSE);
                                if (endHeadlineIndex != -1) {
                                    String headline = currLine.substring((Constants.FBIS_HEADLINE_OPEN + " <TI>").length(), endHeadlineIndex);
                                    Field headlineField = new TextField(Constants.DocTag.HEADLINE.toString(), headline, Field.Store.YES);
                                    doc.add(headlineField);
                                }
                                break;

                            case Constants.TEXT_CLOSE:
                                Field textField = new TextField(Constants.DocTag.TEXT.toString(), currFieldEntry, Field.Store.YES);
                                doc.add(textField);
                                break;

                            case Constants.TEXT_OPEN:
                                currFieldEntry = "";
                                break;
                            case "":
                                currFieldEntry += " " + currLine.trim();
                                break;
                            default:

                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

        }
    }

    private static void parseFR(IndexWriter iw) {
        File dir = new File(Constants.FR_LOC);
        File[] frFolders = dir.listFiles();
        if (frFolders != null) {
            for (File frFolder : frFolders)
            {
                File[] frFiles = frFolder.listFiles();
                if (frFiles != null) {
                    for (File frFile : frFiles) {
                        try {
                            Scanner frFileScanner = new Scanner(frFile);
                            Document doc = new Document();
                            Boolean firstDoc = true;
                            String currLine = "";
                            String currFieldEntry = "";
                            while(frFileScanner.hasNextLine()) {
                                currLine = frFileScanner.nextLine();
                                switch(parseTag(currLine)) {
                                    case Constants.DOC_OPEN: 
                                        if (firstDoc) firstDoc = false;
                                        else iw.addDocument(doc);
                                        doc = new Document();
                                        break;

                                    case Constants.DOC_NO_OPEN: 
                                        int endDocNoIndex = currLine.indexOf(Constants.DOC_NO_CLOSE);
                                        String docNo = currLine.substring(Constants.DOC_NO_OPEN.length(), endDocNoIndex).trim();
                                        Field numField = new TextField(Constants.DocTag.ID.toString(), docNo, Field.Store.YES);
                                        doc.add(numField);
                                        break;

                                    case Constants.DATE_CLOSE: 
                                        Field dateField = new TextField(Constants.DocTag.DATE.toString(), currFieldEntry, Field.Store.YES);
                                        doc.add(dateField);
                                        break;

                                    case Constants.TEXT_CLOSE:
                                        Field textField = new TextField(Constants.DocTag.TEXT.toString(), currFieldEntry, Field.Store.YES);
                                        doc.add(textField);
                                        break;

                                    case Constants.TEXT_OPEN:
                                    case Constants.DATE_OPEN:
                                        currFieldEntry = "";
                                        break;

                                    case "":
                                        if (!currLine.trim().equals("EFFECTIVE DATE:")) //hacky way of managing the date
                                            currFieldEntry += " " + currLine.trim();
                                        break;
                                    default:

                                }
                            }
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {

        }
    }

    private static void parseFTL(IndexWriter iw) {

        File dir = new File(Constants.FT_LOC);
        File[] ftFolders = dir.listFiles();
        if (ftFolders != null) {
            for (File ftFolder : ftFolders)
            {
                File[] ftFiles = ftFolder.listFiles();
                if (ftFiles != null) {
                    for (File ftFile : ftFiles) {
                        try {
                            System.out.println("Opening file: " + ftFile.getName());
                            Scanner ftFileScanner = new Scanner(ftFile);
                            Document doc = new Document();
                            Boolean firstDoc = true;
                            Constants.DocTag currTag = Constants.DocTag.None;
                            String currLine = "";
                            String currFieldEntry = "";
                            while(ftFileScanner.hasNextLine()) {
                                currLine = ftFileScanner.nextLine();
                                switch(parseTag(currLine)) {
                                    case Constants.DOC_OPEN: 
                                        if (firstDoc) firstDoc = false;
                                        else iw.addDocument(doc);
                                        doc = new Document();
                                        break;

                                    case Constants.DOC_NO_OPEN: 
                                        int endDocNoIndex = currLine.indexOf(Constants.DOC_NO_CLOSE);
                                        String docNo = currLine.substring(Constants.DOC_NO_OPEN.length(), endDocNoIndex).trim();
                                        Field numField = new TextField(Constants.DocTag.ID.toString(), docNo, Field.Store.YES);
                                        doc.add(numField);
                                        break;

                                    case Constants.DATE_OPEN: 
                                        String date = currLine.substring(Constants.DATE_OPEN.length()).trim();
                                        Field dateField = new TextField(Constants.DocTag.DATE.toString(), date, Field.Store.YES);
                                        doc.add(dateField);
                                        break;

                                    case Constants.HEADLINE_CLOSE:
                                        Field headlineField = new TextField(Constants.DocTag.HEADLINE.toString(), currFieldEntry, Field.Store.YES);
                                        doc.add(headlineField);
                                        break;

                                    case Constants.TEXT_CLOSE:
                                        Field textField = new TextField(Constants.DocTag.TEXT.toString(), currFieldEntry, Field.Store.YES);
                                        doc.add(textField);
                                        break;

                                    case Constants.TEXT_OPEN:
                                    case Constants.HEADLINE_OPEN:
                                        currFieldEntry = "";
                                        break;
                                    case "":
                                        currFieldEntry += " " + currLine.trim();
                                        break;
                                    default:

                                }
                            }
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {

        }
    }

    private static String parseTag(String currLine) {
        if (currLine.isEmpty() || currLine.charAt(0) != '<') 
            return "";

        int closingBracketIndex = currLine.indexOf('>');

        if (closingBracketIndex == -1)
            return "";
        
        return currLine.substring(0, closingBracketIndex + 1);
    }
}
