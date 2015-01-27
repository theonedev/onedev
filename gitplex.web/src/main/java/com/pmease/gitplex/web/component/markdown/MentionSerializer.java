package com.pmease.gitplex.web.component.markdown;

import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class MentionSerializer implements ToHtmlSerializerPlugin {

	@Override
	public boolean visit(Node node, Visitor visitor, Printer printer) {
		if (node instanceof MentionNode) {
			MentionNode mentionNode = (MentionNode) node;
			printer.print(" <a href='http://" + mentionNode.getUserName() + "'></a> ");
			return true;
		}
		return false;
	}
}