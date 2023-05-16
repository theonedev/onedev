package io.onedev.server.util;

import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

public interface WriterRunnable {

	void run(IndexWriter writer) throws IOException;

}
