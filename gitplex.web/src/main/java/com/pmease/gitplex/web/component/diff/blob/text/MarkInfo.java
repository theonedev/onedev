package com.pmease.gitplex.web.component.diff.blob.text;

import java.io.Serializable;

public class MarkInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final boolean old;
	
	private final int beginLine;
	
	private final int endLine;

	public MarkInfo(boolean old, int beginLine, int endLine) {
		this.old = old;
		this.beginLine = beginLine;
		this.endLine = endLine;
	}
	
	/**
	 * @return the old
	 */
	public boolean isOld() {
		return old;
	}

	/**
	 * @return the beginLine
	 */
	public int getBeginLine() {
		return beginLine;
	}

	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}
	
}
