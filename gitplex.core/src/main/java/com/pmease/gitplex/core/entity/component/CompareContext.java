package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.OptimisticLock;

import com.pmease.commons.lang.diff.WhitespaceOption;

@Embeddable
public class CompareContext implements Serializable {

	private static final long serialVersionUID = 1L;

	@OptimisticLock(excluded=true)
	private String compareCommit;

	@OptimisticLock(excluded=true)
	private Boolean leftSide;
	
	@OptimisticLock(excluded=true)
	private String pathFilter;
	
	@OptimisticLock(excluded=true)
	private WhitespaceOption whitespaceOption;

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
	
}
