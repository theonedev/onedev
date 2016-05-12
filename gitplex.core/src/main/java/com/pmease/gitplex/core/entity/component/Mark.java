package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.gitplex.core.GitPlex;

public class Mark implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int beginLine, beginChar, endLine, endChar;
	
	public Mark() {
	}
	
	public Mark(int beginLine, int beginChar, int endLine, int endChar) {
		this.beginLine = beginLine;
		this.beginChar = beginChar;
		this.endLine = endLine;
		this.endChar = endChar;
	}
	
	public Mark(Mark mark) {
		this.beginLine = mark.beginLine;
		this.beginChar = mark.beginChar;
		this.endLine = mark.endLine;
		this.endChar = mark.endChar;
	}
	
	public Mark(TokenPosition tokenPos) {
		this(tokenPos.getLine(), tokenPos.getRange().getFrom(), 
				tokenPos.getLine(), tokenPos.getRange().getTo());
	}

	public Mark(String str) {
		String begin = StringUtils.substringBefore(str, "-");
		String end = StringUtils.substringAfter(str, "-");
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
		if (!(other instanceof Mark))
			return false;
		if (this == other)
			return true;
		Mark otherMark = (Mark) other;
		return new EqualsBuilder()
				.append(beginLine, otherMark.beginLine)
				.append(beginChar, otherMark.beginChar)
				.append(endLine, otherMark.endLine)
				.append(endChar, otherMark.endChar)
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
	
	public static @Nullable Mark of(@Nullable TokenPosition tokenPos) {
		if (tokenPos != null)
			return new Mark(tokenPos);
		else
			return null;
	}
	
	public String toJSON() {
		try {
			return GitPlex.getInstance(ObjectMapper.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} 
	}
	
}
