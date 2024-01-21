package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import io.onedev.server.git.GitUtils;
import io.onedev.server.search.code.hit.FileHit;
import io.onedev.server.search.code.hit.QueryHit;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.treewalk.TreeWalk;

import javax.annotation.Nullable;
import java.util.List;

public class FileQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;

	private final String term;
	
	private final boolean caseSensitive;
	
	private final String excludeFileName;
	
	private FileQuery(String term, boolean caseSensitive, @Nullable String excludeFileName, 
					  @Nullable String directory, int count) {
		super(directory, count);

		this.term = term;
		this.caseSensitive = caseSensitive;
		this.excludeFileName = excludeFileName;
	}

	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		String blobName = GitUtils.getBlobName(blobPath);
		var match = getOption().matches(blobName, excludeFileName);
		if (match != null)
			hits.add(new FileHit(blobPath, match.orElse(null)));
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder);
	}

	private FileQueryOption getOption() {
		return new FileQueryOption(term, caseSensitive);
	}
	
	public static class Builder {

		private final String term;
		
		private boolean caseSensitive;
		
		private String excludeFileName;

		private String directory;

		private int count;

		public Builder(String term) {
			this.term = term;
		}

		public Builder(FileQueryOption option) {
			this(option.getTerm());
			caseSensitive(option.isCaseSensitive());
		}
		
		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}

		public Builder excludeFileName(@Nullable String excludeFileName) {
			this.excludeFileName = excludeFileName;
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

		public FileQuery build() {
			Preconditions.checkState(term != null);
			Preconditions.checkState(count != 0);
			return new FileQuery(term, caseSensitive, excludeFileName, directory, count);
		}
	}	
}
