package io.onedev.server.web.component.moreinfoside;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class MoreInfoSideResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MoreInfoSideResourceReference() {
		super(MoreInfoSideResourceReference.class, "more-info-side.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				MoreInfoSidePanel.class, "more-info-side.css")));
		return dependencies;
	}

}
