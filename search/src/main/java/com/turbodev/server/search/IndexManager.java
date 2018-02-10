package com.turbodev.server.search;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.turbodev.jsymbol.Symbol;
import com.turbodev.jsymbol.SymbolExtractor;
import com.turbodev.server.model.Project;

public interface IndexManager {
	
	void indexAsync(Project project, ObjectId commit);
	
	boolean isIndexed(Project project, ObjectId commit);
	
	String getIndexVersion();
	
	String getIndexVersion(@Nullable SymbolExtractor<Symbol> extractor);
	
}
