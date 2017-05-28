package com.gitplex.server.search;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.jsymbol.Symbol;
import com.gitplex.server.model.Project;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.query.BlobQuery;
import com.gitplex.server.search.query.TooGeneralQueryException;

public interface SearchManager {

	/**
	 * Search specified project with specified revision and query.
	 * 
	 * @return
	 * 			list of sorted query results, with most relevant result coming first
	 * @throws 
	 * 			TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 * 			InterruptedException if the search process is interrupted
	 */
	List<QueryHit> search(Project project, ObjectId commit, BlobQuery query) 
			throws InterruptedException, TooGeneralQueryException;
	
	@Nullable
	List<Symbol> getSymbols(Project project, ObjectId blobId, String blobPath);
	
	@Nullable
	List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath);
	
}