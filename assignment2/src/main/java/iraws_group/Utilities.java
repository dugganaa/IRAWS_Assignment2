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

/**
 * A class for shared functions and object initializers with shared values
 */

public class Utilities{
		
	// Configure Ranking Function 
	private static final Constants.SimilarityClasses DEFAULT_SELECTED_SIMILARITY_CLASS = Constants.SimilarityClasses.Dirichlet;
	
	//JM cannot be initialized without a lambda value. Initalized with DEFAULT_LAMBDA if none specified as a cl arg.
	private static final float DEFAULT_LAMBDA = 0.1f;

	//Default mu value for DIR if none specified as a cl arg.
	private static final float DEFAULT_MU = 700f;

	/**
	 * Initializes an IndexSearcher object pointing at the created index with preset ranking function and lambda value.
	 * @param indexPath	String path to the index.
	 * @return			IndexSearcher object. 
	 */
	public static IndexSearcher GetSearcher(String indexPath) {
		return GetSearcher(indexPath, DEFAULT_SELECTED_SIMILARITY_CLASS, GetDefaultWeightingValue());
	}
	
	/**
	 * Initializes an IndexSearcher object pointing at the created index and user specified ranking function and weighting value.
	 * @return						IndexSearcher object.
	 */
	public static IndexSearcher GetSearcher(String indexPath, Constants.SimilarityClasses similarityClass, float smoothingWeighting)
	{
		try 
		{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			System.out.println("Ranking Function: " + similarityClass.toString());
			System.out.println("Weighting value: " + smoothingWeighting);

			//Sets the ranking function for the Index Searcher - default is Dirichlet.
			switch(similarityClass)
			{
				case BM25:
					searcher.setSimilarity(new BM25Similarity());
                    break;
                case VSM:
                    searcher.setSimilarity(new ClassicSimilarity());
					break;
				case LMJelinekMercer:
					searcher.setSimilarity(new LMJelinekMercerSimilarity(smoothingWeighting));
					break;
                case Dirichlet:
				default:
					if (smoothingWeighting == DEFAULT_LAMBDA) searcher.setSimilarity(new LMDirichletSimilarity());		
                    else 									  searcher.setSimilarity(new LMDirichletSimilarity(smoothingWeighting));	
			}			
			return searcher;
		}
		catch(Exception e)
		{
			System.out.println("Exception thrown while initializing index searcher: " + e.toString());
			return null;
		}
	}
	
	/**
	 * Initializes an IndexWriter object with preset ranking function and lambda value.
	 * @param analyzer	The analyzer which that the documents will be passed through before being written to the index.
	 * @return			An IndexWriter object.
	 */
	public static IndexWriter GetIndexWriter(Analyzer analyzer) {
		return GetIndexWriter(analyzer, DEFAULT_SELECTED_SIMILARITY_CLASS, GetDefaultWeightingValue());
	}

	/**
	 * Initializes an IndexWriter object with user specificed ranking function and weighting value.
	 * @param analyzer				The analyzer which that the documents will be passed through before being written to the index.
	 * @param similarityClass		Ranking function specified via cl arg.
	 * @param smoothingWeighting	Weighting value specified via cl arg.
	 * @return						An IndexWriter object.
	 */
	public static IndexWriter GetIndexWriter(Analyzer analyzer, Constants.SimilarityClasses similarityClass, float smoothingWeighting)
	{
		try
		{
			Directory dir = FSDirectory.open(Paths.get(Constants.INDEX_LOC));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			//Creates a new index each time to test different analyzers/tokenizers/ranking functions
			iwc.setOpenMode(OpenMode.CREATE);
			System.out.println("Ranking Function: " + similarityClass.toString());
			System.out.println("Weighting value: " + smoothingWeighting);
			//Sets the ranking function for the Index Writer - default is Dirichlet.
			switch(similarityClass)
			{
				case BM25:
					iwc.setSimilarity(new BM25Similarity());
					break;
                case VSM:
                    iwc.setSimilarity(new ClassicSimilarity());
					break;
				case LMJelinekMercer:
					iwc.setSimilarity(new LMJelinekMercerSimilarity(smoothingWeighting));
					break;
                case Dirichlet:
                default:
					if (smoothingWeighting == DEFAULT_LAMBDA) iwc.setSimilarity(new LMDirichletSimilarity());		
					else 									  iwc.setSimilarity(new LMDirichletSimilarity(smoothingWeighting));
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
	
	private static float GetDefaultWeightingValue() {
		if (DEFAULT_SELECTED_SIMILARITY_CLASS == Constants.SimilarityClasses.Dirichlet) return DEFAULT_MU;
		if (DEFAULT_SELECTED_SIMILARITY_CLASS == Constants.SimilarityClasses.LMJelinekMercer) return DEFAULT_LAMBDA;
		return 0f;
	}
	
	/**
	 * Below are the tag mappings for each corpus, as tags in each corpus do not always follow the same convention 
	 * Eg. DATE in LAT corpus, DATE1 in FBIS corpus.
	 */
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

	/**
	 * @return HashSet of narrative specific stop words.
	 */
	public static HashSet<String> GetNarrStopWords() {
		HashSet<String> stopWords = new HashSet<String>();
		Collections.addAll(stopWords, "relevant", "document", "documents", "reference", "references", "include", "identify");
		return stopWords;
	}
}
