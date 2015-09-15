package com.pmease.commons.util;

import java.util.List;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import com.google.common.collect.Lists;

/**
 * Text node visitor, see post on SO:
 * http://stackoverflow.com/a/6594828/4158661
 * 
 */
public abstract class TextNodeVisitor implements NodeVisitor {

	private final List<TextNode> matchedNodes = Lists.newArrayList();

	abstract protected boolean isApplicable(TextNode node);

	@Override
	public void head(Node node, int depth) {
	}

	@Override
	public void tail(Node node, int depth) {
		if (node instanceof TextNode) {
			TextNode tn = (TextNode) node;
			if (isApplicable(tn))
				matchedNodes.add(tn);
		}
	}

	public List<TextNode> getMatchedNodes() {
		return matchedNodes;
	}
}
