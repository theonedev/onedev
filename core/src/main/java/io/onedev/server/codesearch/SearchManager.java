package io.onedev.server.codesearch;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.jsymbol.Symbol;
import io.onedev.server.codesearch.hit.QueryHit;
import io.onedev.server.codesearch.query.BlobQuery;
import io.onedev.server.codesearch.query.TooGeneralQueryException;
import io.onedev.server.model.Project;

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