package com.pmease.commons.antlr.codeassist;

import java.util.List;

public interface Suggester {
	List<InputSuggestion> suggest(String matchWith);
}
