package com.pmease.commons.antlr.codeassist.parse;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RuleCompletion {
	
	private final String ruleName;
	
	private final int beginTokenIndex;
	
	private final int endTokenIndex;
	
	public RuleCompletion(String ruleName, int beginTokenIndex, int endTokenIndex) {
		this.ruleName = ruleName;
		this.beginTokenIndex = beginTokenIndex;
		this.endTokenIndex = endTokenIndex;
	}

	public String getRuleName() {
		return ruleName;
	}

	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}

	public int getEndTokenIndex() {
		return endTokenIndex;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RuleCompletion))
			return false;
		if (this == other)
			return true;
		RuleCompletion otherCompletion = (RuleCompletion) other;
		return new EqualsBuilder()
				.append(ruleName, otherCompletion.ruleName)
				.append(beginTokenIndex, otherCompletion.beginTokenIndex)
				.append(endTokenIndex, otherCompletion.endTokenIndex)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(ruleName)
				.append(beginTokenIndex)
				.append(endTokenIndex)
				.toHashCode();
	}

}
