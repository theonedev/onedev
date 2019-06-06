package io.onedev.server.search.code.query;

import static io.onedev.server.search.code.FieldConstants.BLOB_PATH;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.search.code.hit.PathHit;
import io.onedev.server.search.code.hit.QueryHit;

public class PathQuery extends BlobQuery {

	private final String match;
	
	public PathQuery(@Nullable String directory, String match, int count) {
		super(directory, count);
		this.match = match;
	}

	@Override
	public void collect(IndexSearcher searcher, TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		LinearRange range = PathUtils.matchSegments(blobPath, match, true);
		if (range != null) {
			hits.add(new PathHit(blobPath, range));
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery.Builder builder) {
		builder.add(new WildcardQuery(new Term(BLOB_PATH.name(), "*"+match+"*")), Occur.MUST);
	}
		
}
