package com.pmease.commons.wicket.dropdown;

import com.pmease.commons.wicket.component.floating.AlignFloatingWithCoords;

public class AlignDropdownWithMouse implements AlignDropdownWith {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MOUSE_WIDTH = 8;
	
	private static final int MOUSE_HEIGHT = 12;
	
	public AlignFloatingWithCoords asCoords(int mouseX, int mouseY) {
		return new AlignFloatingWithCoords(mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);
	}
}
