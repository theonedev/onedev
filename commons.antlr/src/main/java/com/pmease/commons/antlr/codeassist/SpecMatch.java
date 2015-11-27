package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class SpecMatch {

	private final List<TokenNode> completePaths;
	
	private final List<TokenNode> incompletePaths;
	
	public SpecMatch(List<TokenNode> completePaths, List<TokenNode> incompletePaths) {
		this.completePaths = completePaths;
		this.incompletePaths = incompletePaths;
	}

	public List<TokenNode> getCompletePaths() {
		return completePaths;
	}

	public List<TokenNode> getIncompletePaths() {
		return incompletePaths;
	}

}
