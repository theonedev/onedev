package io.onedev.server.search.code;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.SymbolExtractor;

public interface CodeIndexManager {
	
	void indexAsync(Long projectId, ObjectId commitId);
	
	boolean isIndexed(Long projectId, ObjectId commitId);
	
	String getIndexVersion(@Nullable SymbolExtractor<Symbol> extractor);
	
}
