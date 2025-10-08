package io.onedev.server.web.component.chart.line;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

public class Line implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<Integer> yAxisValues;

	private final String color;
	
	private final String stack;
	
	private final String style;
	
	private final boolean selected;

	public Line(String name, List<Integer> yAxisValues, String color, @Nullable String stack,
				@Nullable String style) {
		this(name, yAxisValues, color, stack, style, true);
	}
	
	public Line(String name, List<Integer> yAxisValues, String color, @Nullable String stack, 
				@Nullable String style, boolean selected) {
		this.name = name;
		this.yAxisValues = yAxisValues;
		this.color = color;
		this.stack = stack;
		this.style = style;
		this.selected = selected;
	}
	
	public String getName() {
		return name;
	}

	public List<Integer> getYAxisValues() {
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

	public boolean isSelected() {
		return selected;
	}
	
}
