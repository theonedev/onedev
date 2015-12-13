package com.pmease.commons.antlr.codeassist.parse;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.antlr.codeassist.AlternativeSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class Node {
	
	private final int beginTokenIndex;
	
	private final RuleSpec ruleSpec;
	
	private final int alternativeSpecIndex;
	
	private final int expectedElementSpecIndex;
	
	private final boolean expectedElementSpecMatchedOnce;
	
	private final List<Element> elements;
	
	private transient AlternativeSpec alternativeSpec;
	
	private transient List<ElementSpec> elementSpecs;

	public Node(int beginTokenIndex, RuleSpec ruleSpec, int alternativeSpecIndex, int nextElementSpecIndex, 
			boolean nextElementSpecMatchedOnce, List<Element> elements) {
		this.beginTokenIndex = beginTokenIndex;
		this.ruleSpec = ruleSpec;
		this.alternativeSpecIndex = alternativeSpecIndex;
		this.expectedElementSpecIndex = nextElementSpecIndex;
		this.expectedElementSpecMatchedOnce = nextElementSpecMatchedOnce;
		this.elements = elements;
	}
	
	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}

	public RuleSpec getRuleSpec() {
		return ruleSpec;
	}

	public int getAlternativeSpecIndex() {
		return alternativeSpecIndex;
	}

	public int getExpectedElementSpecIndex() {
		return expectedElementSpecIndex;
	}

	public boolean isExpectedElementSpecMatchedOnce() {
		return expectedElementSpecMatchedOnce;
	}
	
	@Nullable
	public ElementSpec getExpectedElementSpec() {
		if (isCompleted())
			return null;
		else
			return getElementSpecs().get(expectedElementSpecIndex);
	}

	public List<Element> getElements() {
		return elements;
	}
	
	public AlternativeSpec getAlternativeSpec() {
		if (alternativeSpec == null)
			alternativeSpec = ruleSpec.getAlternatives().get(alternativeSpecIndex);
		return alternativeSpec;
	}
	
	public List<ElementSpec> getElementSpecs() {
		if (elementSpecs == null)
			elementSpecs = getAlternativeSpec().getElements();
		return elementSpecs;
	}
	
	public boolean isCompleted() {
		return expectedElementSpecIndex == getElementSpecs().size();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Node))
			return false;
		if (this == other)
			return true;
		Node otherNode = (Node) other;
		return new EqualsBuilder()
				.append(beginTokenIndex, otherNode.beginTokenIndex)
				.append(ruleSpec.getName(), otherNode.ruleSpec.getName())
				.append(alternativeSpecIndex, otherNode.alternativeSpecIndex)
				.append(expectedElementSpecIndex, otherNode.expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce, otherNode.expectedElementSpecMatchedOnce)
				.append(elements, otherNode.elements)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(beginTokenIndex)
				.append(ruleSpec.getName())
				.append(alternativeSpecIndex)
				.append(expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce)
				.append(elements)
				.toHashCode();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<expectedElementSpecIndex; i++)
			buffer.append(getElementSpecs().get(i)).append(" ");
		buffer.append(expectedElementSpecMatchedOnce?"~ ":"^ ");
		for (int i=expectedElementSpecIndex; i<getElementSpecs().size(); i++)
			buffer.append(getElementSpecs().get(i)).append(" ");
		
		return ruleSpec.getName() + " -> " + buffer.toString() + ": " + beginTokenIndex;
	}
	
}
