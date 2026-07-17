package io.onedev.server.web.page.project.stats.code;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.asset.jsjoda.JsJodaResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CodeContribsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeContribsResourceReference() {
		super(CodeContribsResourceReference.class, "code-contribs.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JsJodaResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CodeContribsResourceReference.class, "code-contribs.css")));
		return dependencies;
	}
	
}
