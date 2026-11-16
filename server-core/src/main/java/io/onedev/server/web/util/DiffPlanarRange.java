package io.onedev.server.web.util;

import java.io.Serial;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.PlanarRange;

public class DiffPlanarRange extends PlanarRange {

	@Serial
	private static final long serialVersionUID = 1L;

	public final boolean leftSide;
	
	public DiffPlanarRange(boolean leftSide, int fromRow, int fromColumn, int toRow, int toColumn) {
		super(fromRow, fromColumn, toRow, toColumn);
		this.leftSide = leftSide;
	}
	
	public DiffPlanarRange(boolean leftSide, PlanarRange range) {
		super(range);
		this.leftSide = leftSide;
	}

	public boolean isLeftSide() {
		return leftSide;
	}

	@Override
	public String toString() {
		return (leftSide ? "left~" : "right~") + super.toString();
	}

	@Nullable
	public static DiffPlanarRange of(@Nullable String value) {
		if (value == null)
			return null;
		int separator = value.indexOf('~');
		if (separator == -1)
			return null;
		Boolean leftSide = switch (value.substring(0, separator)) {
			case "left" -> true;
			case "right" -> false;
			default -> null;
		};
		return leftSide != null ? new DiffPlanarRange(leftSide, new PlanarRange(value.substring(separator + 1))) : null;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DiffPlanarRange otherRange))
			return false;
		if (this == other)
			return true;
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
