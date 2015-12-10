package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.antlr.codeassist.AssistStream;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class ParseNode {
	
	private final int fromStreamIndex;
	
	private final RuleSpec rule;
	
	private final int alternativeIndex;
	
	private final int nextElementIndex;
	
	private final List<ParseNode> children = new ArrayList<>();
	
	private transient List<ElementSpec> elements;

	public ParseNode(int fromStreamIndex, RuleSpec rule, int alternativeIndex, int nextElementIndex) {
		this.fromStreamIndex = fromStreamIndex;
		this.rule = rule;
		this.alternativeIndex = alternativeIndex;
		this.nextElementIndex = nextElementIndex;
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

	public List<ParseNode> getChildren() {
		return children;
	}
	
	private List<ElementSpec> getElements() {
		if (elements == null)
			elements = rule.getAlternatives().get(alternativeIndex).getElements();
		return elements;
	}
	
	public Set<ParseNode> predict(AssistStream stream) {
		Set<ParseNode> predictions = new HashSet<>();
		if (Math.abs(nextElementIndex) < getElements().size()) {
			ElementSpec nextElement = getElements().get(Math.abs(nextElementIndex));
			if (nextElement instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
				int tokenType;
				if (!stream.isEof())
					tokenType = stream.getCurrentToken().getType();
				else
					tokenType = -1;
				RuleSpec elementRule = ruleRefElement.getRule();
				if (elementRule.getFirstTokenTypes().contains(tokenType)) {
					for (int i=0; i<elementRule.getAlternatives().size(); i++)
						predictions.add(new ParseNode(stream.getIndex(), elementRule, i, 0));
				}
			}
		}
		return predictions;
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
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(fromStreamIndex)
				.append(rule.getName())
				.append(alternativeIndex)
				.append(nextElementIndex)
				.toHashCode();
	}
	
}
