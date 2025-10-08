package io.onedev.server.web.component.chart.line;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

public class LineSeries implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PERCENTAGE_FORMATTER = "function(value) {return value+'%';}";

	public static final String SECONDS_FORMATTER = "function(seconds) {return onedev.server.formatBriefDuration(seconds);}";

	public static final String MINUTES_FORMATTER = "function(minutes) {return onedev.server.formatBriefDuration(minutes*60);}";
	
	private final String seriesName;
	
	private final List<String> xAxisValues;
	
	private final List<Line> lines;
	
	private final String yAxisValueFormatter;
	
	private final Integer minYAxisValue;
	
	private final Integer maxYAxisValue;
	
	public LineSeries(@Nullable String seriesName, List<String> xAxisValues, List<Line> lines, 
			@Nullable String yAxisValueFormatter, @Nullable Integer minYAxisValue, 
			@Nullable Integer maxYAxisValue) {
		this.seriesName = seriesName;
		this.xAxisValues = xAxisValues;
		this.lines = lines;
		this.yAxisValueFormatter = yAxisValueFormatter;
		this.minYAxisValue = minYAxisValue;
		this.maxYAxisValue = maxYAxisValue;
	}

	@Nullable
	public String getSeriesName() {
		return seriesName;
	}

	public List<String> getXAxisValues() {
		return xAxisValues;
	}

	public List<Line> getLines() {
		return lines;
	}

	@Nullable
	public String getYAxisValueFormatter() {
		return yAxisValueFormatter;
	}

	@Nullable
	public Integer getMinYAxisValue() {
		return minYAxisValue;
	}

	@Nullable
	public Integer getMaxYAxisValue() {
		return maxYAxisValue;
	}

}
