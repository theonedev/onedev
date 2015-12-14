package com.pmease.commons.antlr.parser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammar.ElementSpec;

public class Element {
	
	private final ElementSpec spec;
	
	private final int endTokenIndex;
	
	private final Node node;
	
	private final EarleyParser parser;
	
	public Element(EarleyParser parser, @Nullable ElementSpec spec, int endTokenIndex, @Nullable Node node) {
		this.parser = parser;
		this.spec = spec;
		this.endTokenIndex = endTokenIndex;
		this.node = node;
	}

	@Nullable
	public ElementSpec getSpec() {
		return spec;
	}
	
	public boolean isRoot() {
		return spec == null;
	}
	
	public boolean isTerminal() {
		return node == null;
	}
	
	@Nullable
	public String getLabel() {
		return spec!=null?spec.getLabel():null;
	}

	/**
	 * Get index of the next token after the match 
	 * 
	 * @return
	 * 			token index after match of this element, this is NOT index of the 
	 * 			last token matching the element, it is index of the next token 
	 * 			after the match
	 */
	public int getEndTokenIndex() {
		return endTokenIndex;
	}

	@Nullable
	public Node getNode() {
		return node;
	}
	
	/**
	 * Get tokens matched by this element so far.
	 * 
	 * @return
	 * 			tokens matched by this element so far, or empty if this element does not 
	 * 			match any tokens yet
	 */
	public List<Token> getMatchedTokens() {
		if (node != null)
			return parser.getTokens().subList(node.getBeginTokenIndex(), endTokenIndex);
		else if (endTokenIndex > 0)
			return parser.getTokens().subList(endTokenIndex-1, endTokenIndex);
		else
			return new ArrayList<>();
	}
	
	/**
	 * Get the first token matched by this element. 
	 * 
	 * @return
	 * 			first token matched by this element, or <tt>null</tt> if this element 
	 * 			does not match any tokens yet
	 */
	@Nullable
	public Token getFirstMatchedToken() {
		if (node != null) {
			if (endTokenIndex > node.getBeginTokenIndex())
				return parser.getTokens().get(node.getBeginTokenIndex());
			else
				return null;
		} else if (endTokenIndex > 0) {
			return parser.getTokens().get(endTokenIndex-1);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the last token matched by this element. 
	 * 
	 * @return
	 * 			last token matched by this element, or <tt>null</tt> if this element does not 
	 * 			match any tokens yet
	 */
	@Nullable
	public Token getLastMatchedToken() {
		if (node != null) {
			if (endTokenIndex > node.getBeginTokenIndex())
				return parser.getTokens().get(endTokenIndex-1);
			else
				return null;
		} else if (endTokenIndex > 0) {
			return parser.getTokens().get(endTokenIndex-1);
		} else {
			return null;
		}
	}
	
	public String getMatchedText() {
		StringBuilder builder = new StringBuilder();
		for (Token token: getMatchedTokens())
			builder.append(token.getText());
		return builder.toString();
	}
	
	public List<Element> getChildrenByLabel(String label, boolean recursive) {
		List<Element> children = new ArrayList<>();
		if (node != null) {
			for (Element child: node.getElements()) {
				if (label.equals(child.getLabel()))
					children.add(child);
				if (recursive)
					children.addAll(child.getChildrenByLabel(label, recursive));
			}
		}
		return children;
	}
	
	public List<Element> getChildrenByRule(String ruleName, boolean recursive) {
		List<Element> children = new ArrayList<>();
		if (node != null) {
			for (Element child: node.getElements()) {
				if (ruleName.equals(child.getNode().getRuleSpec().getName()))
					children.add(child);
				if (recursive)
					children.addAll(child.getChildrenByRule(ruleName, recursive));
			}
		}
		return children;
	}

	public EarleyParser getParser() {
		return parser;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("(");
		if (spec != null)
			builder.append("spec: " + spec + ", ");
		if (node != null)
			builder.append("node: " + node + ", ");
		builder.append("endTokenIndex: " + endTokenIndex + ")");
		return builder.toString();
	}
	
}
