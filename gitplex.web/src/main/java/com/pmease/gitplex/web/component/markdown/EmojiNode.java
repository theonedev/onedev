package com.pmease.gitplex.web.component.markdown;

import java.util.ArrayList;
import java.util.List;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

public class EmojiNode extends AbstractNode {

	private final String emojiName;

	public EmojiNode(String emojiName) {
		this.emojiName = emojiName;
	}

	public String getEmojiName() {
		return emojiName;
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