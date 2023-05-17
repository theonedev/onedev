package io.onedev.server.util;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

public interface SearcherCallable<T> {

	T call(IndexSearcher searcher) throws IOException;

}
