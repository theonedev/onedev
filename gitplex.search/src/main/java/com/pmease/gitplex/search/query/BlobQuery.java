package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.regex.RegexLiterals;

public abstract class BlobQuery {

	private final String fieldName;
	
	private final String searchFor;
	
	private final int count;

	private final boolean wordMatch;
	
	private final boolean regex;
	
	private final boolean caseSensitive;
	
	private final String pathPrefix;
	
	private final String pathSuffix;
	
	public BlobQuery(String fieldName, String searchFor, @Nullable String pathPrefix, 
			@Nullable String pathSuffix, boolean wordMatch, boolean caseSensitive, 
			boolean regex, int count) {
		this.fieldName = fieldName;
		this.searchFor = searchFor;
		this.pathPrefix = pathPrefix;
		this.pathSuffix = pathSuffix;
		this.count = count;
		this.caseSensitive = caseSensitive;
		this.wordMatch = wordMatch;
		this.regex = regex;
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

	public Query asLuceneQuery() {
		BooleanQuery query = new BooleanQuery(true);
		if (regex) 
			query.add(new RegexLiterals(searchFor).asLuceneQuery(fieldName), Occur.MUST);
		else if (searchFor.length() >= NGRAM_SIZE) 
			query.add(new NGramLuceneQuery(fieldName, searchFor), Occur.MUST);

		if (pathPrefix != null && pathPrefix.length() >= NGRAM_SIZE)
			query.add(new NGramLuceneQuery(fieldName, pathPrefix), Occur.MUST);

		if (pathSuffix != null && pathSuffix.length() >= NGRAM_SIZE)
			query.add(new NGramLuceneQuery(fieldName, pathSuffix), Occur.MUST);
		
		return query;
	}
}
