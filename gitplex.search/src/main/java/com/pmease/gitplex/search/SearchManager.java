package com.pmease.gitplex.search;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.TooGeneralQueryException;

public interface SearchManager extends IndexListener {

	/**
	 * Search specified repository with specified revision and query.
	 * 
	 * @return
	 * 			list of sorted query results, with most relevant result coming first
	 * @throws 
	 * 			TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 * 			InterruptedException if the search process is interrupted
	 */
	List<QueryHit> search(Depot depot, ObjectId commit, BlobQuery query) 
			throws InterruptedException, TooGeneralQueryException;
	
	void closeSearcher(Depot depot);
}
