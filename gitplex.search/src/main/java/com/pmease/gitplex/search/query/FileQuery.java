package com.pmease.gitplex.search.query;

import static com.pmease.gitplex.search.FieldConstants.BLOB_NAME;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Splitter;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.search.hit.FileHit;
import com.pmease.gitplex.search.hit.QueryHit;

public class FileQuery extends BlobQuery {

	private final String fileNames;
	
	private final boolean caseSensitive;
	
	private final String directory;
	
	public FileQuery(String fileNames, boolean caseSensitive, @Nullable String directory, int count) {
		super(count);
		
		this.fileNames = fileNames;
		this.caseSensitive = caseSensitive;
		this.directory = directory;
	}

	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		if (caseSensitive) {
			String blobName;
			int index = blobPath.indexOf('/');
			if (index != -1)
				blobName = StringUtils.substringAfterLast(blobPath, "/");
			else
				blobName = blobPath;

			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames)) {
				if (WildcardUtils.matchString(pattern, blobName)) {
					hits.add(new FileHit(blobPath));
					break;
				}
			}
		} else {
			hits.add(new FileHit(blobPath));
		}
	}

	@Override
	public Query asLuceneQuery() throws TooGeneralQueryException {
		BooleanQuery query = new BooleanQuery(true);

		if (directory != null)
			applyDirectory(query, directory);

		boolean tooGeneral = true;
		for (char ch: fileNames.toCharArray()) {
			if (ch != '?' && ch != '*' && ch != ',' && ch != '.') {
				tooGeneral = false;
				break;
			}
		}
		if (tooGeneral)
			throw new TooGeneralQueryException();
		
		BooleanQuery subQuery = new BooleanQuery(true);
		for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase()))
			subQuery.add(new WildcardQuery(new Term(BLOB_NAME.name(), pattern)), Occur.SHOULD);
		
		if (subQuery.getClauses().length != 0)
			query.add(subQuery, Occur.MUST);
		else
			throw new TooGeneralQueryException();

		return query;
	}

}
