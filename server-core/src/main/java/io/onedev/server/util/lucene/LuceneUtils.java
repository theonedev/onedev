package io.onedev.server.util.lucene;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.WriterCallable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bouncycastle.util.Arrays;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class LuceneUtils {

	private static final char[] SPECIAL_CHARS = new char[] {'+', '-', '&', '|', '!', '(', ')', 
			'{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/'};
	
	@Nullable
	public static String escape(String queryString) {
		StringBuilder builder = new StringBuilder();
		for (char ch: queryString.toCharArray()) {
			if (Arrays.contains(SPECIAL_CHARS, ch))
				builder.append(" ");
			else
				builder.append(ch);
		}
		
		var escaped = builder.toString()
				.replace("AND", "and")
				.replace("OR", "or")
				.replace("NOT", "not");
		if (StringUtils.isNotBlank(escaped))
			return escaped;
		else 
			return null;
	}
	
	public static boolean isEmpty(Query query) {
		AtomicBoolean hasFields = new AtomicBoolean(false);
		query.visit(new QueryVisitor() {

			@Override
			public boolean acceptField(String field) {
				hasFields.set(true);
				return super.acceptField(field);
			}
			
		});
		return !hasFields.get();
	}

	public static <T> T callWithSearcher(File indexDir, Function<IndexSearcher, T> func) {
		try (Directory directory = FSDirectory.open(indexDir.toPath())) {
			if (DirectoryReader.indexExists(directory)) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					return func.apply(new IndexSearcher(reader));
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T callWithWriter(File indexDir, Analyzer analyzer, Function<IndexWriter, T> func) {
		try (Directory directory = FSDirectory.open(indexDir.toPath())) {
			IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
			writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			try (IndexWriter writer = new IndexWriter(directory, writerConfig)) {
				try {
					var result = func.apply(writer);
					writer.commit();
					return result;
				} catch (Exception e) {
					writer.rollback();
					throw ExceptionUtils.unchecked(e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
