package com.pmease.gitplex.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

public enum FieldConstants {
	BLOB_ANALYZER_VERSION, BLOB_HASH, BLOB_PATH, BLOB_SYMBOLS, BLOB_CONTENT, 
	COMMIT_ANALYZERS_VERSION, COMMIT_HASH, 
	META, LAST_COMMIT, LAST_COMMIT_HASH, LAST_COMMIT_ANALYZERS_VERSION;
	
	public TermQuery query(String value) {
		return new TermQuery(term(value));
	}
	
	public Term term(String value) {
		return new Term(name(), value);
	}
	
}
