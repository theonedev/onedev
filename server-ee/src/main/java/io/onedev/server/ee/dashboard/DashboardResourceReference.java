package io.onedev.server.ee.dashboard;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;
import io.onedev.server.web.asset.snapsvg.SnapSvgResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class DashboardResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DashboardResourceReference() {
		super(DashboardResourceReference.class, "dashboard.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new SnapSvgResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				DashboardResourceReference.class, "dashboard.css")));
		return dependencies;
	}

}
