package iraws_group;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class Main
{
    public static void main( String[] args )
    {
        for (String arg : args)
        {
            switch(arg)
            {
                case Constants.INDEX_FLAG:
                    System.out.println("Indexing..");
                    Index indexer = new Index();
                    indexer.ParseTopics();
                    break;
                case Constants.SEARCH_FLAG:
                    System.out.println("Search");
                    break;
                default:
                    System.out.println("Not a valid flag");
            }
        }
        
    }
}
