package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.TextHit;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class TextQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;

	private final String term;

	private final boolean regex;

	private final boolean wholeWord;

	private final boolean caseSensitive;

	private final String fileNames;
	
	private TextQuery(String term, boolean regex, boolean wholeWord, boolean caseSensitive, 
					 @Nullable String fileNames, @Nullable String directory, int count) {
		super(directory, count);
		this.term = term;
		this.regex = regex;
		this.wholeWord = wholeWord;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}

	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		ObjectLoader objectLoader;
		try {
			objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
			var blobPath = treeWalk.getPathString();
			for (var match: getOption().matches(blobPath, objectLoader, getCount() - hits.size())) 
				hits.add(new TextHit(blobPath, match.getPosition(), match.getLine()));								
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder);
	}
	
	private TextQueryOption getOption() {
		return new TextQueryOption(term, regex, wholeWord, caseSensitive, fileNames);
	}
	
	public static class Builder {

		private final String term;

		private boolean regex;

		private boolean wholeWord;

		private boolean caseSensitive;

		private String fileNames;
		
		private String directory;
		
		private int count;
		
		public Builder(String term) {
			this.term = term;
		}
		
		public Builder(TextQueryOption option) {
			this(option.getTerm());
			regex(option.isRegex());
			wholeWord(option.isWholeWord());
			caseSensitive(option.isCaseSensitive());
			fileNames(option.getFileNames());
		}

		public Builder regex(boolean regex) {
			this.regex = regex;
			return this;
		}

		public Builder wholeWord(boolean wholeWord) {
			this.wholeWord = wholeWord;
			return this;
		}

		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}

		public Builder fileNames(String fileNames) {
			this.fileNames = fileNames;
			return this;
		}

		public Builder directory(String directory) {
			this.directory = directory;
			return this;
		}

		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public TextQuery build() {
			Preconditions.checkState(term != null);
			Preconditions.checkState(count != 0);
			return new TextQuery(term, regex, wholeWord, caseSensitive, 
					fileNames, directory, count);
		}
	}
}
