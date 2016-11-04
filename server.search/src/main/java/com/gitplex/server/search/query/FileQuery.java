package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_NAME;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Splitter;
import com.gitplex.commons.util.Range;
import com.gitplex.commons.util.match.WildcardUtils;
import com.gitplex.server.search.hit.FileHit;
import com.gitplex.server.search.hit.QueryHit;

public class FileQuery extends BlobQuery {

	private final String fileNames;
	
	private final String excludeFileName;
	
	private final boolean caseSensitive;
	
	private final String directory;
	
	public FileQuery(String fileNames, @Nullable String excludeFileName, boolean caseSensitive,  
			@Nullable String directory, int count) {
		super(count);
		
		this.fileNames = fileNames;
		this.excludeFileName = excludeFileName;
		this.caseSensitive = caseSensitive;
		this.directory = directory;
	}

	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		String blobName = blobPath.substring(blobPath.lastIndexOf('/')+1);
		if (caseSensitive) {
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames)) {
				if (WildcardUtils.matchString(pattern, blobName) 
						&& (excludeFileName == null || !excludeFileName.equals(blobName))) {
					Range matchRange = WildcardUtils.rangeOfMatch(pattern, blobName);
					hits.add(new FileHit(blobPath, matchRange));
					break;
				}
			}
		} else {
			for (String pattern: Splitter.on(",").omitEmptyStrings().trimResults().split(fileNames.toLowerCase())) {
				if (WildcardUtils.matchString(pattern, blobName.toLowerCase()) 
						&& (excludeFileName == null || !excludeFileName.equalsIgnoreCase(blobName))) {
					Range matchRange = WildcardUtils.rangeOfMatch(pattern, blobName.toLowerCase());
					hits.add(new FileHit(blobPath, matchRange));
					break;
				}
			}
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
