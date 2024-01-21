package io.onedev.server.web.component.chart.line;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

public class Line implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final Map<String, Integer> yAxisValues;

	private final String color;
	
	private final String stack;
	
	private final String style;
	
	public Line(String name, Map<String, Integer> yAxisValues, String color, @Nullable String stack, @Nullable String style) {
		this.name = name;
		this.yAxisValues = yAxisValues;
		this.color = color;
		this.stack = stack;
		this.style = style;
	}
	
	public String getName() {
		return name;
	}

	public Map<String, Integer> getYAxisValues() {
		return yAxisValues;
	}

	public String getColor() {
		return color;
	}

	@Nullable
	public String getStack() {
		return stack;
	}

	@Nullable
	public String getStyle() {
		return style;
	}
	
}
