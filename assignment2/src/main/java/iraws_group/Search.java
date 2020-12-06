package iraws_group;


import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;



public class Search {
    public Search()
    {

    }

    Analyzer analyzer = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer tokenizer = new StandardTokenizer();
            TokenStream stream = new ClassicFilter(tokenizer);
            //below you must always lowercase filter before anything else to avoid skipping words because of a case mismatch
            //we can add custome lists of stop words below also instead of StopAnalyzer.ENGLISH_STOP_WORDS_SET
            stream = new LowerCaseFilter(stream);
            stream = new StopFilter(stream, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            stream = new PorterStemFilter(stream);
            return new TokenStreamComponents(tokenizer, stream);
        }
    };

    // Analyzer Analyzer = CustomAnalyzer.builder()
    // .withTokenizer("standard")
    // .addTokenFilter("lowercase")
    // .addTokenFilter("stop")
    // .addTokenFilter("porterstem")
    // .addTokenFilter("capitalization")
    // .build();


}
