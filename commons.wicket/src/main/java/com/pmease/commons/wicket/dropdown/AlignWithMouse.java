package com.pmease.commons.wicket.dropdown;

import com.pmease.commons.wicket.component.floating.AlignWithCoords;

public class AlignWithMouse implements AlignWith {
	
	private static final int MOUSE_WIDTH = 8;
	
	private static final int MOUSE_HEIGHT = 12;
	
	public AlignWithCoords asCoords(int mouseX, int mouseY) {
		return new AlignWithCoords(mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);
	}
}
