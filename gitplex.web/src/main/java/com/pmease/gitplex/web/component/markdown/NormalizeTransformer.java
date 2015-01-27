package com.pmease.gitplex.web.component.markdown;

import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.gitplex.core.extensionpoint.HtmlTransformer;

public class NormalizeTransformer implements HtmlTransformer {

	private static final Collection<String> LISTS = ImmutableSet.<String>of("ul", "ol");
	
	private static final Collection<String> TABLE_CHILDREN = 
			ImmutableSet.<String>of("thead", "tbody", "tfoot", "tr", "td", "th");
	
	@Override
	public Element transform(Element body) {
		final List<Element> nodes = Lists.newArrayList();
		
		NodeVisitor visitor = new NodeVisitor() {

			@Override
			public void head(Node node, int depth) {
			}

			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Element) {
					Element e = (Element) node;
					if (e.tagName().toLowerCase().equals("li") && !JsoupUtils.hasAncestor(e, LISTS)) 
						nodes.add(e);
					else if (TABLE_CHILDREN.contains(e.tagName().toLowerCase()) && !JsoupUtils.hasAncestor(e, "table"))
						nodes.add(e);
				}
			}
		};
		
		body.traverse(visitor);
		
		for (Element node: nodes) {
			Element div = new Element(Tag.valueOf("div"), node.baseUri());
			div.appendText(node.toString());
			node.replaceWith(div);
		}
		
		return body;
	}

}
