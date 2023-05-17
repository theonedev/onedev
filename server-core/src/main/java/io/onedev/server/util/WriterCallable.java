package io.onedev.server.util;

import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

public interface WriterCallable<T> {

	T call(IndexWriter writer) throws IOException;

}
