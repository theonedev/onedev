package com.pmease.gitplex.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

public enum FieldConstants {
	BLOB_ANALYZER_VERSION, BLOB_HASH, BLOB_PATH, BLOB_DEFS_SYMBOLS, BLOB_SYMBOLS, 
	COMMIT_ANALYZERS_VERSION, COMMIT_HASH, 
	META, LAST_COMMIT, LAST_COMMIT_HASH, LAST_COMMIT_ANALYZERS_VERSION;
	
	public TermQuery query(String value) {
		return new TermQuery(new Term(name(), value));
	}
}
