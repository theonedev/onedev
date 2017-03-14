package com.gitplex.server.util;

import java.util.Collection;
import java.util.regex.Matcher;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.google.common.collect.Lists;

public class JsoupUtils {
	
	public static boolean hasAncestor(Node node, Collection<String> tags) {
		Node parent = node.parentNode();
		while (parent != null) {
			if (parent instanceof Element) {
				Element e = (Element) parent;
				if (tags.contains(e.tagName().toLowerCase())) {
					return true;
				}
			}
			
			parent = parent.parentNode();
		}
		
		return false;
	}

	public static boolean hasAncestor(Node node, String tag) {
		return hasAncestor(node, Lists.newArrayList(tag));
	}

	public static void appendReplacement(Matcher matcher, Node node, String replacement) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendReplacement(buffer, "");
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString(), node.baseUri()));
		node.before(new DataNode(replacement, node.baseUri()));
	}
	
	public static void appendTail(Matcher matcher, Node node) {
		StringBuffer buffer = new StringBuffer();
		matcher.appendTail(buffer);
		if (buffer.length() != 0)
			node.before(new TextNode(buffer.toString(), node.baseUri()));
		node.remove();
	}
}
