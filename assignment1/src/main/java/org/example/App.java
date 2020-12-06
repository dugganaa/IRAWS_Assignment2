/**
 * Thank you to Dr. Gary Munnelly for the orginal example scripts
 * and thank you to Colin Daly who adapted them for 2020
 * some of the code from the example scripts from class have been repurposed for use in my project below
 */


package org.example;

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

public class App {
    public static String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException, ParseException {




//Set up your analyzer from the list below, just comment out the ones you arent using.
        //https://www.baeldung.com/lucene-analyzers
        Analyzer analyzer = CustomAnalyzer.builder().withTokenizer("standard").addTokenFilter("lowercase").addTokenFilter("stop").addTokenFilter("porterstem").build();
//        Analyzer analyzer = new StandardAnalyzer();
//        Analyzer analyzer = new WhitespaceAnalyzer();
//        Analyzer analyzer = new EnglishAnalyzer();
        //make a call to the indexer function passing in our analyzer
        docIndexer(analyzer);
        querySearch(analyzer);

    }

    public static void docIndexer(Analyzer analyzer) throws IOException {
        //set up directory and index writer in open mode to index files
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        //String indicating location of the cranfiled data to index
        String CRANFIELD_DATA = "../corpus/cran.all.1400";

        //open a file reader and buffer reader to read in the data
        FileReader fileReader = new FileReader(CRANFIELD_DATA);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        Document doc;

        // just creates an int for doc id as it was easier than trying to separate the number form the .I part of the string and they are all sequential anyway
        int id = 0;

        /**
         * The below goes line by line as long as there is lines and looks for each separator (.T, .A, etc)
         * the .I is ignored and only used to indicate the start of a new doc because it is added to each doc as just an integer value that increments each loop
         * as it was easier than trying to separate the number form the .I part of the string and the docs are all sequential anyway
         * This code looks at where the first separator after the .I, .T is and then creates and empty string for that data
         * it then goes line by line and adds each line to that string to form the whole data for that section until it hits the next separator .A
         * at that point it adds that data (in this first case title) to the doc as a field and then repeats the process for each filed until it hits .I indicating a new doc
         * when it hits a new doc it adds the current doc to the index and starts with the new doc. it does this until all docs are done
         */
        while (line != null) {
            id++;
            doc = new Document();
            doc.add(new StringField("id", id + "", Field.Store.YES));
            line = bufferedReader.readLine();
            while (!line.contains(".T")) {
                line = bufferedReader.readLine();
            }
            if (line.contains(".T")) {
                String title = "";
                while (!line.contains(".A")) {
                    line = bufferedReader.readLine();
                    title += line;
                    title += " ";
                }
                doc.add(new TextField("title", title, Field.Store.YES));
            }
            if (line.contains(".A")) {
                String author = "";
                while (!line.contains(".B")) {
                    line = bufferedReader.readLine();
                    author += line;
                    author += " ";
                }
                doc.add(new TextField("author", author, Field.Store.YES));
            }
            if (line.contains(".B")) {
                String bibliography = "";
                while (!line.contains(".W")) {
                    line = bufferedReader.readLine();
                    bibliography += line;
                    bibliography += " ";
                }
                doc.add(new TextField("bibliography", bibliography, Field.Store.YES));
            }
            if (line.contains(".W")) {
                String words = "";
                while (!line.startsWith(".I")) {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    words += line;
                    words += " ";
                }
                doc.add(new TextField("words", words, Field.Store.YES));
            }
            iwriter.addDocument(doc);
        }
        iwriter.close();
        directory.close();
    }

    public static void querySearch(Analyzer analyzer) throws IOException, ParseException {
        //set up directory and index reader to read indexed files and seracher to serach them
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);


//Set the similarity that you wish to use when searching docs
//        isearcher.setSimilarity(new ClassicSimilarity());
        isearcher.setSimilarity(new BM25Similarity());
//        isearcher.setSimilarity(new BooleanSimilarity());
//        isearcher.setSimilarity(new LMDirichletSimilarity());


        /**
         * The below works similar to before. I created a multi field query parsers so that lucene can search the title, author, bibliography and words for relevance
         * I then used the file reader to read in the queries from cran.qry like in the indexer for the docs
         * I created an array list that will store all the queries for later use.
         * The blow reader goes through the queries and for each .W it adds lines to an empty string called words2store until it hits the next .I
         * it then adds this string to the list and goes again for the next .W
         *
         */

        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"title", "author", "bibliography", "words"}, analyzer);
        ArrayList<String> queryList = new ArrayList<String>();
        String QFILE = "../corpus/cran.qry";
        FileReader fileReader = new FileReader(QFILE);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            line = bufferedReader.readLine();
            while (!line.contains(".W")) {
                line = bufferedReader.readLine();
            }
            if (line.contains(".W")) {
                String words2store = "";
                line = bufferedReader.readLine();
                while ((line != null) && (!line.contains(".I"))) {
                    if(line.startsWith(".I")){
                        break;
                    }
                    else if(line == null){
                        break;
                    }
                    else {
                        words2store += line;
                        words2store += " ";
                        line = bufferedReader.readLine();
                    }
                }
                queryList.add(words2store);
            }
        }

        /**
         * Below I have created a file called cranfield_results.txt where I output the results for trekeval to test
         * it routes all the print statements from here on out to that file using print sream.
         */

        File outfile = new File("../cranfield_results.txt");
        PrintStream stream = new PrintStream(outfile);
        System.setOut(stream);

        /**
         * the below code goes though each query, parses it with the query parser, gets the top 1000 docs from the searchers as trek eval tests all the way up to first 1000 precision.
         * it then goes through each document revived and outputs it in the format specified for trek eval using the index of the query plus one as the outputted query id
         * the string formatted version of the documents id stored as a field in the indexed doc from earlier and its score from the hits lists of all retrieved docs for that query.
         */

        Query query;
        for(int i=0;i<(queryList.size());i++) {
            query = parser.parse(QueryParser.escape(queryList.get(i)));
            TopDocs results = isearcher.search(query, 1000);
            ScoreDoc[] hits = results.scoreDocs;
            for (int j = 0; j < hits.length; j++) {
                Document document = isearcher.doc(hits[j].doc);
                System.out.println(queryList.indexOf(queryList.get(i))+1 + " 0 " + document.getField("id").stringValue() + " 0 " + hits[j].score + " STANDARD");
            }
        }
        ireader.close();
        directory.close();
    }
}
