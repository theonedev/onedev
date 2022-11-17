package io.onedev.server.util.lucene;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.bouncycastle.util.Arrays;

public class LuceneUtils {

	private static final char[] SPECIAL_CHARS = new char[] {'+', '-', '&', '|', '!', '(', ')', 
			'{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/'};
	
	public static String escape(String queryString) {
		StringBuilder builder = new StringBuilder();
		for (char ch: queryString.toCharArray()) {
			if (Arrays.contains(SPECIAL_CHARS, ch))
				builder.append('\\');
			builder.append(ch);
		}
		return builder.toString();
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
	
}
