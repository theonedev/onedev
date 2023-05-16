package io.onedev.server.ee.xsearch;

import io.onedev.server.ee.xsearch.hit.QueryHit;
import io.onedev.server.ee.xsearch.query.BlobQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;

import java.util.List;

public interface CodeSearchManager {

	List<QueryHit> search(BlobQuery query) throws InterruptedException, TooGeneralQueryException;
	
	void indexUpdated();
	
}