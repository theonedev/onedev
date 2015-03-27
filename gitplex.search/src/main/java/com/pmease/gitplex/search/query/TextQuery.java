package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Splitter;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.TextHit;
import com.pmease.gitplex.search.hit.QueryHit;

public class TextQuery extends BlobQuery {

	public TextQuery(String searchFor, @Nullable String pathPrefix, @Nullable String pathSuffix, 
			boolean caseSensitive, boolean regex, int count) {
		super(FieldConstants.BLOB_TEXT.name(), searchFor, pathPrefix, pathSuffix, false, 
				caseSensitive, regex, count, IndexConstants.NGRAM_SIZE);
	}

	public TextQuery(String searchFor, boolean caseSensitive, boolean regex, int count) {
		this(searchFor, null, null, caseSensitive, regex, count);
	}
	
	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		ObjectLoader objectLoader;
		try {
			objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
			if (objectLoader.getSize() <= IndexConstants.MAX_INDEXABLE_SIZE) {
				byte[] bytes = objectLoader.getCachedBytes();
				Charset charset = Charsets.detectFrom(bytes);
				if (charset != null) {
					String blobPath = treeWalk.getPathString();
					String content = new String(bytes, charset);
					String searchFor = getSearchFor();
					if (!isCaseSensitive())
						searchFor = searchFor.toLowerCase();
					
					int lineNo = 0;
					for (String line: Splitter.on("\n").split(content)) {
						List<TextHit.Match> matches = new ArrayList<>();
						String normalizedLine = line;
						if (!isCaseSensitive())
							normalizedLine = line.toLowerCase();
						int start = normalizedLine.indexOf(searchFor, 0);
						while (start != -1) {
							int end = start + searchFor.length();
							matches.add(new TextHit.Match(start, end));
							start = normalizedLine.indexOf(searchFor, end);
						}
						if (!matches.isEmpty()) {
							TextHit hit = new TextHit(blobPath, line, lineNo, matches);
							hits.add(hit);
							if (hits.size() >= getCount())
								break;
						}
						
						lineNo++;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
