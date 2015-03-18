package com.pmease.gitplex.search;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

public class TriGramAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer src = new NGramTokenizer(reader, 3, 3);

        TokenStream tok = new LowerCaseFilter(src);

        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            } 
        };
        
    }

}
