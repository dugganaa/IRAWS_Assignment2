package iraws_group;

import org.apache.lucene.analysis.standard.StandardTokenizer;

import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;


import java.util.Arrays;

import iraws_group.Constants;

/**
 * Creates a customized analyzer object.
 * The base tokenizer is selected with SELECTED_TOKENIZER.
 * Each extra filtering operation is set in the FILTER_PIPELINE.
 */
public class ModifiableTokenizer extends Analyzer{
	
	// Configure Tokenizer
	private static final Constants.Tokenizers SELECTED_TOKENIZER = Constants.Tokenizers.Letter;
	
	// Configure Filter Pipeline
	private static final Constants.Filters[] FILTER_PIPELINE = new Constants.Filters[] {Constants.Filters.Stem, Constants.Filters.Stop, Constants.Filters.Length};
		

	/**
	 * Overrides Analyzer.TokenStreamComponents.CreateComponents() to specify tokenization and filtering of the analyzer.
	 * @return A customized analyzer.
	 */
	
	@Override
	public Analyzer.TokenStreamComponents createComponents(String t)
	{
		System.out.println("Tokenizer: " + (SELECTED_TOKENIZER == null ? Constants.Tokenizers.Standard.toString() : SELECTED_TOKENIZER.toString()));

		//Sets the tokenizer. Default: StandardTokenizer.
		Tokenizer tokenizer = SELECTED_TOKENIZER == Constants.Tokenizers.Whitespace ? new WhitespaceTokenizer() :
						SELECTED_TOKENIZER == Constants.Tokenizers.Letter ? new LetterTokenizer() :
						SELECTED_TOKENIZER == Constants.Tokenizers.Standard ? new StandardTokenizer() :
						SELECTED_TOKENIZER == Constants.Tokenizers.Classic ? new ClassicTokenizer() :
						new StandardTokenizer();
					
		//Initializes the tokenstream with the tokenizer.
		TokenStream stream = tokenizer;
						
		//Applys each filter in FILTER_PIPELINE to the tokenstream
		for (Constants.Filters filter : FILTER_PIPELINE)
		{
			System.out.println("Filter: " + filter.toString());
			switch(filter)
			{
			case Classic:
				stream = new ClassicFilter(stream);
				break;
			case Case:
				stream = new LowerCaseFilter(stream);
				break;
			case Stop:
				CharArraySet stopWords = new CharArraySet(Constants.STOPS.length, true);
				stopWords.addAll(Arrays.asList(Constants.STOPS));
				stream = new StopFilter(stream, stopWords);
				break;
			case Length:
				stream = new LengthFilter(stream, 3, 17);
				break;
			case Stem:
				stream = new PorterStemFilter(stream);
			}
		}			
		return new Analyzer.TokenStreamComponents(tokenizer, stream);
	}
}
