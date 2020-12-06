package ie.seamus.searchEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.io.PrintWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.*;

// java -jar target/mySearch-1.0-SNAPSHOT.jar --similarity 2 --analyzer 2
// ../trec_eval-9.0.7/trec_eval cranfieldData/QRelsCorrectedforTRECeval searchOutput.txt 

public class App {
    static String INDEX_DIR = "index";
    static String QUERIES_DIR = "cranfieldData/cran.qry";
    static String CRANFIELD_PATH = "cranfieldData/cran.all.1400";

    /** 
     * Main function of the search engine
     * 
     * Description: For the simplicity of the assignment this indexes all of the documents, then runs all of the queries.
     * 
     * @param --similarity An int ranging from 0-3 specifying which similarity function to select.
     * (Defaults to BM25 since it was found to be the most effective)
     * 0: Boolean Similarity
     * 1: Classic Similarity
     * 2: BM25 Similarity
     * 3: LMDirichlet Similarity
     * 
     * @param --analyzer An int ranging from 0-2 specifying which analyzer function to select.
     * (Defaults to English analyzer since it was found to be the most effective)
     * 0: Standard Analyzer
     * 1: Whitespace Analyzer
     * 2: English Analyzer
    */
    public static void main(String[] args) throws Exception {

        // For loop to take in arguments from command line.
        int similarityChoice = 0;
        int analyzerChoice = 0;
        for (int i = 0; i < args.length; i++) {
            if ("--similarity".equals(args[i])) {
                similarityChoice = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("--analyzer".equals(args[i])) {
                analyzerChoice = Integer.parseInt(args[i + 1]);
                i++;
            }
        }
        final Path cranDir = Paths.get(CRANFIELD_PATH);
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));

        // Switch to assign analyzer selection.
        Analyzer analyzer = null;
        switch (analyzerChoice) {
            case 0:
                System.out.println("[*] Selected standard analyzer...");
                analyzer = new StandardAnalyzer();
                break;
            case 1:
                System.out.println("[*] Selected whitespace analyzer...");
                analyzer = new WhitespaceAnalyzer();
                break;
            case 2:
                System.out.println("[*] Selected english analyzer...");
                analyzer = new EnglishAnalyzer();
                break;
            default:
                System.out.println("[*] Selected standard analyzer...");
                analyzer = new EnglishAnalyzer();
                break;
        }

        // For simplicity we re-index the documents on every run (and time how long that takes).
        System.out.println("[*] Indexing to: " + INDEX_DIR + "...");
        IndexWriterConfig iwconfig = new IndexWriterConfig(analyzer);
        iwconfig.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwconfig);
        Date start = new Date();
        myDocumentIndexer(writer, cranDir);
        Date end = new Date();
        System.out.println("[*] Indexed in " + (end.getTime() - start.getTime()) + " milliseconds!");
        writer.close();

        // We run the queries and time how long that takes.
        System.out.println("[*] Running queries...");
        start = new Date();
        myDocumentSearcher(INDEX_DIR, similarityChoice, analyzer);
        end = new Date();
        System.out.println("[*] Queries ran in " + (end.getTime() - start.getTime()) + " milliseconds!");
    }

    // LEGEND FOR DOCUMENTS:
        // .I == Index
        // .T == Title
        // .A == Authors
        // .B == Bibliography
        // .W == Words
    /**
     * myDocumentIndexer
     * 
     * Description: Takes in an index writer and the path to the document and indexes the document at that path.
     * Note: This function assumes all the documents are stores in one file.
     * 
     * @param writer - the index writer (Set to create mode since we're writing a new index every time).
     * @param path - the path of our document, cran.all.1400.
     * @throws IOException
     */
    public static void myDocumentIndexer(IndexWriter writer, Path path) throws IOException {
        // We create a stream to the document so we can read it line by line...
        InputStream docStream = Files.newInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(docStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        Document doc;
        String type = "";
        String currentLine = bufferedReader.readLine();
        // Loops through the entire document and segmenting it by it's different sections.
        while (currentLine != null) {
            if (currentLine.startsWith(".I")) {
                doc = new Document();
                doc.add(new StringField("path", currentLine, Field.Store.YES));
                currentLine = bufferedReader.readLine();
                while ((currentLine != null) && !(currentLine.startsWith(".I"))) {
                    if (currentLine.startsWith(".T"))
                        type = "Title";
                    else if (currentLine.startsWith(".A"))
                        type = "Author";
                    else if (currentLine.startsWith(".B"))
                        type = "Bibliography";
                    else if (currentLine.startsWith(".W"))
                        type = "Words";
                    doc.add(new TextField(type, currentLine, Field.Store.YES));
                    currentLine = bufferedReader.readLine();
                }
                writer.addDocument(doc);
            }
        }
    }

    /**
     * myDocumentSearcher
     * 
     * Description: Parses the query file and runs each query against the indexed documents.
     * 
     * @param index - path of the indices.
     * @param similarityChoice - int to choose similarity function
     * @param analyzer - our analyzer function.
     * @throws Exception
     */
    public static void myDocumentSearcher(String index, int similarityChoice, Analyzer analyzer) throws Exception {
        // We make an indexSearcher using the indices produced from the myDocumentIndexer function.
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);

        // Switch to select similarity function
        switch (similarityChoice) {
            case 0:
                System.out.println("[*] Selected boolean similarity...");
                searcher.setSimilarity(new BooleanSimilarity());
                break;
            case 1:
                System.out.println("[*] Selected classic similarity...");
                searcher.setSimilarity(new ClassicSimilarity());
                break;
            case 2:
                System.out.println("[*] Selected BM25 similarity...");
                searcher.setSimilarity(new BM25Similarity());
                break;
            case 3:
                System.out.println("[*] Selected LMDirichlet similarity...");
                searcher.setSimilarity(new LMDirichletSimilarity());
                break;
            default:
                System.out.println("[*] Selected BM25 similarity...");
                searcher.setSimilarity(new BM25Similarity());
                break;

        }

        // We create a stream of the queries so we can read them line by line.
        BufferedReader queryStream = Files.newBufferedReader(Paths.get(QUERIES_DIR), StandardCharsets.UTF_8);
        String[] allTags = new String[] { "Title", "Author", "Bibliography", "Words" };
        MultiFieldQueryParser parser = new MultiFieldQueryParser(allTags, analyzer);

        String currentQuery = queryStream.readLine();
        String queryBuilder = "";
        int queryNumber = 1;

        // We'll save all of our searches to "searchOutput.txt"
        PrintWriter writer = new PrintWriter("searchOutput.txt", "UTF-8");

        // A loop to run every query against the documents.
        while (true) {
            // Small sanity check to make sure our current query is valid.
            if (currentQuery == null || currentQuery.length() == -1) {
                break;
            }
            if (currentQuery.startsWith(".I")) {
                currentQuery = queryStream.readLine();
                if (currentQuery.equals(".W")) {
                    currentQuery = queryStream.readLine();
                }
                // We make a query builder to build up the entire query since it's on multiple lines.
                queryBuilder = "";
                while (!currentQuery.startsWith(".I")) {
                    queryBuilder = queryBuilder + " " + currentQuery;
                    currentQuery = queryStream.readLine();
                    if (currentQuery == null)
                        break;
                }
            }
            Query query = parser.parse(QueryParser.escape(queryBuilder.trim()));

            // We run the query and grab the top 25 results (I decided that anything past this is irrelevant).
            TopDocs results = searcher.search(query, 25);
            ScoreDoc[] hits = results.scoreDocs;

            // Loops through the hits and writes them all to "searchOutput.txt" in a format trec_eval will accept.
            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);
                String docIndex = doc.get("path");
                if (docIndex != null) {
                    // int docNum = Integer.parseInt(path.replace(".I", "").replace(" ", "")) - 1;
                    writer.println(queryNumber + " 0 " + docIndex.replace(".I", "").replace(" ", "") + " " + (i + 1) + " "
                            + hits[i].score + " STANDARD");
                }
            }
            queryNumber++;
        }
        writer.close();
    }
}