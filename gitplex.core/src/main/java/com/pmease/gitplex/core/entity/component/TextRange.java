package com.pmease.gitplex.core.entity.component;

public class TextRange {

	private Integer beginLine;
	
	private Integer beginChar;
	
	private Integer endLine;
	
	private Integer endChar;

	/**
	 * @return the beginLine
	 */
	public Integer getBeginLine() {
		return beginLine;
	}

	/**
	 * @param beginLine the beginLine to set
	 */
	public void setBeginLine(Integer beginLine) {
		this.beginLine = beginLine;
	}

	/**
	 * @return the beginChar
	 */
	public Integer getBeginChar() {
		return beginChar;
	}

	/**
	 * @param beginChar the beginChar to set
	 */
	public void setBeginChar(Integer beginChar) {
		this.beginChar = beginChar;
	}

	/**
	 * @return the endLine
	 */
	public Integer getEndLine() {
		return endLine;
	}

	/**
	 * @param endLine the endLine to set
	 */
	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}

	/**
	 * @return the endChar
	 */
	public Integer getEndChar() {
		return endChar;
	}

	/**
	 * @param endChar the endChar to set
	 */
	public void setEndChar(Integer endChar) {
		this.endChar = endChar;
	}
	
}
