package com.pmease.commons.wicket.component.floating;

import java.io.Serializable;

public class Alignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int targetX, targetY;
	
	private final int x, y; 
	
	private final int offset;
	
	public Alignment(int targetX, int targetY, int x, int y, int offset) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.x = x;
		this.y = y;
		this.offset = offset;
	}

	public int getTargetX() {
		return targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getOffset() {
		return offset;
	}

}
