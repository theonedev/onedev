package io.onedev.server.web.component.chart.line;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class LineSeries implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String seriesName;
	
	private final List<String> lineNames;
	
	private final Map<String, List<Integer>> lineValues;
	
	private final String valueFormatter;
	
	private final Integer minValue;
	
	private final Integer maxValue;
	
	private final List<String> lineColors;
	
	public LineSeries(@Nullable String seriesName, List<String> lineNames, 
			Map<String, List<Integer>> lineValues, @Nullable String valueFormatter, 
			@Nullable Integer minValue, @Nullable Integer maxValue, 
			List<String> lineColors) {
		this.seriesName = seriesName;
		this.lineNames = lineNames;
		this.lineValues = lineValues;
		this.valueFormatter = valueFormatter;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.lineColors = lineColors;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public List<String> getLineNames() {
		return lineNames;
	}

	public Map<String, List<Integer>> getLineValues() {
		return lineValues;
	}

	@Nullable
	public String getValueFormatter() {
		return valueFormatter;
	}

	@Nullable
	public Integer getMinValue() {
		return minValue;
	}

	@Nullable
	public Integer getMaxValue() {
		return maxValue;
	}

	public List<String> getLineColors() {
		return lineColors;
	}

}
