package io.onedev.server.search.code.query;

import static io.onedev.server.search.code.FieldConstants.BLOB_NAME;
import static io.onedev.server.search.code.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static io.onedev.server.search.code.FieldConstants.BLOB_SECONDARY_SYMBOLS;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.OneDev;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.util.match.WildcardUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

public class SymbolQuery extends BlobQuery {

	private final String term;

	private final String excludeTerm;
	
	private final String excludeBlobPath;
	
	private final Boolean primary;
	
	private final Boolean local;
	
	private final boolean caseSensitive;
	
	private final String fileNames;
	
	private SymbolQuery(String term, @Nullable String excludeTerm, @Nullable String excludeBlobPath, 
			@Nullable Boolean primary, @Nullable Boolean local, boolean caseSensitive, @Nullable String directory, 
			@Nullable String fileNames, int count) {
		super(directory, count);
		
		this.term = term;
		this.excludeTerm = excludeTerm;
		this.excludeBlobPath = excludeBlobPath;
		this.primary = primary;
		this.local = local;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}
 
	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		ObjectId blobId = treeWalk.getObjectId(0);
		
		List<Symbol> symbols = OneDev.getInstance(SearchManager.class).getSymbols(searcher, blobId, blobPath);
		if (symbols != null) {
			for (Symbol symbol: symbols) {
				if (hits.size() < getCount()) {
					if ((primary==null || primary.booleanValue() == symbol.isPrimary()) 
							&& symbol.getName() != null 
							&& symbol.isSearchable()
							&& (local == null || local.booleanValue() == symbol.isLocalInHierarchy())) {
						String normalizedTerm;
						if (!caseSensitive)
							normalizedTerm = term.toLowerCase();
						else
							normalizedTerm = term;
						
						String normalizedSymbolName;
						if (!caseSensitive)
							normalizedSymbolName = symbol.getName().toLowerCase();
						else
							normalizedSymbolName = symbol.getName();
						
						String normalizedExcludeTerm;
						if (excludeTerm != null) {
							if (!caseSensitive)
								normalizedExcludeTerm = excludeTerm.toLowerCase();
							else
								normalizedExcludeTerm = excludeTerm;
						} else {
							normalizedExcludeTerm = null;
						}
						if (WildcardUtils.matchString(normalizedTerm, normalizedSymbolName)
								&& (normalizedExcludeTerm == null || !normalizedSymbolName.equals(normalizedExcludeTerm))
								&& (excludeBlobPath == null || !excludeBlobPath.equals(blobPath))) {
							LinearRange match = WildcardUtils.rangeOfMatch(normalizedTerm, normalizedSymbolName);
							hits.add(new SymbolHit(blobPath, symbol, match));
						}
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		if (fileNames != null) {
			BooleanQuery.Builder subQueryBuilder = new BooleanQuery.Builder();
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
				subQueryBuilder.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), Occur.SHOULD);
			BooleanQuery subQuery = subQueryBuilder.build();
			if (subQuery.clauses().size() != 0)
				builder.add(subQuery, Occur.MUST);
		}

		boolean tooGeneral = true;
		for (char ch: term.toCharArray()) {
			if (ch != '?' && ch != '*') {
				tooGeneral = false;
				break;
			}
		}
		if (tooGeneral)
			throw new TooGeneralQueryException();

		if (primary != null) {
			String fieldName;
			if (primary)
				fieldName = BLOB_PRIMARY_SYMBOLS.name();
			else
				fieldName = BLOB_SECONDARY_SYMBOLS.name();
			
			builder.add(new WildcardQuery(new Term(fieldName, term.toLowerCase())), Occur.MUST);
		}
	}
	
	public static class Builder {

		private String term;
		
		private int count;
		
		private String directory;

		private String excludeTerm;
		
		private String excludeBlobPath;
		
		private Boolean primary;
		
		private Boolean local;
		
		private boolean caseSensitive;
		
		private String fileNames;
		
		public Builder term(String term) {
			this.term = term;
			return this;
		}
		
		public Builder count(int count) {
			this.count = count;
			return this;
		}
		
		public Builder directory(String directory) {
			this.directory = directory;
			return this;
		}
		
		public Builder excludeTerm(String excludeTerm) {
			this.excludeTerm = excludeTerm;
			return this;
		}
		
		public Builder excludeBlobPath(String excludeBlobPath) {
			this.excludeBlobPath = excludeBlobPath;
			return this;
		}
		
		public Builder primary(Boolean primary) {
			this.primary = primary;
			return this;
		}
		
		public Builder local(Boolean local) {
			this.local = local;
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
		
		public SymbolQuery build() {
			Preconditions.checkArgument(term!=null, "Query term should be specified");
			Preconditions.checkArgument(count!=0, "Query count should be specified");
			return new SymbolQuery(term, excludeTerm, excludeBlobPath, primary, local, 
					caseSensitive, directory, fileNames, count);
		}
		
	}
}
