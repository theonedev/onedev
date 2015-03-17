package com.pmease.gitplex.search;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.Query;

import com.pmease.gitplex.core.model.Repository;

public interface SearchManager {
	
	List<SearchHit> search(Repository repository, String commitHash, Query query, 
			@Nullable SearchHit startFrom, int numResults);
	
}
