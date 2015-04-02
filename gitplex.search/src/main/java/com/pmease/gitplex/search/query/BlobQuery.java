package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.FieldConstants.*;
import static com.pmease.gitplex.search.IndexConstants.NGRAM_SIZE;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
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
	
	private final Collection<String> pathSuffixes;
	
	private transient Pattern pattern;
	
	private transient String casedSearchFor;
	
	public BlobQuery(String fieldName, String searchFor, boolean regex, boolean wordMatch, boolean caseSensitive, 
			@Nullable String pathPrefix, @Nullable Collection<String> pathSuffixes, int count) {
		this.fieldName = fieldName;
		this.searchFor = searchFor;
		this.regex = regex;
		this.wordMatch = wordMatch;
		this.caseSensitive = caseSensitive;
		this.pathPrefix = pathPrefix;
		this.pathSuffixes = pathSuffixes;
		this.count = count;
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

	public Collection<String> getPathSuffixes() {
		return pathSuffixes;
	}

	public boolean isRegex() {
		return regex;
	}

	public abstract void collect(TreeWalk treeWalk, List<QueryHit> hits);

	public Query asLuceneQuery() {
		BooleanQuery query = new BooleanQuery(true);
		if (regex) {
			Query literalsQuery = new RegexLiterals(searchFor).asLuceneQuery(fieldName);
			if (literalsQuery != null)
				query.add(literalsQuery, Occur.MUST);
		} else if (searchFor.length() >= NGRAM_SIZE) { 
			query.add(new NGramLuceneQuery(fieldName, searchFor), Occur.MUST);
		}
		
		if (pathPrefix != null)
			query.add(new WildcardQuery(BLOB_PATH.term(pathPrefix + "*")), Occur.MUST);
		if (pathSuffixes != null && !pathSuffixes.isEmpty()) {
			BooleanQuery suffixQuery = new BooleanQuery(true);
			for (String suffix: pathSuffixes)
				suffixQuery.add(new WildcardQuery(BLOB_PATH.term("*" + suffix)), Occur.SHOULD);
			query.add(suffixQuery, Occur.MUST);
		}
		
		if (query.getClauses().length != 0)
			return query;
		else
			return new WildcardQuery(BLOB_PATH.term("*"));
	}

	protected Pattern getPattern() {
		if (regex) {
			if (pattern == null) {
				String expression = getSearchFor();
				if (isWordMatch()) {
					if (!expression.startsWith("\\b"))
						expression = "\\b" + expression;
					if (!expression.endsWith("\\b"))
						expression = expression + "\\b";
				}
				if (isCaseSensitive())
					pattern = Pattern.compile(expression);
				else
					pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			}
			return pattern;
		} else {
			return null;
		}
	}
	
	protected String getCasedSearchFor() {
		if (casedSearchFor == null) {
			if (caseSensitive)
				casedSearchFor = searchFor;
			else
				casedSearchFor = searchFor.toLowerCase();
		}
		return casedSearchFor;
	}
	
	protected String getCasedText(String text) {
		if (caseSensitive)
			return text;
		else
			return text.toLowerCase();
	}
	
}
