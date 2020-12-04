package iraws_group;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import iraws_group.Constants;
import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.StringReader;

public class Index {

    public ArrayList topics;

    public static void main(String[] args) {
        System.out.println("In index.java");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        ParseTopics();
    }

    public static void ParseTopics() {
        try {
            File topicsFile = new File(Constants.REL_TOPICS_LOC);
            Scanner topicsReader = new Scanner(topicsFile);

            String currLine = "";
            String currFieldEntry = "";
            Document doc = new Document();
            Field field;
            Constants.Tag currTag = Constants.Tag.None;

            //Just putting them into a list for now instead of indexing the docs
            //to make sure it's parsing okay
            ArrayList<Document> documents = new ArrayList<Document>();
            while (topicsReader.hasNextLine()) {
                currLine = topicsReader.nextLine();

                if (currLine.isEmpty())
                    continue;

                Constants.Tag tagOnCurrLine = getTag(currLine);
                switch(tagOnCurrLine) {
                    case Open: 
                        if (currTag != Constants.Tag.None) documents.add(doc);
                        doc = new Document();
                        currTag = Constants.Tag.Open;
                        break;
                    case Num: 
                        Field numField = new TextField(Constants.Tag.Num.toString(), currLine.split(" ")[2].trim(), Field.Store.YES);
                        doc.add(numField);
                        System.out.println("Adding Num: " + currLine.split(" ")[2].trim() );
                        currTag = Constants.Tag.Num;
                        break;
                    case Title: 
                        Field titleField = new TextField(Constants.Tag.Title.toString(), currLine.substring(Constants.TITLE_TAG.length()).trim(), Field.Store.YES);
                        doc.add(titleField);
                        System.out.println("Adding Title: " + currLine.substring(Constants.TITLE_TAG.length()).trim());
                        currTag = Constants.Tag.Title;
                        break;
                    case Desc: 
                            currFieldEntry = "";
                            currTag = Constants.Tag.Desc;
                        break;
                    case Narr: 
                            currFieldEntry = "";
                            currTag = Constants.Tag.Narr;
                        break;
                    case Close: 
                        System.out.println("Adding Narr: " + currFieldEntry);
                        field = new TextField(currTag.toString(), currFieldEntry, Field.Store.YES);
                        documents.add(doc);
                        currFieldEntry = "";
                        currTag = Constants.Tag.Close;
                        break;
                    case None: 
                        currFieldEntry += currLine.trim();
                        break;
                    default:
                        break;
                }
            }
            System.out.println("Parsed " + documents.size() + " documents.");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static Constants.Tag getTag(String currLine) {
       if (currLine.indexOf(Constants.OPEN_TOP_TAG) == 0) return Constants.Tag.Open;
       if (currLine.indexOf(Constants.NUM_TAG) == 0) return Constants.Tag.Num;
       if (currLine.indexOf(Constants.TITLE_TAG) == 0) return Constants.Tag.Title;
       if (currLine.indexOf(Constants.DESC_TAG) == 0) return Constants.Tag.Desc;
       if (currLine.indexOf(Constants.NARR_TAG) == 0) return Constants.Tag.Narr;
       if (currLine.indexOf(Constants.CLOSE_TOP_TAG) == 0) return Constants.Tag.Close;
       return Constants.Tag.None;
    }
}
