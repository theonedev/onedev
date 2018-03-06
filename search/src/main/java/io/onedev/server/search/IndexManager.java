package io.onedev.server.search;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.jsymbol.Symbol;
import io.onedev.jsymbol.SymbolExtractor;
import io.onedev.server.model.Project;

public interface IndexManager {
	
	void indexAsync(Project project, ObjectId commit);
	
	boolean isIndexed(Project project, ObjectId commit);
	
	String getIndexVersion();
	
	String getIndexVersion(@Nullable SymbolExtractor<Symbol> extractor);
	
}
