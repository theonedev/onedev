package io.onedev.server.web.component.chart.line;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class LineChartResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public LineChartResourceReference() {
		super(LineChartResourceReference.class, "line-chart.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				LineChartResourceReference.class, "line-chart.css")));
		return dependencies;
	}

}
