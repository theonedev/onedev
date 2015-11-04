package com.pmease.commons.wicket.component.floating;

import java.io.Serializable;

public class Alignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int targetX, targetY;
	
	private final int x, y; 
	
	private final int offset;
	
	public Alignment(int targetX, int targetY, int x, int y) {
		this(targetX, targetY, x, y, 0);
	}

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

	public static Alignment bottom(int offset) {
		return new Alignment(0, 100, 0, 0, offset);
	}
	
	public static Alignment top(int offset) {
		return new Alignment(0, 0, 0, 100, offset);
	}
	
	public static Alignment left(int offset) {
		return new Alignment(0, 50, 100, 50, offset);
	}
	
	public static Alignment right(int offset) {
		return new Alignment(100, 50, 0, 50, offset);
	}
	
}
