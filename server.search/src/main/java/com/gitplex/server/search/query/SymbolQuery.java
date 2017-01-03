package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_NAME;
import static com.gitplex.server.search.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static com.gitplex.server.search.FieldConstants.BLOB_SECONDARY_SYMBOLS;
import static com.gitplex.server.search.IndexConstants.MAX_INDEXABLE_SIZE;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.commons.util.ContentDetector;
import com.gitplex.commons.util.match.WildcardUtils;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.hit.SymbolHit;
import com.gitplex.symbolextractor.ExtractException;
import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.Symbol;
import com.gitplex.symbolextractor.SymbolExtractor;
import com.gitplex.symbolextractor.SymbolExtractorRegistry;
import com.google.common.base.Splitter;

public class SymbolQuery extends BlobQuery {

	private static final Logger logger = LoggerFactory.getLogger(SymbolQuery.class);

	private final String term;

	private final String excludeTerm;
	
	private final boolean primary;
	
	private final boolean caseSensitive;
	
	private final String fileNames;
	
	public SymbolQuery(String term, @Nullable String excludeTerm, boolean primary, 
			boolean caseSensitive, @Nullable String directory, @Nullable String fileNames, 
			int count) {
		super(directory, count);
		
		this.term = term;
		this.excludeTerm = excludeTerm;
		this.primary = primary;
		this.caseSensitive = caseSensitive;
		this.fileNames = fileNames;
	}

	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		
		SymbolExtractor extractor = SymbolExtractorRegistry.getExtractor(blobPath);
		if (extractor != null) {
			ObjectLoader objectLoader;
			try {
				objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				if (objectLoader.getSize() <= MAX_INDEXABLE_SIZE) {
					byte[] bytes = objectLoader.getCachedBytes();
					String content = ContentDetector.convertToText(bytes, blobPath);
					if (content != null) {
						try {
							for (Symbol symbol: extractor.extract(content)) {
								if (hits.size() < getCount()) {
									if (primary == symbol.isPrimary() && symbol.getIndexName() != null) {
										String normalizedTerm;
										if (!caseSensitive)
											normalizedTerm = term.toLowerCase();
										else
											normalizedTerm = term;
										
										String normalizedSymbolName;
										if (!caseSensitive)
											normalizedSymbolName = symbol.getIndexName().toLowerCase();
										else
											normalizedSymbolName = symbol.getIndexName();
										
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
												&& (normalizedExcludeTerm == null || !normalizedSymbolName.equals(normalizedExcludeTerm))) {
											Range matchRange = WildcardUtils.rangeOfMatch(normalizedTerm, normalizedSymbolName);
											hits.add(new SymbolHit(blobPath, symbol, matchRange));
										}
									}
								} else {
									break;
								}
							}
						} catch (ExtractException e) {
							logger.debug("Error extracting symbols from blob (hash:" 
									+ treeWalk.getObjectId(0).getName() + ", path:" + blobPath + ")", e);
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
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
