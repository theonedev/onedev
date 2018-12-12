package io.onedev.server.search.code.query;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.PhraseQuery;

import com.google.common.base.Preconditions;

public class NGramLuceneQuery extends NGramPhraseQuery {

	public NGramLuceneQuery(String fieldName, String fieldValue, int gramSize) {
		super(gramSize, build(fieldName, fieldValue, gramSize));
	}
	
	private static PhraseQuery build(String fieldName, String fieldValue, int gramSize) {
		Preconditions.checkArgument(fieldValue.length()>=gramSize);
		PhraseQuery.Builder builder = new PhraseQuery.Builder();
		try (NGramTokenizer tokenizer = new NGramTokenizer(gramSize, gramSize)) {
			tokenizer.setReader(new StringReader(fieldValue.toLowerCase()));
			tokenizer.reset();
			while (tokenizer.incrementToken()) { 
				builder.add(new Term(fieldName, 
						tokenizer.getAttribute(CharTermAttribute.class).toString()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return builder.build();
	}

}
