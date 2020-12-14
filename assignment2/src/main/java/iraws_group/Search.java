package iraws_group;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.wordnet.SynExpand;
import org.apache.lucene.wordnet.Syns2Index;


import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.io.PrintWriter;
import java.util.*;

import iraws_group.Constants;

public class Search {

    public static void main(String[] args)
    {
        Syns2Index.main(Constants.WORDNET_LOC, Constants.INDEX_LOC);
        List<ParsedTopic> parsedTopics = ParseTopics();

        try
		{
			PrintWriter resultsWriter = new PrintWriter(new FileOutputStream(Constants.RESULTS_LOC, false));
			Analyzer analyzer = new ModifiableTokenizer();
		    IndexSearcher searcher = Utilities.GetSearcher(Constants.INDEX_LOC);
            QueryParser queryParser = Utilities.GetQueryParser(analyzer);
            
            for(ParsedTopic topic : parsedTopics) {
                try {
                    //Use more fields from topic
                    //BooleanQuery query = GetQuery(analyzer, topic);
                    //ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
                    
                    //Currently only uses description of topic

                    //NOTE FOR ADAM TO REMEBER
                    //GENERATE THE QUERYIES FORM THE PARSED TOPIC OBJECTS
                    //SAVE THE QUERY FOR THE RESEARCH PAPER SO YOU CAN COMMENT ON THEM 



                    ScoreDoc[] hits = searcher.search(queryParser.parse(SynExpand.expand(topic.getDesc())), 1000).scoreDocs;
                    System.out.println("Found " + hits.length + " hits");
                    
                    for (int i = 0; i < hits.length-1; i++)
                    {
                        Document hitDoc = searcher.doc(hits[i].doc);
                        //System.out.println("Got score of: " + hits[i].score);
                        
                        //QueryID null null DocumentID Rank Score null
                        resultsWriter.println(topic.getNum() + " 0 " + hitDoc.get("ID") + " " + i + " " + hits[i].score + " 0");
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
			resultsWriter.close();
			System.out.println("Finished.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
    private static Constants.QueryTag getTag(String currLine) {
       if (currLine.indexOf(Constants.OPEN_TOP_TAG) == 0) return Constants.QueryTag.Open;
       if (currLine.indexOf(Constants.NUM_TAG) == 0) return Constants.QueryTag.Num;
       if (currLine.indexOf(Constants.TITLE_TAG) == 0) return Constants.QueryTag.Title;
       if (currLine.indexOf(Constants.DESC_TAG) == 0) return Constants.QueryTag.Desc;
       if (currLine.indexOf(Constants.NARR_TAG) == 0) return Constants.QueryTag.Narr;
       if (currLine.indexOf(Constants.CLOSE_TOP_TAG) == 0) return Constants.QueryTag.Close;
       return Constants.QueryTag.None;
    }

    private static List<ParsedTopic> ParseTopics() {

        ArrayList<ParsedTopic> parsedTopics = new ArrayList<ParsedTopic>();

        try {
            File topicsFile = new File(Constants.REL_TOPICS_LOC);
            Scanner topicsReader = new Scanner(topicsFile);

            String currLine = "";
            String currFieldEntry = "";
            ParsedTopic parsedTopic = new ParsedTopic();
            Constants.QueryTag currTag = Constants.QueryTag.None;

            while (topicsReader.hasNextLine()) {
                currLine = topicsReader.nextLine();

                if (currLine.isEmpty())
                    continue;

                Constants.QueryTag tagOnCurrLine = getTag(currLine);
                switch(tagOnCurrLine) {
                    case Open: 
                        if (currTag != Constants.QueryTag.None) parsedTopics.add(parsedTopic);
                        parsedTopic = new ParsedTopic();
                        currTag = Constants.QueryTag.Open;
                        break;

                    case Num: 
                        parsedTopic.setNum(currLine.split(" ")[2].trim());
                        System.out.println("Adding Num: " + currLine.split(" ")[2].trim() );
                        currTag = Constants.QueryTag.Num;
                        break;

                    case Title: 
                        parsedTopic.setTitle(currLine.substring(Constants.TITLE_TAG.length()).trim());
                        System.out.println("Adding Title: " + currLine.substring(Constants.TITLE_TAG.length()).trim());
                        currTag = Constants.QueryTag.Title;
                        break;

                    case Desc: 
                        currFieldEntry = "";
                        currTag = Constants.QueryTag.Desc;
                        break;

                    case Narr: 
                        System.out.println("Adding desc: " + currFieldEntry);
                        parsedTopic.setDesc(currFieldEntry);
                        currFieldEntry = "";
                        currTag = Constants.QueryTag.Narr;
                        break;

                    case Close: 
                        System.out.println("Adding Narr: " + currFieldEntry);
                        parsedTopic.setNarr(currFieldEntry);
                        currFieldEntry = "";
                        currTag = Constants.QueryTag.Close;
                        break;

                    case None: 
                        currFieldEntry += currLine.trim();
                        break;

                    default:
                        break;
                }
            }
            System.out.println("Parsed " + parsedTopics.size() + " topics.");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return parsedTopics;
    }
}