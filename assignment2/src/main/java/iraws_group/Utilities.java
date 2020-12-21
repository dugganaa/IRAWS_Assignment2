package iraws_group;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Utilities{
		
	// Configure Ranking Function 
	private static final Constants.SimilarityClasses DEFAULT_SELECTED_SIMILARITY_CLASS = Constants.SimilarityClasses.Dirichlet;
	private static final float DEFAULT_LAMBDA = 0.1f;

	public static IndexSearcher GetSearcher(String indexPath) {
		return GetSearcher(indexPath, DEFAULT_SELECTED_SIMILARITY_CLASS, DEFAULT_LAMBDA);
	}
	
	public static IndexSearcher GetSearcher(String indexPath, Constants.SimilarityClasses similarityClass, float lambda)
	{
		try 
		{
			System.out.println("Ranking Function: " + similarityClass.toString());
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			switch(similarityClass)
			{
				case BM25:
					searcher.setSimilarity(new BM25Similarity());
                    break;
                case VSM:
                    searcher.setSimilarity(new ClassicSimilarity());
					break;
				case LMJelinekMercer:
					searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
					break;
                case Dirichlet:
                default:
                    searcher.setSimilarity(new LMDirichletSimilarity(lambda));	
			}			
			return searcher;
		}
		catch(Exception e)
		{
			System.out.println("Exception thrown while initializing index searcher: " + e.toString());
			return null;
		}
	}
	
	public static IndexWriter GetIndexWriter(Analyzer analyzer) {
		return GetIndexWriter(analyzer, DEFAULT_SELECTED_SIMILARITY_CLASS, DEFAULT_LAMBDA);
	}
	public static IndexWriter GetIndexWriter(Analyzer analyzer, Constants.SimilarityClasses similarityClass, float lambda)
	{
		try
		{
			Directory dir = FSDirectory.open(Paths.get(Constants.INDEX_LOC));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			//Creates a new index each time to test different analyzers/tokenizers/ranking functions
			iwc.setOpenMode(OpenMode.CREATE);
			System.out.println("Ranking Function: " + similarityClass.toString());
			switch(similarityClass)
			{
				case BM25:
					iwc.setSimilarity(new BM25Similarity());
					break;
                case VSM:
                    iwc.setSimilarity(new ClassicSimilarity());
					break;
				case LMJelinekMercer:
					iwc.setSimilarity(new LMJelinekMercerSimilarity(lambda));
					break;
                case Dirichlet:
                default:
					iwc.setSimilarity(new LMDirichletSimilarity(lambda));
			}			
	        IndexWriter writer = new IndexWriter(dir, iwc);
	        return writer;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
    }
    
     /*
     * Currently returns 0 hits :( 
     */
	public static BooleanQuery GetQuery(Analyzer analyzer, ParsedTopic topic)
	{
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        Query query1 = new TermQuery(new Term(Constants.DocTag.HEADLINE.toString(), topic.getTitle()));
        Query query2 = new TermQuery(new Term(Constants.DocTag.TEXT.toString(), topic.getDesc()));
        booleanQuery.add(query1, BooleanClause.Occur.SHOULD);
        booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
		return booleanQuery.build();
    }
    
    public static MultiFieldQueryParser GetQueryParser(Analyzer analyzer)
	{
		// MultiFieldQueryParser allows for a query to search all the fields of a document
		String[] fieldsToAnalyze = { Constants.DocTag.HEADLINE.toString(), Constants.DocTag.TEXT.toString() };
		MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsToAnalyze, analyzer);
		parser.setAllowLeadingWildcard(true);
		
		return parser;
	}

	public static HashMap<String, Constants.DocTag> GetLATTags() {
		HashMap<String, Constants.DocTag> tagMappings = new HashMap<String, Constants.DocTag>();
		tagMappings.put("DOCNO", Constants.DocTag.DOCNO);
		tagMappings.put("DATE", Constants.DocTag.DATE);
		tagMappings.put("HEADLINE", Constants.DocTag.HEADLINE);
		tagMappings.put("TEXT", Constants.DocTag.TEXT);
		return tagMappings;
	}

	public static HashMap<String, Constants.DocTag> GetFBISTags() {
		HashMap<String, Constants.DocTag> tagMappings = new HashMap<String, Constants.DocTag>();
		tagMappings.put("DOCNO", Constants.DocTag.DOCNO);
		tagMappings.put("DATE1", Constants.DocTag.DATE);
		tagMappings.put("TI", Constants.DocTag.HEADLINE);
		tagMappings.put("TEXT", Constants.DocTag.TEXT);
		return tagMappings;
	}

	public static HashMap<String, Constants.DocTag> GetFTLTags() {
		HashMap<String, Constants.DocTag> tagMappings = new HashMap<String, Constants.DocTag>();
		tagMappings.put("DOCNO", Constants.DocTag.DOCNO);
		tagMappings.put("DATE", Constants.DocTag.DATE);
		tagMappings.put("HEADLINE", Constants.DocTag.HEADLINE);
		tagMappings.put("TEXT", Constants.DocTag.TEXT);
		tagMappings.put("BYLINE", Constants.DocTag.AUTHOR);
		return tagMappings;
	}

	public static HashMap<String, Constants.DocTag> GetFRTags() {
		HashMap<String, Constants.DocTag> tagMappings = new HashMap<String, Constants.DocTag>();
		tagMappings.put("DOCNO", Constants.DocTag.DOCNO);
		tagMappings.put("DATE", Constants.DocTag.DATE);
		tagMappings.put("TEXT", Constants.DocTag.TEXT);
		tagMappings.put("SIGNER", Constants.DocTag.AUTHOR);
		return tagMappings;
	}

	public static HashSet<String> GetNarrStopWords() {
		HashSet<String> stopWords = new HashSet<String>();
		Collections.addAll(stopWords, "relevant", "document", "documents", "reference", "references", "include", "identify");
		return stopWords;
	}
}
