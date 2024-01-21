package io.onedev.server.search.code;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.server.model.Project;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.apache.lucene.search.IndexSearcher;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.util.List;

public interface CodeSearchManager {

	/**
	 * Search specified project with specified revision and query.
	 * 
	 * @return
	 * 			list of sorted query results, with most relevant result coming first
	 * @throws 
	 * 			TooGeneralQueryException if supplied query term is too general to possibly cause query slow
	 * 			InterruptedException if the search process is interrupted
	 */
	List<QueryHit> search(Project project, ObjectId commitId, BlobQuery query) 
			throws TooGeneralQueryException;
	
	@Nullable
	List<Symbol> getSymbols(Project project, ObjectId blobId, String blobPath);
	
	@Nullable
	List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath);

	@Nullable
	String findBlobPathBySuffix(Project project, ObjectId commit, String blobPathSuffix);

	@Nullable
	SymbolHit findPrimarySymbol(Project project, ObjectId commitId, String symbolFQN,
								String fqnSeparator);
}