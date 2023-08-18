package io.onedev.server.ee.xsearch;

import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.ee.xsearch.query.BlobQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;

import java.util.List;

public interface CodeSearchManager {

	List<BlobMatch> search(BlobQuery query) throws TooGeneralQueryException;
	
	void indexUpdated();
	
}