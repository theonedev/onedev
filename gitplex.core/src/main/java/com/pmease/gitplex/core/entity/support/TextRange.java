package com.pmease.gitplex.core.entity.support;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.lang.extractors.TokenPosition;

public class TextRange implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int beginLine, beginChar, endLine, endChar;
	
	public TextRange() {
	}
	
	public TextRange(int beginLine, int beginChar, int endLine, int endChar) {
		this.beginLine = beginLine;
		this.beginChar = beginChar;
		this.endLine = endLine;
		this.endChar = endChar;
	}
	
	public TextRange(TextRange range) {
		beginLine = range.beginLine;
		beginChar = range.beginChar;
		endLine = range.endLine;
		endChar = range.endChar;
	}
	
	public TextRange(TokenPosition tokenPos) {
		this(tokenPos.getLine(), tokenPos.getRange().getFrom(), 
				tokenPos.getLine(), tokenPos.getRange().getTo());
	}

	public TextRange(String markStr) {
		String begin = StringUtils.substringBefore(markStr, "-");
		String end = StringUtils.substringAfter(markStr, "-");
		beginLine = Integer.parseInt(StringUtils.substringBefore(begin, "."))-1;
		beginChar = Integer.parseInt(StringUtils.substringAfter(begin, "."));
		endLine = Integer.parseInt(StringUtils.substringBefore(end, "."))-1;
		endChar = Integer.parseInt(StringUtils.substringAfter(end, "."));
	}
	
	public int getBeginLine() {
		return beginLine;
	}

	public int getBeginChar() {
		return beginChar;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndChar() {
		return endChar;
	}
	
	@Override
	public String toString() {
		return (beginLine+1) + "." + beginChar + "-" + (endLine+1) + "." + endChar;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TextRange))
			return false;
		if (this == other)
			return true;
		TextRange otherRange = (TextRange) other;
		return new EqualsBuilder()
				.append(beginLine, otherRange.beginLine)
				.append(beginChar, otherRange.beginChar)
				.append(endLine, otherRange.endLine)
				.append(endChar, otherRange.endChar)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(beginLine)
				.append(beginChar)
				.append(endLine)
				.append(endChar)
				.toHashCode();
	}
	
	public static @Nullable TextRange of(@Nullable TokenPosition tokenPos) {
		if (tokenPos != null)
			return new TextRange(tokenPos);
		else
			return null;
	}
	
	public static @Nullable TextRange of(@Nullable String str) {
		if (str != null)
			return new TextRange(str);
		else
			return null;
	}
	
}
