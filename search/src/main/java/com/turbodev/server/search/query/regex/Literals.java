package com.turbodev.server.search.query.regex;

import java.util.List;

public interface Literals {
	
	List<List<LeafLiterals>> flattern(boolean outmost);
	
}
