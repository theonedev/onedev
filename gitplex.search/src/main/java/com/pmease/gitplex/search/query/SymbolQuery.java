package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.lang.Extractor;
import com.pmease.commons.lang.Extractors;
import com.pmease.commons.lang.Symbol;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.SymbolHit;

public class SymbolQuery extends BlobQuery {

	public SymbolQuery(String searchFor, boolean exactMatch, boolean caseSensitive, int count) {
		super(FieldConstants.BLOB_SYMBOLS.name(), searchFor, exactMatch, caseSensitive, 
				count, IndexConstants.MIN_SYMBOLS_GRAM, IndexConstants.MAX_SYMBOLS_GRAM);
	}

	@Override
	public void hit(TreeWalk treeWalk) {
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
						for (Symbol match: symbol.search(getSearchFor(), isExactMatch(), isCaseSensitive())) {
							getHits().add(new SymbolHit(blobPath, match));
							if (getHits().size() >= getCount())
								break;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} 

		if (!isCaseSensitive()) {
			blobPath = blobPath.toLowerCase();
			searchFor = searchFor.toLowerCase();
		}
		if (blobPath.startsWith(searchFor))
			getHits().add(new FileHit(treeWalk.getPathString()));
	}

}
