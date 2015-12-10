package com.pmease.commons.antlr.codeassist.parse;

import java.util.HashSet;
import java.util.Set;

public class ParseState {
	
	private final Set<ParseNode> nodes = new HashSet<>();
	
	public Set<ParseNode> getNodes() {
		return nodes;
	}
	
	public void predict() {
		for (ParseNode node: nodes) {
			
		}
	}
	
}
