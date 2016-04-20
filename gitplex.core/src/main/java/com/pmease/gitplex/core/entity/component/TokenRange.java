package com.pmease.gitplex.core.entity.component;

public class TokenRange {

	private Integer beginLine;
	
	private Integer beginToken;
	
	private Integer endLine;
	
	private Integer endToken;

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
	 * @return the beginToken
	 */
	public Integer getBeginToken() {
		return beginToken;
	}

	/**
	 * @param beginToken the beginToken to set
	 */
	public void setBeginToken(Integer beginToken) {
		this.beginToken = beginToken;
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
	 * @return the endToken
	 */
	public Integer getEndToken() {
		return endToken;
	}

	/**
	 * @param endToken the endToken to set
	 */
	public void setEndToken(Integer endToken) {
		this.endToken = endToken;
	}
	
}
