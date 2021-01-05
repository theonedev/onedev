package io.onedev.server.web.component.chart.line;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

public class LineSeries implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final Map<String, Integer> values;
	
	private final String valueFormatter;
	
	private final Integer minValue;
	
	private final Integer maxValue;
	
	public LineSeries(@Nullable String name, Map<String, Integer> values, String valueFormatter, 
			@Nullable Integer minValue, @Nullable Integer maxValue) {
		this.name = name;
		this.values = values;
		this.valueFormatter = valueFormatter;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public Map<String, Integer> getValues() {
		return values;
	}

	public String getValueFormatter() {
		return valueFormatter;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public Integer getMinValue() {
		return minValue;
	}

	@Nullable
	public Integer getMaxValue() {
		return maxValue;
	}

}
