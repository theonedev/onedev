package com.pmease.commons.wicket.behavior.dropdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

public class AlignmentTarget {
	
	private final Component component;
	
	private final int x, y;

	public AlignmentTarget(@Nullable Component component, int x, int y) {
		this.component = component;
		this.x = x;
		this.y = y;
	}
	
	public Component getComponent() {
		return component;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
