package io.onedev.server.codesearch;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

public class NGramAnalyzer extends Analyzer {
	
	private final int minGram;
	
	private final int maxGram;
	
	public NGramAnalyzer(int minGram, int maxGram) {
		this.minGram = minGram;
		this.maxGram = maxGram;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {

		Tokenizer src = new NGramTokenizer(reader, minGram, maxGram);

		TokenStream stream = new LowerCaseFilter(src);

		return new TokenStreamComponents(src, stream) {
			
			@Override
			protected void setReader(final Reader reader) throws IOException {
				super.setReader(reader);
			}
			
		};
	}

}
