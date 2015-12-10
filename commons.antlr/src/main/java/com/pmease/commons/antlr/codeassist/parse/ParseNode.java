package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class ParseNode {
	
	private final int fromStreamIndex;
	
	private final RuleSpec rule;
	
	private final int alternativeIndex;
	
	private final int nextElementIndex;
	
	private final boolean nextElementMatched;
	
	private final List<ParseNode> children = new ArrayList<>();
	
	private transient List<ElementSpec> elements;

	public ParseNode(int fromStreamIndex, RuleSpec rule, int alternativeIndex, int nextElementIndex, 
			boolean nextElementMatched) {
		this.fromStreamIndex = fromStreamIndex;
		this.rule = rule;
		this.alternativeIndex = alternativeIndex;
		this.nextElementIndex = nextElementIndex;
		this.nextElementMatched = nextElementMatched;
	}
	
	public int getFromStreamIndex() {
		return fromStreamIndex;
	}

	public RuleSpec getRule() {
		return rule;
	}

	public int getAlternativeIndex() {
		return alternativeIndex;
	}

	public int getNextElementIndex() {
		return nextElementIndex;
	}

	public boolean isNextElementMatched() {
		return nextElementMatched;
	}

	public List<ParseNode> getChildren() {
		return children;
	}
	
	public List<ElementSpec> getElements() {
		if (elements == null)
			elements = rule.getAlternatives().get(alternativeIndex).getElements();
		return elements;
	}
	
	public boolean isCompleted() {
		return nextElementIndex == elements.size();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ParseNode))
			return false;
		if (this == other)
			return true;
		ParseNode otherNode = (ParseNode) other;
		return new EqualsBuilder()
				.append(fromStreamIndex, otherNode.fromStreamIndex)
				.append(rule.getName(), otherNode.rule.getName())
				.append(alternativeIndex, otherNode.alternativeIndex)
				.append(nextElementIndex, otherNode.nextElementIndex)
				.append(nextElementMatched, otherNode.nextElementMatched)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(fromStreamIndex)
				.append(rule.getName())
				.append(alternativeIndex)
				.append(nextElementIndex)
				.append(nextElementMatched)
				.toHashCode();
	}
	
}
