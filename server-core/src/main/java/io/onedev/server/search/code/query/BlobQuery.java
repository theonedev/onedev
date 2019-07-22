package io.onedev.server.search.code.query;

import static io.onedev.server.search.code.FieldConstants.BLOB_PATH;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.server.search.code.hit.QueryHit;

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
		BooleanQuery.Builder luceneQueryBuilder = new BooleanQuery.Builder();
		
		String directory = this.directory;
		if (directory != null) {
			if (!directory.endsWith("/"))
				directory += "/";
			luceneQueryBuilder.add(new WildcardQuery(BLOB_PATH.term(directory + "*")), Occur.MUST);
		}
		
		applyConstraints(luceneQueryBuilder);
		
		return luceneQueryBuilder.build();
	}

	protected abstract void applyConstraints(BooleanQuery.Builder query);
	
}
