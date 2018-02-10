package com.turbodev.server.search.query;

import static com.turbodev.server.search.FieldConstants.BLOB_PATH;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.turbodev.server.search.hit.QueryHit;

public abstract class BlobQuery {

	private final String directory;
	
	private final int count;

	public BlobQuery(@Nullable String directory, int count) {
		this.directory = directory;
		this.count = count;
	}

	@Nullable
	public String getDirectory() {
		return directory;
	}

	public int getCount() {
		return count;
	}

	public abstract void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits);

	/**
	 * Get lucene query representation of this query
	 * 
	 * @return 
	 * 			lucene query
	 * @throws 
	 * 			TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 */
	public Query asLuceneQuery() throws TooGeneralQueryException {
		BooleanQuery luceneQuery = new BooleanQuery(true);
		
		String directory = this.directory;
		if (directory != null) {
			if (!directory.endsWith("/"))
				directory += "/";
			luceneQuery.add(new WildcardQuery(BLOB_PATH.term(directory + "*")), Occur.MUST);
		}
		
		applyConstraints(luceneQuery);
		
		return luceneQuery;
	}

	protected abstract void applyConstraints(BooleanQuery query);
	
}
