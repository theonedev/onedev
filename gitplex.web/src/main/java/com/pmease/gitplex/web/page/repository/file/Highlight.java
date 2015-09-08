package com.pmease.gitplex.web.page.repository.file;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.lang.extractors.TokenPosition;

public class Highlight implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int fromLine, fromChar, toLine, toChar;
	
	public Highlight(int fromLine, int fromChar, int toLine, int toChar) {
		this.fromLine = fromLine;
		this.fromChar = fromChar;
		this.toLine = toLine;
		this.toChar = toChar;
	}
	
	public Highlight(TokenPosition tokenPos) {
		this(tokenPos.getLine(), tokenPos.getRange().getStart(), 
				tokenPos.getLine(), tokenPos.getRange().getEnd());
	}

	public Highlight(String str) {
		String from = StringUtils.substringBefore(str, "-");
		String to = StringUtils.substringAfter(str, "-");
		fromLine = Integer.parseInt(StringUtils.substringBefore(from, ","))-1;
		fromChar = Integer.parseInt(StringUtils.substringAfter(from, ","))-1;
		toLine = Integer.parseInt(StringUtils.substringBefore(to, ","))-1;
		toChar = Integer.parseInt(StringUtils.substringAfter(to, ","))-1;
	}
	
	public int getFromLine() {
		return fromLine;
	}

	public int getFromChar() {
		return fromChar;
	}

	public int getToLine() {
		return toLine;
	}

	public int getToChar() {
		return toChar;
	}

	@Override
	public String toString() {
		return (fromLine+1) + "," + (fromChar+1) + "-" + (toLine+1) + "," + (toChar+1);
	}
	
}
