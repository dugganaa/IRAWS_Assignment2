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

    public Index() {
        System.out.println("Initializing Indexer");
    }

    public void ParseTopics() {
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
                System.out.println("Entering while loop..");
                currLine = topicsReader.nextLine();
                Constants.Tag tagOnCurrLine = getTag(currLine);
                switch(tagOnCurrLine) {
                    case Open: 
                        if (currTag != Constants.Tag.None) documents.add(doc);
                        doc = new Document();
                        currTag = Constants.Tag.Open;
                        break;
                    case Close: 
                        field = new TextField(currTag.toString(), currFieldEntry, Field.Store.YES);
                        documents.add(doc);
                        currFieldEntry = "";
                        currTag = Constants.Tag.Open;
                    case Num: 
                        Field numField = new TextField(Constants.Tag.Num.toString(), currLine.split(" ")[2].trim(), Field.Store.YES);
                        doc.add(numField);
                        currTag = Constants.Tag.Num;
                        break;
                    case Title: 
                        Field titleField = new TextField(Constants.Tag.Title.toString(), currLine.substring(Constants.TITLE_TAG.length()).trim(), Field.Store.YES);
                        doc.add(titleField);
                        currTag = Constants.Tag.Title;
                        break;
                    default:
                        if (currTag == tagOnCurrLine) {
                            currFieldEntry += " " + currLine.trim();
                        }
                        else {
                            field = new TextField(currTag.toString(), currFieldEntry, Field.Store.YES);
                            doc.add(field);
                            currTag = tagOnCurrLine;
                            currFieldEntry = "";
                        }
                        break;
                }
            }
            System.out.println("Parsed " + documents.size() + " documents.");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private Constants.Tag getTag(String currLine) {
       if (currLine.indexOf(Constants.OPEN_TOP_TAG) == 0) return Constants.Tag.Open;
       if (currLine.indexOf(Constants.NUM_TAG) == 0) return Constants.Tag.Num;
       if (currLine.indexOf(Constants.TITLE_TAG) == 0) return Constants.Tag.Title;
       if (currLine.indexOf(Constants.DESC_TAG) == 0) return Constants.Tag.Desc;
       if (currLine.indexOf(Constants.NARR_TAG) == 0) return Constants.Tag.Narr;
       if (currLine.indexOf(Constants.CLOSE_TOP_TAG) == 0) return Constants.Tag.Close;
       return Constants.Tag.None;
    }
}
