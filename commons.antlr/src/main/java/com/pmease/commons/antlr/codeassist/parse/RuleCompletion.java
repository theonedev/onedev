package com.pmease.commons.antlr.codeassist.parse;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RuleCompletion {
	
	private final String ruleName;
	
	private final int fromStreamIndex;
	
	private final int toStreamIndex;
	
	public RuleCompletion(String ruleName, int fromStreamIndex, int toStreamIndex) {
		this.ruleName = ruleName;
		this.fromStreamIndex = fromStreamIndex;
		this.toStreamIndex = toStreamIndex;
	}

	public String getRuleName() {
		return ruleName;
	}

	public int getFromStreamIndex() {
		return fromStreamIndex;
	}

	public int getToStreamIndex() {
		return toStreamIndex;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RuleCompletion))
			return false;
		if (this == other)
			return true;
		RuleCompletion otherSpan = (RuleCompletion) other;
		return new EqualsBuilder()
				.append(ruleName, otherSpan.ruleName)
				.append(fromStreamIndex, otherSpan.fromStreamIndex)
				.append(toStreamIndex, otherSpan.toStreamIndex)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(ruleName)
				.append(fromStreamIndex)
				.append(toStreamIndex)
				.toHashCode();
	}

}
