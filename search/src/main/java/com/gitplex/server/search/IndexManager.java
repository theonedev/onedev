package com.gitplex.server.search;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.jsymbol.Symbol;
import com.gitplex.jsymbol.SymbolExtractor;
import com.gitplex.server.model.Project;

public interface IndexManager {
	
	void indexAsync(Project project, ObjectId commit);
	
	boolean isIndexed(Project project, ObjectId commit);
	
	String getIndexVersion();
	
	String getIndexVersion(@Nullable SymbolExtractor<Symbol> extractor);
	
}
