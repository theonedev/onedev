package com.pmease.commons.wicket.component.floating;

import java.io.Serializable;

public class Alignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int targetX, targetY;
	
	private final int x, y; 
	
	private final int offset;
	
	private final boolean triangle;
	
	public Alignment(int targetX, int targetY, int x, int y, int offset, boolean triangle) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.x = x;
		this.y = y;
		this.offset = offset;
		this.triangle = triangle;
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

	public boolean isTriangle() {
		return triangle;
	}

}
