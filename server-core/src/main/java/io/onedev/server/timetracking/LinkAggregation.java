package io.onedev.server.timetracking;

public class LinkAggregation {

	public enum Direction {SOURCE, TARGET, BOTH}
	
	private final String linkName;
	
	private final Direction direction;
	
	public LinkAggregation(String linkName, Direction direction) {
		this.linkName = linkName;
		this.direction = direction;
	}

	public String getLinkName() {
		return linkName;
	}

	public Direction getDirection() {
		return direction;
	}
}
