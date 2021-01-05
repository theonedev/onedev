package io.onedev.server.web.component.chart.pie;

import java.io.Serializable;

public class PieSlice implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final int value;
	
	private final String color;
	
	private final boolean selected;
	
	public PieSlice(String name, int value, String color, boolean selected) {
		this.name = name;
		this.value = value;
		this.color = color;
		this.selected = selected;
	}

	public String getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public boolean isSelected() {
		return selected;
	}

}
