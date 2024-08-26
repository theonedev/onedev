package io.onedev.server.web.page.project.stats.code;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.jsjoda.JsJodaResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import java.util.List;

public class CodeStatsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeStatsResourceReference() {
		super(CodeStatsResourceReference.class, "code-stats.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JsJodaResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CodeStatsResourceReference.class, "code-stats.css")));
		return dependencies;
	}
	
}
