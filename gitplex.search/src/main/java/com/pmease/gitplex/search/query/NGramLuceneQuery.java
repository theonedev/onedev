package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NGramPhraseQuery;

import com.google.common.base.Preconditions;

public class NGramLuceneQuery extends NGramPhraseQuery {

	public NGramLuceneQuery(String fieldName, String fieldValue, int gramSize) {
		super(gramSize);
		
		Preconditions.checkArgument(fieldValue.length()>=gramSize);
		
		try (NGramTokenizer tokenizer = new NGramTokenizer(new StringReader(fieldValue.toLowerCase()), gramSize, gramSize)) {
			tokenizer.reset();
			while (tokenizer.incrementToken()) { 
				add(new Term(fieldName, 
						tokenizer.getAttribute(CharTermAttribute.class).toString()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
