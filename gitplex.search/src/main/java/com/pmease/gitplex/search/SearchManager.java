package com.pmease.gitplex.search;

import java.util.List;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;

public interface SearchManager extends IndexListener {
	
	List<QueryHit> search(Repository repository, String commitHash, BlobQuery query) throws InterruptedException;
	
}
