package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.Query;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.gitplex.search.hit.QueryHit;

public abstract class BlobQuery {

	private final String fieldName;
	
	private final String searchFor;
	
	private final int count;

	private final boolean wordMatch;
	
	private final boolean regex;
	
	private final boolean caseSensitive;
	
	private final String pathPrefix;
	
	private final String pathSuffix;
	
	private final int gramSize;
	
	public BlobQuery(String fieldName, String searchFor, String pathPrefix, 
			String pathSuffix, boolean wordMatch, boolean caseSensitive, 
			boolean regex, int count, int gramSize) {
		this.fieldName = fieldName;
		this.searchFor = searchFor;
		this.pathPrefix = pathPrefix;
		this.pathSuffix = pathSuffix;
		this.count = count;
		this.caseSensitive = caseSensitive;
		this.wordMatch = wordMatch;
		this.regex = regex;
		this.gramSize = gramSize;
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

	public boolean isWordMatch() {
		return wordMatch;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public String getPathSuffix() {
		return pathSuffix;
	}

	public boolean isRegex() {
		return regex;
	}

	public abstract void collect(TreeWalk treeWalk, List<QueryHit> hits);

	@Nullable
	private NGramPhraseQuery asNGramQuery(String text) {
		if (text != null && text.length() >= gramSize) {
			NGramPhraseQuery query = new NGramPhraseQuery(gramSize);
			try (NGramTokenizer tokenizer = 
					new NGramTokenizer(new StringReader(searchFor.toLowerCase()), gramSize, gramSize)) {
				tokenizer.reset();
				while (tokenizer.incrementToken()) { 
					query.add(new Term(fieldName, 
							tokenizer.getAttribute(CharTermAttribute.class).toString()));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return query;
		} else {
			return null;
		}
	}
	
	public Query asLuceneQuery() {
		BooleanQuery query = new BooleanQuery(true);
		/*
		if (regex) {
			for (String literal: getLiterals(searchFor)) {
				NGramPhraseQuery subQuery = asNGramQuery(literal);
				if (subQuery != null)
					query.add(subQuery, Occur.MUST);
			}
		} else {
			NGramPhraseQuery subQuery = asNGramQuery(searchFor);
			if (subQuery != null)
				query.add(subQuery, Occur.MUST);
		}
		*/
		NGramPhraseQuery subQuery = asNGramQuery(pathPrefix);
		if (subQuery != null)
			query.add(subQuery, Occur.MUST);

		subQuery = asNGramQuery(pathSuffix);
		if (subQuery != null)
			query.add(subQuery, Occur.MUST);
		
		return query;
	}
}
