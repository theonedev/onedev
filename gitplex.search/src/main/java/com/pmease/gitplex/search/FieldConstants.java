package com.pmease.gitplex.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.TermQuery;

public enum FieldConstants {
	BLOB_INDEX_VERSION, BLOB_HASH, BLOB_PATH, BLOB_SYMBOLS, BLOB_CONTENT, 
	COMMIT_INDEX_VERSION, COMMIT_HASH, 
	META, LAST_COMMIT, LAST_COMMIT_HASH, LAST_COMMIT_INDEX_VERSION;
	
	public TermQuery query(String value) {
		return new TermQuery(term(value));
	}
	
	public Term term(String value) {
		return new Term(name(), value);
	}
	
	public NGramPhraseQuery ngramQuery(String value) {
		return null;
	}
}
