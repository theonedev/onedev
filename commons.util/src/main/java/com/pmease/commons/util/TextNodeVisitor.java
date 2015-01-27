package com.pmease.commons.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static TextNodeVisitor all() {
		return new TextNodeVisitor() {

			@Override
			protected boolean isApplicable(TextNode node) {
				return true;
			}
		};
	}

	public static TextNodeVisitor none() {
		return new TextNodeVisitor() {

			@Override
			protected boolean isApplicable(TextNode node) {
				return false;
			}
			
		};
	}
	
	public static TextNodeVisitor matches(String pattern) {
		final Pattern p = Pattern.compile(pattern);
		return new TextNodeVisitor() {

			@Override
			protected boolean isApplicable(TextNode node) {
				String text = node.getWholeText();
				Matcher m = p.matcher(text);
				return m.matches();
			}
		};
	}
	
	@Override
	public void head(Node node, int depth) {
	}

	@Override
	public void tail(Node node, int depth) {
		if (node instanceof TextNode) {
			TextNode tn = (TextNode) node;
			if (isApplicable(tn)) {
				matchedNodes.add(tn);
			}
		}
	}

	public List<TextNode> getMatchedNodes() {
		return matchedNodes;
	}
}
