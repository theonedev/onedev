package com.pmease.gitplex.search;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.query.BlobQuery;

public interface SearchManager extends IndexListener {
	
	void search(Repository repository, String commitHash, BlobQuery query);
	
}
