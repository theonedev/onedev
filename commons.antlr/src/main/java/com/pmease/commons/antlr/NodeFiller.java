package com.pmease.commons.antlr;

import java.util.List;

import com.pmease.commons.antlr.parsetree.Node;

public interface NodeFiller {
	
	List<Node> fill(Node template);
	
}
