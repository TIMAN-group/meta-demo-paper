import java.io.Reader;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

/**
 * Analyzer performs the following functions:
 *  - make all tokens lowercase
 *  - remove stop words based on standard list
 *  - have max token length of 32 characters
 *  - run porter stemmer on each token
 */
public
class SpecialAnalyzer extends StopwordAnalyzerBase {
   public
    SpecialAnalyzer() { super(StopFilter.makeStopSet(Stopwords.STOPWORDS)); }

    @Override protected TokenStreamComponents createComponents(
        String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        filter = new LengthFilter(filter, 2, 32);
        filter = new StopFilter(filter, stopwords);
        filter = new PorterStemFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
