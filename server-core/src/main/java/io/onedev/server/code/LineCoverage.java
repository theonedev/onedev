package io.onedev.server.code;

import java.io.Serializable;

public class LineCoverage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int fromLine;
	
	private final int toLine;
	
	private final int testCount;
	
	public LineCoverage(int fromLine, int toLine, int testCount) {
		this.fromLine = fromLine;
		this.toLine = toLine;
		this.testCount = testCount;
	}

	public int getFromLine() {
		return fromLine;
	}

	public int getToLine() {
		return toLine;
	}

	public int getTestCount() {
		return testCount;
	}
	
}
