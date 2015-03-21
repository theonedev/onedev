package com.pmease.gitplex.search.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Splitter;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.search.FieldConstants;
import com.pmease.gitplex.search.IndexConstants;
import com.pmease.gitplex.search.hit.ContentHit;
import com.pmease.gitplex.search.hit.QueryHit;

public class ContentQuery extends BlobQuery {

	public ContentQuery(String searchFor, boolean caseSensitive, int count) {
		super(FieldConstants.BLOB_CONTENT.name(), searchFor, false, 
				caseSensitive, count, IndexConstants.CONTENT_GRAM_SIZE);
	}

	@Override
	public void check(TreeWalk treeWalk, List<QueryHit> hits) {
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
						List<ContentHit.Match> matches = new ArrayList<>();
						String normalizedLine = line;
						if (!isCaseSensitive())
							normalizedLine = line.toLowerCase();
						int start = normalizedLine.indexOf(searchFor, 0);
						while (start != -1) {
							int end = start + searchFor.length();
							matches.add(new ContentHit.Match(start, end));
							start = normalizedLine.indexOf(searchFor, end);
						}
						if (!matches.isEmpty()) {
							ContentHit hit = new ContentHit(blobPath, line, lineNo, matches);
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
