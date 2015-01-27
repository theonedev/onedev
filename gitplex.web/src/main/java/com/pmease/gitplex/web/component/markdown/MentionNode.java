package com.pmease.gitplex.web.component.markdown;

import java.util.ArrayList;
import java.util.List;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

public class MentionNode extends AbstractNode {

	private final String userName;

	public MentionNode(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}

}