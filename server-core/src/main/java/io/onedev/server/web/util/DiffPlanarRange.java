package io.onedev.server.web.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.commons.utils.PlanarRange;

public class DiffPlanarRange extends PlanarRange {

	private static final long serialVersionUID = 1L;

	public final boolean leftSide;
	
	public DiffPlanarRange(boolean leftSide, int fromRow, int fromColumn, int toRow, int toColumn) {
		super(fromRow, fromColumn, toRow, toColumn);
		this.leftSide = leftSide;
	}
	
	public DiffPlanarRange(boolean leftSide, PlanarRange range) {
		this(leftSide, range.getFromRow(), range.getFromColumn(), range.getToRow(), range.getToColumn());
	}

	public boolean isLeftSide() {
		return leftSide;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DiffPlanarRange))
			return false;
		if (this == other)
			return true;
		DiffPlanarRange otherRange = (DiffPlanarRange) other;
		return new EqualsBuilder()
				.append(getFromRow(), otherRange.getFromRow())
				.append(getFromColumn(), otherRange.getFromColumn())
				.append(getToRow(), otherRange.getToRow())
				.append(getToColumn(), otherRange.getToColumn())
				.append(isLeftSide(), otherRange.isLeftSide())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getFromRow())
				.append(getFromColumn())
				.append(getToRow())
				.append(getToColumn())
				.append(isLeftSide())
				.toHashCode();
	}
	
}
