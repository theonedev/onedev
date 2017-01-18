package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_PATH;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.commons.util.PathUtils;
import com.gitplex.server.search.hit.PathHit;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.jsymbol.Range;

public class PathQuery extends BlobQuery {

	private final String match;
	
	public PathQuery(@Nullable String directory, String match, int count) {
		super(directory, count);
		this.match = match;
	}

	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		Range range = PathUtils.matchSegments(blobPath, match, true);
		if (range != null) {
			hits.add(new PathHit(blobPath, range));
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery query) {
		query.add(new WildcardQuery(new Term(BLOB_PATH.name(), "*"+match+"*")), Occur.MUST);
	}
		
}
