package io.onedev.server.search.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import io.onedev.commons.jsymbol.java.JavaExtractor;

public class DiffIndexedSymbolsTest {

	@Test
	public void readsOnlyMatchingIndexEntriesOnRepeatedReads() throws Exception {
		var service = new DefaultCodeSearchService();
		var indexService = new CodeIndexService() {
			@Override public void indexAsync(Long projectId, ObjectId commitId) { throw new AssertionError("Must not index"); }
			@Override public boolean isIndexed(Long projectId, ObjectId commitId) { return true; }
			@Override public String getIndexVersion(io.onedev.commons.jsymbol.SymbolExtractor<io.onedev.commons.jsymbol.Symbol> extractor) { return "current"; }
		};
		var field = DefaultCodeSearchService.class.getDeclaredField("indexService");
		field.setAccessible(true);
		field.set(service, indexService);
		var symbols = new ArrayList<>(new JavaExtractor().extract("Example.java", "class Example { void run() {} }"));
		byte[] bytes = SerializationUtils.serialize(symbols);
		var blobId = ObjectId.fromString("1111111111111111111111111111111111111111");
		try (var directory = new ByteBuffersDirectory();
				var writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
			for (String path : new String[] {"Example.java", "stale.java"}) {
				var document = new Document();
				document.add(new StringField(FieldConstants.BLOB_HASH.name(), blobId.name(), Store.NO));
				document.add(new StringField(FieldConstants.BLOB_PATH.name(), path, Store.NO));
				document.add(new StoredField(FieldConstants.BLOB_INDEX_VERSION.name(), path.equals("stale.java") ? "old" : "current"));
				document.add(new StoredField(FieldConstants.BLOB_SYMBOL_LIST.name(), bytes));
				writer.addDocument(document);
			}
			try (var reader = DirectoryReader.open(writer)) {
				var searcher = new IndexSearcher(reader);
				assertEquals(symbols.size(), service.getSymbols(searcher, blobId, "Example.java").size());
				assertEquals(symbols.size(), service.getSymbols(searcher, blobId, "Example.java").size());
				assertNull(service.getSymbols(searcher, blobId, "stale.java"));
				assertNull(service.getSymbols(searcher, blobId, "missing.java"));
			}
		}
	}
}
