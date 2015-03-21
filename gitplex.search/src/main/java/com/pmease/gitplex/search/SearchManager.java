package com.pmease.gitplex.search;

import java.util.List;
import java.util.concurrent.Future;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;

public interface SearchManager extends IndexListener {
	
	Future<List<QueryHit>> search(Repository repository, String commitHash, BlobQuery query);
	
}
