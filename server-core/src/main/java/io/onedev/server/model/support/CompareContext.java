package io.onedev.server.model.support;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.util.diff.WhitespaceOption;

@Embeddable
public class CompareContext implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable=false)
	private String compareCommit;

	private boolean leftSide;
	
	private String pathFilter;
	
	@Column(nullable=false)
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;

	public String getCompareCommit() {
		return compareCommit;
	}

	public void setCompareCommit(String compareCommit) {
		this.compareCommit = compareCommit;
	}

	public boolean isLeftSide() {
		return leftSide;
	}

	public void setLeftSide(boolean leftSide) {
		this.leftSide = leftSide;
	}

	public String getPathFilter() {
		return pathFilter;
	}

	public void setPathFilter(String pathFilter) {
		this.pathFilter = pathFilter;
	}

	public WhitespaceOption getWhitespaceOption() {
		return whitespaceOption;
	}

	public void setWhitespaceOption(WhitespaceOption whitespaceOption) {
		this.whitespaceOption = whitespaceOption;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CompareContext))
			return false;
		if (this == other)
			return true;
		CompareContext otherContext = (CompareContext) other;
		return new EqualsBuilder()
				.append(compareCommit, otherContext.compareCommit)
				.append(leftSide, otherContext.leftSide)
				.append(pathFilter, otherContext.pathFilter)
				.append(whitespaceOption, otherContext.whitespaceOption)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(compareCommit)
				.append(leftSide)
				.append(pathFilter)
				.append(whitespaceOption)
				.toHashCode();
	}
	
}
