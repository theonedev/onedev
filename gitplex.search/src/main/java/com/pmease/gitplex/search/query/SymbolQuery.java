package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.lang.ExtractException;
import com.pmease.commons.lang.Extractor;
import com.pmease.commons.lang.Extractors;
import com.pmease.commons.lang.Symbol;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.hit.SymbolHit;
import com.pmease.gitplex.search.query.regex.RegexLiterals;

public class SymbolQuery extends BlobQuery {

	private static final Logger logger = LoggerFactory.getLogger(SymbolQuery.class);
	
	public SymbolQuery(String term, boolean regex, boolean exactMatch, boolean caseSensitive, 
			@Nullable String pathPrefix, Collection<String> pathSuffixes, int count) {
		super(FieldConstants.BLOB_SYMBOLS.name(), term, regex, exactMatch, 
				caseSensitive, pathPrefix, pathSuffixes, count);
	}

	public SymbolQuery(String term, boolean regex, boolean exactMatch, boolean caseSensitive, int count) {
		this(term, regex, exactMatch, caseSensitive, null, null, count);
	}
	
	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		
		Extractor extractor = GitPlex.getInstance(Extractors.class).getExtractor(blobPath);
		if (extractor != null) {
			ObjectLoader objectLoader;
			try {
				objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_SIZE) {
					byte[] bytes = objectLoader.getCachedBytes();
					Charset charset = Charsets.detectFrom(bytes);
					if (charset != null) {
						String content = new String(bytes, charset);
						try {
							for (Symbol symbol: extractor.extract(content)) {
								if (hits.size() < getCount()) {
									String name = symbol.getName();
									if (name != null) {
										if (matches(name))
											hits.add(new SymbolHit(blobPath, symbol));
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

		if (hits.size() < getCount()) {
			String blobName;
			int index = blobPath.indexOf('/');
			if (index != -1)
				blobName = StringUtils.substringAfterLast(blobPath, "/");
			else
				blobName = blobPath;
			
			if (matches(blobName))
				hits.add(new FileHit(blobPath));
		}
	}

	private boolean matches(String text) {
		Pattern pattern = getPattern();
		String searchFor = getCasedTerm();
		if (pattern != null) {
			return pattern.matcher(text).find();
		} else {
			if (isWordMatch()) 
				return getCasedText(text).equals(searchFor);
			else 
				return getCasedText(text).startsWith(searchFor);
		}
		
	}

	@Override
	protected Query asLuceneQuery(String term) {
		if (isRegex()) { 
			return new RegexLiterals(term).asWildcardQuery(getFieldName());
		} else {
			term = StringUtils.replaceChars(term, "*?", "");
			if (StringUtils.isNotBlank(term)) { 
				if (isWordMatch())
					return new TermQuery(new Term(getFieldName(), term.toLowerCase()));
				else
					return new WildcardQuery(new Term(getFieldName(), term.toLowerCase() + "*"));
			} else {
				throw new TooGeneralQueryException();
			}			
		} 
	}
	
}
