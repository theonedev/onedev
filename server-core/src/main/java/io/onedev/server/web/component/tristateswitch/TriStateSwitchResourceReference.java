package io.onedev.server.web.component.tristateswitch;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class TriStateSwitchResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TriStateSwitchResourceReference() {
		super(TriStateSwitchResourceReference.class, "tri-state-switch.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TriStateSwitchResourceReference.class, "tri-state-switch.css")));
		return dependencies;
	}
}
