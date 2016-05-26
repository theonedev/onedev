package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.FieldConstants.BLOB_NAME;
import static com.pmease.gitplex.search.FieldConstants.BLOB_PRIMARY_SYMBOLS;
import static com.pmease.gitplex.search.FieldConstants.BLOB_SECONDARY_SYMBOLS;
import static com.pmease.gitplex.search.IndexConstants.MAX_INDEXABLE_SIZE;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.Extractor;
import com.pmease.commons.lang.extractors.Extractors;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.util.ContentDetector;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.hit.SymbolHit;

public class SymbolQuery extends BlobQuery {

	private static final Logger logger = LoggerFactory.getLogger(SymbolQuery.class);

	private final String term;

	private final String excludeTerm;
	
	private final boolean primary;
	
	private final boolean caseSensitive;
	
	private final String directory;
	
	private final String fileNames;
	
	public SymbolQuery(String term, @Nullable String excludeTerm, boolean primary, 
			boolean caseSensitive, @Nullable String directory, @Nullable String fileNames, int count) {
		super(count);
		
		this.term = term;
		this.excludeTerm = excludeTerm;
		this.primary = primary;
		this.caseSensitive = caseSensitive;
		this.directory = directory;
		this.fileNames = fileNames;
	}

	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		
		Extractor extractor = GitPlex.getInstance(Extractors.class).getExtractor(blobPath);
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
									if (primary == symbol.isPrimary() && symbol.getName() != null) {
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
	public Query asLuceneQuery() throws TooGeneralQueryException {
		BooleanQuery query = new BooleanQuery(true);

		if (directory != null)
			applyDirectory(query, directory);

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
		
		return query;
	}
	
}
