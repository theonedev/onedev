package io.onedev.server.web.component.floating;

public class RectTarget implements AlignTarget {

	private static final long serialVersionUID = 1L;
	
	private static final int CURSOR_WIDTH = 6;
	
	private static final int CURSOR_HEIGHT = 8;
	
	private final int left, top, width, height;
	
	public RectTarget(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	public static RectTarget ofMouse(int mouseX, int mouseY) {
		return new RectTarget(mouseX, mouseY, CURSOR_WIDTH, CURSOR_HEIGHT);
	}
	
	@Override
	public String toString() {
		return String.format("{left: %d, top: %d, width: %d, height: %d}", left, top, width, height);
	}

}