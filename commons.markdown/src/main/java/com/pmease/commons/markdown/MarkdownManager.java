package com.pmease.commons.markdown;

import org.pegdown.ast.RootNode;

public interface MarkdownManager {
	
	RootNode toAST(String markdown); 

	String toHtml(RootNode ast, boolean sanitizeHtml, boolean applyTransformers);

	String toHtml(String markdown, boolean sanitizeHtml, boolean applyTransformers);

}
