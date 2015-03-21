package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NGramPhraseQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.gitplex.search.hit.QueryHit;

public abstract class BlobQuery extends NGramPhraseQuery {

	private final String fieldName;
	
	private final String searchFor;
	
	private final int count;

	private final boolean exactMatch;
	
	private final boolean caseSensitive;
	
	private final List<QueryHit> hits = new ArrayList<>();
	
	public BlobQuery(String fieldName, String searchFor, boolean exactMatch, 
			boolean caseSensitive, int count, int minGram, int maxGram) {
		super(minGram);
		
		try (NGramTokenizer tokenizer = new NGramTokenizer(
				new StringReader(searchFor.toLowerCase()), minGram, maxGram)) {
			tokenizer.reset();
			while (tokenizer.incrementToken()) {
				add(new Term(fieldName, tokenizer.getAttribute(CharTermAttribute.class).toString()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.fieldName = fieldName;
		this.searchFor = searchFor;
		this.count = count;
		this.caseSensitive = caseSensitive;
		this.exactMatch = exactMatch;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getSearchFor() {
		return searchFor;
	}

	public int getCount() {
		return count;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public List<QueryHit> getHits() {
		return hits;
	}
	
	public abstract void hit(TreeWalk treeWalk);
	
}
