package io.onedev.server.rest.resource.support;

import java.io.Serializable;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.rest.annotation.Api;

public class PlanarRangeData implements Serializable {

	private static final long serialVersionUID = 1L;

	@Api(description="Zero-based start row (inclusive). Must be non-negative", exampleProvider="getZeroExample")
	private int fromRow;

	@Api(description="Zero-based start column (inclusive), or -1 for the beginning of the start row", exampleProvider="getZeroExample")
	private int fromColumn;

	@Api(description="Zero-based end row (inclusive). Must be greater than or equal to fromRow", exampleProvider="getZeroExample")
	private int toRow;

	@Api(description="Zero-based end column (exclusive), or -1 for the end of the end row", exampleProvider="getToColumnExample")
	private int toColumn;

	@Api(description="Number of columns counted for each tab character. Must be positive; defaults to 1", exampleProvider="getTabWidthExample")
	private int tabWidth = 1;

	public int getFromRow() {
		return fromRow;
	}

	public void setFromRow(int fromRow) {
		this.fromRow = fromRow;
	}

	public int getFromColumn() {
		return fromColumn;
	}

	public void setFromColumn(int fromColumn) {
		this.fromColumn = fromColumn;
	}

	public int getToRow() {
		return toRow;
	}

	public void setToRow(int toRow) {
		this.toRow = toRow;
	}

	public int getToColumn() {
		return toColumn;
	}

	public void setToColumn(int toColumn) {
		this.toColumn = toColumn;
	}

	public int getTabWidth() {
		return tabWidth;
	}

	public void setTabWidth(int tabWidth) {
		this.tabWidth = tabWidth;
	}

	public PlanarRange toPlanarRange() {
		return new PlanarRange(fromRow, fromColumn, toRow, toColumn, tabWidth);
	}

	@SuppressWarnings("unused")
	private static int getZeroExample() {
		return 0;
	}

	@SuppressWarnings("unused")
	private static int getToColumnExample() {
		return 5;
	}

	@SuppressWarnings("unused")
	private static int getTabWidthExample() {
		return 1;
	}
}
