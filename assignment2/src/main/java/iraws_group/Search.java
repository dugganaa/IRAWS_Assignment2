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

public class Search {
    public static void main(String[] args)
    {
        List<ParsedTopic> parsedTopics = ParseTopics();

        try
		{
			PrintWriter resultsWriter = new PrintWriter(new FileOutputStream(Constants.RESULTS_LOC, false));
			Analyzer analyzer = new ModifiableTokenizer();
		    IndexSearcher searcher = Utilities.GetSearcher(Constants.INDEX_LOC);
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] {Constants.DocTag.HEADLINE.toString(), Constants.DocTag.TEXT.toString()}, analyzer);
            
            for(ParsedTopic topic : parsedTopics) {
                try {
                    ScoreDoc[] hits = searcher.search(queryParser.parse(QueryParser.escape(queryBuilder(topic))), 1000).scoreDocs;
                    System.out.println("Found " + hits.length + " hits");
                    
                    for (int i = 0; i < hits.length-1; i++)
                    {
                        Document hitDoc = searcher.doc(hits[i].doc);
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

    private static String queryBuilder(ParsedTopic topic){
        String strOne = topic.getTitle();
        String strTwo = topic.getDesc();
        String strThree = topic.getNarr();

        String newStringOne = strOne.concat(" "); 
        newStringOne = newStringOne.concat(strTwo);

        String newStringTwo = newStringOne.concat(" ");
        newStringTwo = newStringTwo.concat(strThree);
        System.out.println(" ");
        System.out.println(" ");

        System.out.println(newStringTwo);

        System.out.println(" ");
        System.out.println(" ");


        String[] words = newStringTwo.split(" ");
        ArrayList<String> wordsList = new ArrayList<String>();
        Set<String> stopWordsSet = new HashSet<String>();
        String[] stops = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
        for (int l = 0; l<stops.length;l++){
            stopWordsSet.add(stops[l]);
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
        System.out.println(finalString);
        return finalString;
    }
}