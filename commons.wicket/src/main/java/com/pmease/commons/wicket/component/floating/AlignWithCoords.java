package com.pmease.commons.wicket.component.floating;

public class AlignWithCoords implements AlignWith {

	private final int left, top, width, height;
	
	public AlignWithCoords(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toJSON() {
		return String.format("{left: %d, top: %d, width: %d, height: %d}", left, top, width, height);
	}

}
