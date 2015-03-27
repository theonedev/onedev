package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NGramPhraseQuery;

import com.google.common.base.Preconditions;

public class NGramLuceneQuery extends NGramPhraseQuery {

	public NGramLuceneQuery(String fieldName, String fieldValue) {
		super(NGRAM_SIZE);
		
		Preconditions.checkArgument(fieldValue.length()>=NGRAM_SIZE);
		
		try (NGramTokenizer tokenizer = new NGramTokenizer(new StringReader(fieldValue.toLowerCase()), NGRAM_SIZE, NGRAM_SIZE)) {
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
