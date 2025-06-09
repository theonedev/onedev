package io.onedev.server.web.component.chart.pie;

import java.io.Serializable;

public class PieSlice implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	private final String displayName;
	
	private final int value;
	
	private final String color;
	
	private final boolean selected;
	
	public PieSlice(String name, String displayName, int value, String color, boolean selected) {
		this.name = name;
		this.displayName = displayName;
		this.value = value;
		this.color = color;
		this.selected = selected;
	}

	public PieSlice(String name, int value, String color, boolean selected) {
		this(name, name, value, color, selected);
	}

	public String getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getValue() {
		return value;
	}

	public boolean isSelected() {
		return selected;
	}

}
