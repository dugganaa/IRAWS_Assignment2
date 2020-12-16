package iraws_group;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.io.PrintWriter;
import java.util.*;

import iraws_group.Constants;

import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

public class Search {
    public static void main(String[] args)
    {
        List<ParsedTopic> parsedTopics = ParseTopics();

        try
		{
			PrintWriter resultsWriter = new PrintWriter(new FileOutputStream(Constants.RESULTS_LOC, false));
            Analyzer analyzer = new ModifiableTokenizer();
            IndexSearcher searcher;
            if (args.length > 1) {
                searcher = Utilities.GetSearcher(Constants.INDEX_LOC, Constants.SimilarityClasses.values()[Integer.parseInt(args[1])], Float.parseFloat(args[2]));
            }
            else {
                searcher = Utilities.GetSearcher(Constants.INDEX_LOC);
            }
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] {Constants.DocTag.HEADLINE.toString(), Constants.DocTag.TEXT.toString()}, analyzer);
            

            int COUNT = 0;
            for(ParsedTopic topic : parsedTopics) {
                COUNT+=1;
                try {
                    // String title = topic.getTitle();
                    // System.out.println(title);

                    // // title = title.replaceAll(" ", "");
                    // String[] brokenTitle = title.split(",");
                    // String titleMustHave ="(";
                    // int count = 0;
                    // for(int m = 0; m<brokenTitle.length;m++)
                    // {
                    //     System.out.println(brokenTitle.length);
                    //     if(count == brokenTitle.length-1)
                    //     {
                    //         titleMustHave += brokenTitle[m];
                    //         titleMustHave += ")";
                    //         break;
                    //     }
                    //     titleMustHave += brokenTitle[m];
                    //     titleMustHave += " AND";
                    //     count+=1;
                    // }
                    // System.out.println(titleMustHave);
                    String query = queryBuilder(topic);
                    // query += titleMustHave;
                    ScoreDoc[] hits = searcher.search(queryParser.parse(QueryParser.escape(query)), 1000).scoreDocs;
                    System.out.println("Found " + hits.length + " hits");
                    
                    for (int i = 0; i < hits.length-1; i++)
                    {
                        Document hitDoc = searcher.doc(hits[i].doc);
                        resultsWriter.println(topic.getNum() + " 0 " + hitDoc.get("DOCNO") + " " + i + " " + hits[i].score + " 0");
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println(COUNT);
			resultsWriter.close();
			System.out.println("Finished.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
    // private static Constants.QueryTag getTag(String currLine) {
    //    if (currLine.indexOf(Constants.OPEN_TOP_TAG) == 0) return Constants.QueryTag.Open;
    //    if (currLine.indexOf(Constants.NUM_TAG) == 0) return Constants.QueryTag.Num;
    //    if (currLine.indexOf(Constants.TITLE_TAG) == 0) return Constants.QueryTag.Title;
    //    if (currLine.indexOf(Constants.DESC_TAG) == 0) return Constants.QueryTag.Desc;
    //    if (currLine.indexOf(Constants.NARR_TAG) == 0) return Constants.QueryTag.Narr;
    //    if (currLine.indexOf(Constants.CLOSE_TOP_TAG) == 0) return Constants.QueryTag.Close;
    //    return Constants.QueryTag.None;
    // }

    
    private static List<ParsedTopic> ParseTopics() {

        ArrayList<ParsedTopic> parsedTopics = new ArrayList<ParsedTopic>();
    
        try 
        {
            ParsedTopic parsedTopic = new ParsedTopic();
            File topicsFile = new File(Constants.REL_TOPICS_LOC);
            FileReader fileReader = new FileReader(topicsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int runCounter = 0;
            while (line != null) 
            {
                System.out.println(runCounter);
                System.out.println(line);
                parsedTopic = new ParsedTopic();
                // line = bufferedReader.readLine();
                while (!line.contains("<num>")) {
                    line = bufferedReader.readLine();
                    if(line == null){
                        break;
                    }
                }
                if(line == null){
                    break;
                }
                if(line.contains("<num>"))
                {
                    String words2store = "";
                    words2store += line;
                    words2store = words2store.replaceAll("[^0-9]", "");
                    line = bufferedReader.readLine();
                    parsedTopic.setNum(words2store);
                    // System.out.println(words2store);
                }
                while (!line.contains("<title>")) {
                    line = bufferedReader.readLine();
                }
                if(line.contains("<title>"))
                {
                    String words2store = "";
                    words2store += line;
                    words2store = words2store.replaceAll("<title> ", "");
                    line = bufferedReader.readLine();
                    parsedTopic.setTitle(words2store);
                    // System.out.println(words2store);
                }
                while (!line.contains("<desc>")) {
                    line = bufferedReader.readLine();
                }
                if(line.contains("<desc>"))
                {
                    String words2store = "";
                    line = bufferedReader.readLine();
                    while ((line != null) && (!line.contains("<narr>")))
                    {
                        words2store += line;
                        words2store += " ";
                        // words2store = words2store.replaceAll("<title> ", "");
                        line = bufferedReader.readLine();
                    }
                    parsedTopic.setDesc(words2store);
                    // System.out.println(words2store);
                }
                while (!line.contains("<narr>")) {
                    line = bufferedReader.readLine();
                }
                if(line.contains("<narr>"))
                {
                    String words2store = "";
                    line = bufferedReader.readLine();
                    while ((line != null) && (!line.contains("</top>")))
                    {
                        words2store += line;
                        words2store += " ";
                        // words2store = words2store.replaceAll("<title> ", "");
                        line = bufferedReader.readLine();
                    }
                    parsedTopic.setNarr(words2store);
                    System.out.println(words2store);
                }
                parsedTopics.add(parsedTopic);
                runCounter+=1;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return parsedTopics;
    }
    
    
    
    
    // private static List<ParsedTopic> ParseTopics() {

    //     ArrayList<ParsedTopic> parsedTopics = new ArrayList<ParsedTopic>();

    //     try {
    //         File topicsFile = new File(Constants.REL_TOPICS_LOC);
    //         Scanner topicsReader = new Scanner(topicsFile);
    //         String currLine = "";
    //         String currFieldEntry = "";
    //         ParsedTopic parsedTopic = new ParsedTopic();
    //         Constants.QueryTag currTag = Constants.QueryTag.None;

    //         while (topicsReader.hasNextLine()) {
    //             currLine = topicsReader.nextLine();

    //             if (currLine.isEmpty())
    //                 continue;

    //             Constants.QueryTag tagOnCurrLine = getTag(currLine);
    //             switch(tagOnCurrLine) {
    //                 case Open: 
    //                     if (currTag != Constants.QueryTag.None) parsedTopics.add(parsedTopic);
    //                     parsedTopic = new ParsedTopic();
    //                     currTag = Constants.QueryTag.Open;
    //                     break;

    //                 case Num: 
    //                     parsedTopic.setNum(currLine.split(" ")[2].trim());
    //                     System.out.println("Adding Num: " + currLine.split(" ")[2].trim() );
    //                     currTag = Constants.QueryTag.Num;
    //                     break;

    //                 case Title: 
    //                     parsedTopic.setTitle(currLine.substring(Constants.TITLE_TAG.length()).trim());
    //                     System.out.println("Adding Title: " + currLine.substring(Constants.TITLE_TAG.length()).trim());
    //                     currTag = Constants.QueryTag.Title;
    //                     break;

    //                 case Desc: 
    //                     currFieldEntry = "";
    //                     currTag = Constants.QueryTag.Desc;
    //                     break;

    //                 case Narr: 
    //                     System.out.println("Adding desc: " + currFieldEntry);
    //                     parsedTopic.setDesc(currFieldEntry);
    //                     currFieldEntry = "";
    //                     currTag = Constants.QueryTag.Narr;
    //                     break;

    //                 case Close: 
    //                     System.out.println("Adding Narr: " + currFieldEntry);
    //                     parsedTopic.setNarr(currFieldEntry);
    //                     currFieldEntry = "";
    //                     currTag = Constants.QueryTag.Close;
    //                     break;

    //                 case None: 
    //                     currFieldEntry += currLine.trim();
    //                     break;

    //                 default:
    //                     break;
    //             }
    //         }
    //         System.out.println("Parsed " + parsedTopics.size() + " topics.");
    //     }catch(Exception e)
    //     {
    //         e.printStackTrace();
    //     }
    //     return parsedTopics;
    // }



    private static String queryBuilder(ParsedTopic topic){
        String strOne = topic.getTitle();
        String strTwo = topic.getDesc();
        String strThree = topic.getNarr();

        String newStringOne = strOne.concat(" "); 
        newStringOne = newStringOne.concat(strTwo);

        String newStringTwo = newStringOne.concat(" ");
        newStringTwo = newStringTwo.concat(strThree);
        // System.out.println(" ");
        // System.out.println(" ");

        // // System.out.println(newStringTwo);

        // System.out.println(" ");
        // System.out.println(" ");


        String[] words = newStringTwo.split(" ");
        ArrayList<String> wordsList = new ArrayList<String>();
        Set<String> stopWordsSet = new HashSet<String>();
        for (int l = 0; l<Constants.STOPS.length;l++){
            stopWordsSet.add(Constants.STOPS[l]);
        }

        for(String word : words)
        {
            String wordCompare = word.toUpperCase();
            if(!stopWordsSet.contains(wordCompare))
            {
                wordsList.add(word);
            }
        }
        String finalString="";
        for (String str : wordsList){
            finalString = finalString.concat(" ");
            finalString = finalString.concat(str);
        }
        //test change
        //System.out.println(finalString);
        return finalString;
    }
}