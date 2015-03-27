package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

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

public class SymbolQuery extends BlobQuery {

	public SymbolQuery(String searchFor, @Nullable String pathPrefix, @Nullable String pathSuffix, 
			boolean exactMatch, boolean caseSensitive, boolean regex, int count) {
		super(FieldConstants.BLOB_SYMBOLS.name(), pathPrefix, pathSuffix, searchFor, exactMatch, 
				caseSensitive, regex, count, IndexConstants.NGRAM_SIZE);
	}

	public SymbolQuery(String searchFor, boolean exactMatch, boolean caseSensitive, boolean regex, int count) {
		this(searchFor, null, null, exactMatch, caseSensitive, regex, count);
	}
	
	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		String searchFor = getSearchFor();
		
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
						Symbol symbol = extractor.extract(content);
						for (Symbol match: symbol.search(getSearchFor(), isWordMatch(), isCaseSensitive())) {
							hits.add(new SymbolHit(blobPath, match));
							if (hits.size() >= getCount())
								break;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} 

		String blobName;
		int index = blobPath.indexOf('/');
		if (index != -1)
			blobName = StringUtils.substringAfterLast(blobPath, "/");
		else
			blobName = blobPath;
		if (!isCaseSensitive()) {
			blobName = blobName.toLowerCase();
			searchFor = searchFor.toLowerCase();
		}
		if (blobName.startsWith(searchFor))
			hits.add(new FileHit(blobPath));
	}

}
