package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class SpecMatch {

	private final List<TokenNode> paths;
	
	private final boolean matched;
	
	public SpecMatch(List<TokenNode> paths, boolean matched) {
		this.paths = paths;
		this.matched = matched;
	}

	public List<TokenNode> getPaths() {
		return paths;
	}

	public boolean isMatched() {
		return matched;
	}

}
