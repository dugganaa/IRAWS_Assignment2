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
import java.util.HashMap;

public class Utilities{
		
	// Configure Ranking Function 
	private static final SimilarityClasses SELECTED_SIMILARITY_CLASS = SimilarityClasses.Dirichlet;
	
	public static enum SimilarityClasses {
		BM25,
        VSM,
        Dirichlet
	}
	
	public static IndexSearcher GetSearcher(String indexPath)
	{
		try 
		{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			System.out.println("Ranking Function: " + SELECTED_SIMILARITY_CLASS);
			switch(SELECTED_SIMILARITY_CLASS)
			{
				case BM25:
					searcher.setSimilarity(new BM25Similarity());
                    break;
                case VSM:
                    searcher.setSimilarity(new ClassicSimilarity());
                    break;
                case Dirichlet:
                default:
                    searcher.setSimilarity(new LMDirichletSimilarity());	
			}			
			return searcher;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static IndexWriter GetIndexWriter(Analyzer analyzer)
	{
		try
		{
			Directory dir = FSDirectory.open(Paths.get(Constants.INDEX_LOC));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			//Creates a new index each time to test different analyzers/tokenizers/ranking functions
			iwc.setOpenMode(OpenMode.CREATE);
			System.out.println("Ranking Function: " + SELECTED_SIMILARITY_CLASS.toString());
			switch(SELECTED_SIMILARITY_CLASS)
			{
				case BM25:
					iwc.setSimilarity(new BM25Similarity());
					break;
                case VSM:
                    iwc.setSimilarity(new ClassicSimilarity());
                    break;
                case Dirichlet:
                default:
					iwc.setSimilarity(new LMDirichletSimilarity());
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
}
