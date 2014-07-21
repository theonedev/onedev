package com.pmease.gitplex.web.page.repository.source.commit.diff.patch;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HunkLine implements Serializable {

	public static enum LineType {
		CONTEXT, 
		OLD, 
		NEW
	}
	
	final String text;
	final LineType lineType;
	final int oldLineNo;
	final int newLineNo;
	
	boolean noNewLine = false;
	
	public static class Builder {
		String text;
		LineType lineType;
		int oldLineNo;
		int newLineNo;
		
		public Builder text(String text) {
			this.text = text;
			return this;
		}
		
		public Builder lineType(LineType lineType) {
			this.lineType = lineType;
			return this;
		}
		
		public Builder oldLineNo(int oldLineNo) {
			this.oldLineNo = oldLineNo;
			return this;
		}
		
		public Builder newLineNo(int newLineNo) {
			this.newLineNo = newLineNo;
			return this;
		}
		
		public HunkLine build() {
			return new HunkLine(text, lineType, oldLineNo, newLineNo);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public HunkLine(String text, LineType lineType, int oldLineNo, int newLineNo) {
		this.text = text;
		this.lineType = lineType;
		this.oldLineNo = oldLineNo;
		this.newLineNo = newLineNo;
	}

	public boolean isNoNewLine() {
		return noNewLine;
	}

	public void setNoNewLine(boolean noNewLine) {
		this.noNewLine = noNewLine;
	}

	public String getText() {
		return text;
	}

	public LineType getLineType() {
		return lineType;
	}

	public int getOldLineNo() {
		return oldLineNo;
	}

	public int getNewLineNo() {
		return newLineNo;
	}

	@Override
	public String toString() {
		switch (lineType) {

		case CONTEXT:
			return " " + text;

		case OLD:
			return "-" + text;

		case NEW:
			return "+" + text;

		default:
			throw new IllegalArgumentException();
		}
	}
}