package io.onedev.server.plugin.report.jest;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class JestReportResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public JestReportResourceReference() {
		super(JestReportResourceReference.class, "jest-report.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				JestReportResourceReference.class, "jest-report.css")));
		return dependencies;
	}

}
