package io.onedev.server.search.code;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

public enum FieldConstants {
	PROJECT_ID, BLOB_INDEX_VERSION, BLOB_HASH, BLOB_PATH, BLOB_NAME, BLOB_SYMBOL_LIST, 
	BLOB_PRIMARY_SYMBOLS, BLOB_SECONDARY_SYMBOLS, BLOB_TEXT, COMMIT_INDEX_VERSION, COMMIT_HASH, 
	META, BLOB, LAST_COMMIT, LAST_COMMIT_HASH, LAST_COMMIT_INDEX_VERSION;
	
	public TermQuery getTermQuery(String value) {
		return new TermQuery(getTerm(value));
	}
	
	public Term getTerm(String value) {
		return new Term(name(), value);
	}
	
}
