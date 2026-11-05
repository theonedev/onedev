package io.onedev.server.web.component.referencetooltip;

import java.io.Serial;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ReferenceTooltipResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ReferenceTooltipResourceReference() {
		super(ReferenceTooltipResourceReference.class, "reference-tooltip.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(ReferenceTooltipResourceReference.class, "reference-tooltip.css")));
		return dependencies;
	}

}
