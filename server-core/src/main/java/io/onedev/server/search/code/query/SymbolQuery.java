package io.onedev.server.search.code.query;

import com.google.common.base.Preconditions;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.server.OneDev;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

import javax.annotation.Nullable;
import java.util.List;

public class SymbolQuery extends BlobQuery {

	private static final long serialVersionUID = 1L;

	private final String term;

	private final boolean caseSensitive;

	private final String fileNames;

	private final String excludeTerm;
	
	private final String excludeBlobPath;
	
	private final Boolean primary;
	
	private final Boolean local;
	
	private SymbolQuery(String term, boolean caseSensitive, String fileNames, 
						@Nullable String excludeTerm, @Nullable String excludeBlobPath, 
						@Nullable Boolean primary, @Nullable Boolean local, 
						@Nullable String directory, int count) {
		super(directory, count);

		this.term = term;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
		this.excludeTerm = excludeTerm;
		this.excludeBlobPath = excludeBlobPath;
		this.primary = primary;
		this.local = local;
	}
	
	private SymbolQueryOption getOption() {
		return new SymbolQueryOption(term, caseSensitive, fileNames);
	}
 
	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		ObjectId blobId = treeWalk.getObjectId(0);
		
		List<Symbol> symbols = OneDev.getInstance(CodeSearchManager.class).getSymbols(searcher, blobId, blobPath);
		if (symbols != null) {
			var matches = getOption().matches(blobPath, symbols, excludeTerm, excludeBlobPath, 
					primary, local, getCount() - hits.size());
			for (var match: matches) 
				hits.add(new SymbolHit(blobPath, match.getSymbol(), match.getPosition()));				
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		getOption().applyConstraints(builder, primary);
	}

	public static class Builder {

		private final String term;

		private boolean caseSensitive;

		private String fileNames;

		private String excludeTerm;

		private String excludeBlobPath;

		private Boolean primary;

		private Boolean local;
		
		private String directory;
		
		private int count;

		public Builder(String term) {
			this.term = term;
		}
		
		public Builder(SymbolQueryOption option) {
			this(option.getTerm());
			caseSensitive(option.isCaseSensitive());
			fileNames(option.getFileNames());
		}

		public Builder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}

		public Builder fileNames(@Nullable String fileNames) {
			this.fileNames = fileNames;
			return this;
		}

		public Builder excludeTerm(@Nullable String excludeTerm) {
			this.excludeTerm = excludeTerm;
			return this;
		}

		public Builder excludeBlobPath(@Nullable String excludeBlobPath) {
			this.excludeBlobPath = excludeBlobPath;
			return this;
		}

		public Builder primary(@Nullable Boolean primary) {
			this.primary = primary;
			return this;
		}

		public Builder local(@Nullable Boolean local) {
			this.local = local;
			return this;
		}
		
		public Builder directory(@Nullable String directory) {
			this.directory = directory;
			return this;
		}

		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public SymbolQuery build() {
			Preconditions.checkState(term != null);
			Preconditions.checkState(count != 0);
			return new SymbolQuery(term, caseSensitive, fileNames, excludeTerm, 
					excludeBlobPath, primary, local, directory, count);
		}
		
	}
	
}
