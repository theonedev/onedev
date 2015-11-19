package com.pmease.commons.antlr;

import java.util.List;

import com.pmease.commons.antlr.codeassist.Node;

public interface NodeFiller {
	
	List<Node> fill(Node template);
	
}
