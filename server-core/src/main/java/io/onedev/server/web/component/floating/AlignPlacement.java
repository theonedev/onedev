package io.onedev.server.web.component.floating;

import java.io.Serializable;

public class AlignPlacement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final int targetX, targetY;
	
	private final int x, y; 
	
	private final int offset;
	
	public AlignPlacement(int targetX, int targetY, int x, int y) {
		this(targetX, targetY, x, y, 0);
	}

	public AlignPlacement(int targetX, int targetY, int x, int y, int offset) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.x = x;
		this.y = y;
		this.offset = offset;
	}

	public static AlignPlacement bottom(int offset) {
		return new AlignPlacement(0, 100, 0, 0, offset);
	}
	
	public static AlignPlacement top(int offset) {
		return new AlignPlacement(0, 0, 0, 100, offset);
	}
	
	public static AlignPlacement left(int offset) {
		return new AlignPlacement(0, 50, 100, 50, offset);
	}
	
	public static AlignPlacement right(int offset) {
		return new AlignPlacement(100, 50, 0, 50, offset);
	}
	
	@Override
	public String toString() {
		return String.format("{x:%d, y:%d, targetX:%d, targetY:%d, offset:%d}", x, y, targetX, targetY, offset);
	}
	
}