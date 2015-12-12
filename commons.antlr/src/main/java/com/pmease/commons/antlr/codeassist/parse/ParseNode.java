package com.pmease.commons.antlr.codeassist.parse;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.antlr.codeassist.AlternativeSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class ParseNode {
	
	private final int startTokenIndex;
	
	private final RuleSpec ruleSpec;
	
	private final int alternativeSpecIndex;
	
	private final int expectedElementSpecIndex;
	
	private final boolean expectedElementSpecMatchedOnce;
	
	private final List<ParsedElement> parsedElements;
	
	private transient AlternativeSpec alternativeSpec;
	
	private transient List<ElementSpec> elementSpecs;

	public ParseNode(int startTokenIndex, RuleSpec ruleSpec, int alternativeSpecIndex, int nextElementSpecIndex, 
			boolean nextElementSpecMatchedOnce, List<ParsedElement> parsedElements) {
		this.startTokenIndex = startTokenIndex;
		this.ruleSpec = ruleSpec;
		this.alternativeSpecIndex = alternativeSpecIndex;
		this.expectedElementSpecIndex = nextElementSpecIndex;
		this.expectedElementSpecMatchedOnce = nextElementSpecMatchedOnce;
		this.parsedElements = parsedElements;
	}
	
	public int getStartTokenIndex() {
		return startTokenIndex;
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

	public List<ParsedElement> getParsedElements() {
		return parsedElements;
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
		if (!(other instanceof ParseNode))
			return false;
		if (this == other)
			return true;
		ParseNode otherNode = (ParseNode) other;
		return new EqualsBuilder()
				.append(startTokenIndex, otherNode.startTokenIndex)
				.append(ruleSpec.getName(), otherNode.ruleSpec.getName())
				.append(alternativeSpecIndex, otherNode.alternativeSpecIndex)
				.append(expectedElementSpecIndex, otherNode.expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce, otherNode.expectedElementSpecMatchedOnce)
				.append(parsedElements, otherNode.parsedElements)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(startTokenIndex)
				.append(ruleSpec.getName())
				.append(alternativeSpecIndex)
				.append(expectedElementSpecIndex)
				.append(expectedElementSpecMatchedOnce)
				.append(parsedElements)
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
		
		return ruleSpec.getName() + " -> " + buffer.toString() + ": " + startTokenIndex;
	}
	
}
