package io.onedev.server.search.code.query.regex;

import java.util.List;

public interface Literals {
	
	List<List<LeafLiterals>> flattern(boolean outmost);
	
}
