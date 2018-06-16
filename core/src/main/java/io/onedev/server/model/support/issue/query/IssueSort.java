package io.onedev.server.model.support.issue.query;

import java.io.Serializable;

public class IssueSort implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum Direction {ASCENDING, DESCENDING};
	
	private String field;
	
	private IssueSort.Direction direction = Direction.ASCENDING;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public IssueSort.Direction getDirection() {
		return direction;
	}

	public void setDirection(IssueSort.Direction direction) {
		this.direction = direction;
	}

	@Override
	public String toString() {
		if (direction == Direction.ASCENDING)
			return IssueQuery.quote(field) + " asc";
		else
			return IssueQuery.quote(field) + " desc";
	}
	
}