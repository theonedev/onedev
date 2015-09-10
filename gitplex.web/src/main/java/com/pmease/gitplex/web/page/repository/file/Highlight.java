package com.pmease.gitplex.web.page.repository.file;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.pmease.commons.lang.extractors.TokenPosition;

public class Highlight implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int beginLine, beginChar, endLine, endChar;
	
	public Highlight(int beginLine, int beginChar, int endLine, int endChar) {
		this.beginLine = beginLine;
		this.beginChar = beginChar;
		this.endLine = endLine;
		this.endChar = endChar;
	}
	
	public Highlight(Highlight highlight) {
		this.beginLine = highlight.beginLine;
		this.beginChar = highlight.beginChar;
		this.endLine = highlight.endLine;
		this.endChar = highlight.endChar;
	}
	
	public Highlight(TokenPosition tokenPos) {
		this(tokenPos.getLine(), tokenPos.getRange().getStart(), 
				tokenPos.getLine(), tokenPos.getRange().getEnd());
	}

	public Highlight(String str) {
		String begin = StringUtils.substringBefore(str, "-");
		String end = StringUtils.substringAfter(str, "-");
		beginLine = Integer.parseInt(StringUtils.substringBefore(begin, ","))-1;
		beginChar = Integer.parseInt(StringUtils.substringAfter(begin, ","))-1;
		endLine = Integer.parseInt(StringUtils.substringBefore(end, ","))-1;
		endChar = Integer.parseInt(StringUtils.substringAfter(end, ","))-1;
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
		return (beginLine+1) + "," + (beginChar+1) + "-" + (endLine+1) + "," + (endChar+1);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Highlight))
			return false;
		if (this == other)
			return true;
		Highlight otherHighlight = (Highlight) other;
		return new EqualsBuilder()
				.append(beginLine, otherHighlight.beginLine)
				.append(beginChar, otherHighlight.beginChar)
				.append(endLine, otherHighlight.endLine)
				.append(endChar, otherHighlight.endChar)
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
	
	public static @Nullable Highlight of(@Nullable TokenPosition tokenPos) {
		if (tokenPos != null)
			return new Highlight(tokenPos);
		else
			return null;
	}
}
