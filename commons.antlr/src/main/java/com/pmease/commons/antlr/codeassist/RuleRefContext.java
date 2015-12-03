package com.pmease.commons.antlr.codeassist;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class RuleRefContext {
	
	private final String ruleName;
	
	private final int alternativeIndex;
	
	private final int elementIndex;
	
	private final int streamIndex;

	public RuleRefContext(String ruleName, int alternativeIndex, int elementIndex, int streamIndex) {
		this.ruleName = ruleName;
		this.alternativeIndex = alternativeIndex;
		this.elementIndex = elementIndex;
		this.streamIndex = streamIndex;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RuleRefContext))
			return false;
		if (this == other)
			return true;
		RuleRefContext otherContext = (RuleRefContext) other;
		return new EqualsBuilder()
				.append(ruleName, otherContext.ruleName)
				.append(alternativeIndex, otherContext.alternativeIndex)
				.append(elementIndex, otherContext.elementIndex)
				.append(streamIndex, otherContext.streamIndex)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(ruleName)
				.append(alternativeIndex)
				.append(elementIndex)
				.append(streamIndex)
				.toHashCode();
	}

}
