package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_NAME;
import static com.gitplex.server.search.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static com.gitplex.server.search.FieldConstants.BLOB_SECONDARY_SYMBOLS;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.commons.util.match.WildcardUtils;
import com.gitplex.jsymbol.Range;
import com.gitplex.jsymbol.Symbol;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.search.SearchManager;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.hit.SymbolHit;
import com.google.common.base.Splitter;

public class SymbolQuery extends BlobQuery {

	private final String term;

	private final String excludeTerm;
	
	private final String excludeBlobPath;
	
	private final boolean primary;
	
	private final boolean caseSensitive;
	
	private final String fileNames;
	
	private final SourceContext sourceContext;
	
	public SymbolQuery(String term, @Nullable String excludeTerm, @Nullable String excludeBlobPath, 
			boolean primary, boolean caseSensitive, @Nullable String directory, @Nullable String fileNames, 
			@Nullable SourceContext sourceContext, int count) {
		super(directory, count);
		
		this.term = term;
		this.excludeTerm = excludeTerm;
		this.excludeBlobPath = excludeBlobPath;
		this.primary = primary;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
		this.sourceContext = sourceContext;
	}
 
	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		ObjectId blobId = treeWalk.getObjectId(0);
		
		List<Symbol> symbols = GitPlex.getInstance(SearchManager.class).getSymbols(searcher, blobId, blobPath);
		if (symbols != null) {
			for (Symbol symbol: symbols) {
				if (hits.size() < getCount()) {
					if (primary == symbol.isPrimary() && symbol.getName() != null && !symbol.isEffectivelyLocal()) {
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
								&& (excludeBlobPath == null || !excludeBlobPath.equals(blobPath))
								&& (sourceContext == null || sourceContext.maybeUsing(symbol))) {
							Range matchRange = WildcardUtils.rangeOfMatch(normalizedTerm, normalizedSymbolName);
							hits.add(new SymbolHit(blobPath, symbol, matchRange));
						}
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery query) {
		if (fileNames != null) {
			BooleanQuery subQuery = new BooleanQuery(true);
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
				subQuery.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), Occur.SHOULD);
			if (subQuery.getClauses().length != 0)
				query.add(subQuery, Occur.MUST);
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
		
		String fieldName;
		if (primary)
			fieldName = BLOB_PRIMARY_SYMBOLS.name();
		else
			fieldName = BLOB_SECONDARY_SYMBOLS.name();
		
		query.add(new WildcardQuery(new Term(fieldName, term.toLowerCase())), Occur.MUST);
	}
	
}
