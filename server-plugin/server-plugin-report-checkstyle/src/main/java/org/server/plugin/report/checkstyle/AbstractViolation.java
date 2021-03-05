package org.server.plugin.report.checkstyle;

import java.io.Serializable;

import io.onedev.commons.utils.PlanarRange;

public abstract class AbstractViolation implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int TAB_WIDTH = 8;
	
	private final String message;
	
	private final String line;
	
	private final String column;
	
	public AbstractViolation(String message, String line, String column) {
		this.message = message;
		this.line = line;
		this.column = column;
	}

	public String getMessage() {
		return message;
	}

	public String getLine() {
		return line;
	}

	public String getColumn() {
		return column;
	}
	
	public PlanarRange getRange() {
		int lineNo = Integer.parseInt(line)-1;
		if (column != null) {
			int columnNo = Integer.parseInt(column)-1;
			return new PlanarRange(lineNo, columnNo, -1, -1, TAB_WIDTH);
		} else {
			return new PlanarRange(lineNo, -1, -1, -1, TAB_WIDTH);
		}
	}

	public String describePosition() {
		String position = "line:" + line;
		if (column != null)
			position += ", column:" + column;
		return position;
	}
	
}
