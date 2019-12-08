package io.onedev.server.util;

import java.util.List;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import com.google.common.collect.Lists;

public class TextNodeVisitor implements NodeVisitor {

	private final List<TextNode> matchedNodes = Lists.newArrayList();

	protected boolean isApplicable(TextNode node) {
		return true;
	}

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
