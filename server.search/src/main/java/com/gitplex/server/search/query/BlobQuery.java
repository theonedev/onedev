package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_PATH;

import java.util.List;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.server.search.hit.QueryHit;

public abstract class BlobQuery {

	private final int count;

	public BlobQuery(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public abstract void collect(TreeWalk treeWalk, List<QueryHit> hits);

	/**
	 * Get lucene query representation of this query
	 * 
	 * @return 
	 * 			lucene query
	 * @throws 
	 * 			TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 */
	public abstract Query asLuceneQuery() throws TooGeneralQueryException;

	protected void applyDirectory(BooleanQuery query, String directory) {
		if (!directory.endsWith("/"))
			directory += "/";
		query.add(new WildcardQuery(BLOB_PATH.term(directory + "*")), Occur.MUST);
	}
}
