package com.gitplex.server.search.query;

import static com.gitplex.server.search.FieldConstants.BLOB_PATH;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.server.search.hit.PathHit;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.symbolextractor.Range;

public class PathQuery extends BlobQuery {

	private final String subPath;
	
	public PathQuery(@Nullable String directory, String subPath, int count) {
		super(directory, count);
		this.subPath = subPath;
	}

	@Override
	public void collect(TreeWalk treeWalk, List<QueryHit> hits) {
		String blobPath = treeWalk.getPathString();
		int index = blobPath.indexOf(subPath);
		if (index != -1) {
			Range matchRange = new Range(index, index+subPath.length());
			hits.add(new PathHit(blobPath, matchRange));
		}
	}

	@Override
	protected void applyConstraints(BooleanQuery query) {
		query.add(new WildcardQuery(new Term(BLOB_PATH.name(), "*"+subPath+"*")), Occur.MUST);
	}
		
}
