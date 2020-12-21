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
import java.util.HashSet;

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
        //A list of parsed topics found in topics.txt
        List<ParsedTopic> parsedTopics = ParseTopics();

        try
		{
            //Creates the custom analyzer which will be used by the IndexSearcher
            Analyzer analyzer = new ModifiableTokenizer();

            String outputFile;
            IndexSearcher searcher;

            //In general, there will only be one cl arg specifiying whether the code should index or search. 
            //Passing more arguments manually specifies the ranking function, weighting value and output file where appropriate.
            if (args.length > 1) {
                searcher = Utilities.GetSearcher(Constants.INDEX_LOC, Constants.SimilarityClasses.values()[Integer.parseInt(args[1])], Float.parseFloat(args[2]));
                outputFile = Constants.RESULTS_FOLDER_LOC + "//" + args[3];
            }else {
                searcher = Utilities.GetSearcher(Constants.INDEX_LOC);
                outputFile = Constants.RESULTS_LOC;
            }

            PrintWriter resultsWriter = new PrintWriter(new FileOutputStream(outputFile, false));
            //MFQP applies the query to the specified fields of each document
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] {Constants.DocTag.HEADLINE.toString(), Constants.DocTag.TEXT.toString()}, analyzer);
            
            //For each parsed topic, generate a list of max 1000 relevant documents.
            for(ParsedTopic topic : parsedTopics) {
                try {
                    //Generates a string query from the topic
                    String query = queryBuilder(topic);

                    //Searches index and creates a list of relevant documents
                    ScoreDoc[] hits = searcher.search(queryParser.parse(QueryParser.escape(query)), 1000).scoreDocs;
                    System.out.println("Found " + hits.length + " hits");
                    
                    //Outputs the results in trec_eval format
                    for (int i = 0; i < hits.length-1; i++){
                        Document hitDoc = searcher.doc(hits[i].doc);
                        resultsWriter.println(topic.getNum() + " 0 " + hitDoc.get("DOCNO") + " " + i + " " + hits[i].score + " 0");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
			resultsWriter.close();
			System.out.println("Finished.");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Function that parses topics from topics.txt
     * @return List of parsed topics
     */
    private static List<ParsedTopic> ParseTopics() {

        ArrayList<ParsedTopic> parsedTopics = new ArrayList<ParsedTopic>();
    
        try 
        {
            ParsedTopic parsedTopic = new ParsedTopic();
            File topicsFile = new File(Constants.REL_TOPICS_LOC);
            FileReader fileReader = new FileReader(topicsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null) 
            {
                parsedTopic = new ParsedTopic();
                while (!line.contains("<num>")) {
                    line = bufferedReader.readLine();
                    if(line == null){
                        break;
                    }
                }
                if(line == null){
                    break;
                }
                //Add topic number to parsedTopic
                if(line.contains("<num>"))
                {
                    String words2store = "";
                    words2store += line;
                    //Num is a numerical value, all non-numerical characters can be removed from the line
                    words2store = words2store.replaceAll("[^0-9]", "");
                    line = bufferedReader.readLine();
                    parsedTopic.setNum(words2store);
                }
                //Ignores whitespace between "num" and "topic"
                while (!line.contains("<title>")) {
                    line = bufferedReader.readLine();
                }
                //Adds titled to parsedTopic
                if(line.contains("<title>"))
                {
                    String words2store = "";
                    words2store += line;
                    words2store = words2store.replaceAll("<title> ", "");
                    line = bufferedReader.readLine();
                    parsedTopic.setTitle(words2store);
                }
                //Ignores whitespace between "title" and "desc"
                while (!line.contains("<desc>")) {
                    line = bufferedReader.readLine();
                }
                //Iterates through all the description of a topic and stores it in parsedTopic
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
                }
                //Ignores whitespace between "desc" and "narr"
                while (!line.contains("<narr>")) {
                    line = bufferedReader.readLine();
                }
                //Iterates through all the narrative of a topic and stores it in parsedTopic
                if(line.contains("<narr>"))
                {
                    String words2store = "";
                    line = bufferedReader.readLine();
                    while ((line != null) && (!line.contains("</top>")))
                    {
                        words2store += line;
                        words2store += " ";
                        line = bufferedReader.readLine();
                    }
                    parsedTopic.setNarr(words2store);
                }
                parsedTopics.add(parsedTopic);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return parsedTopics;
    }
    
    /**
     * @param topic A parsed topic from the topics file.
     * @return      A string query generated from the parsed topic
     */
    private static String queryBuilder(ParsedTopic topic){

        //Creates a basic query string from the query title, description and relative words in the narrative.
        String queryString = topic.getTitle() + " " + topic.getDesc() + " " + getNarrRel(topic.getNarr());

        return queryString;
    }

    /**
     * The narrative section of a document contains both "relevant" and "non-relevant" sections.
     * This function provides a basic approach to understanding what's "relevant".
     * @param narr  Narr section of a document
     * @return      String of relevant terms
     */
    private static String getNarrRel(String narr) {
        if (narr.isEmpty()) return "";

        //Sentences are split by a period.
        String[] sentences = narr.split("\\.");
        ArrayList<String> relevantSentences = new ArrayList<String>();
        //Whenever something is expressed as irrelevant in the Narr section, the sentence typically contains the expression "not relevant"
        for (String sentence : sentences) {
            if (!sentence.contains("not relevant")) {
                relevantSentences.add(sentence);
            }
        }

        //Removes stopwords specific to the narrative section
        relevantSentences = removeNarrStopwords(relevantSentences);
        return String.join(" ", relevantSentences);
    }

    /**
     * Some words frequently occur in the narrative section that can be remove.
     * eg. "relevant", "cites", "document"
     * @param sentences     The list of sentences in the narrative section
     * @return              The sentences passed as arg with stop words removed
     */
    private static ArrayList<String> removeNarrStopwords(ArrayList<String> sentences)  {
        Set<String> stopWords = Utilities.GetNarrStopWords();

        for (int i = 0; i < sentences.size(); i++) {
            String[] wordsInSentence = sentences.get(i).split(" ");
            String newSentence = "";
            for (String word : wordsInSentence) {
                if (!stopWords.contains(word.toLowerCase())){
                    newSentence += " " + word;
                }
            }
            sentences.set(i, newSentence);
        }
        return sentences;
    }
}