package com.pmease.commons.antlr.codeassist.parse;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CompletedRange {
	
	private final int fromStreamIndex;
	
	private final int toStreamIndex;
	
	public CompletedRange(int fromStreamIndex, int toStreamIndex) {
		this.fromStreamIndex = fromStreamIndex;
		this.toStreamIndex = toStreamIndex;
	}

	public int getFromStreamIndex() {
		return fromStreamIndex;
	}

	public int getToStreamIndex() {
		return toStreamIndex;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CompletedRange))
			return false;
		if (this == other)
			return true;
		CompletedRange otherRange = (CompletedRange) other;
		return new EqualsBuilder()
				.append(fromStreamIndex, otherRange.fromStreamIndex)
				.append(toStreamIndex, otherRange.toStreamIndex)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(fromStreamIndex)
				.append(toStreamIndex)
				.toHashCode();
	}

}
