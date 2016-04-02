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
import com.pmease.commons.util.Range;
import com.pmease.commons.util.match.WildcardUtils;
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
		String blobName = blobPath.substring(blobPath.lastIndexOf('/')+1);
		if (caseSensitive) {
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames)) {
				Range matchRange = WildcardUtils.rangeOfMatch(pattern, blobName);
				if (matchRange != null) {
					hits.add(new FileHit(blobPath, matchRange));
					break;
				}
			}
		} else {
			Range matchRange = null;
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase())) {
				matchRange = WildcardUtils.rangeOfMatch(pattern, blobName.toLowerCase());
				if (matchRange != null) {
					break;
				}
			}
			hits.add(new FileHit(blobPath, matchRange));
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
