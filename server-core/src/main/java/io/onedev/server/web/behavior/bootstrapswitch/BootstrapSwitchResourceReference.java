package io.onedev.server.web.behavior.bootstrapswitch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BootstrapSwitchResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BootstrapSwitchResourceReference() {
		super(BootstrapSwitchResourceReference.class, "bootstrap-switch-integration.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				BootstrapSwitchResourceReference.class, "bootstrap-switch.min.js")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BootstrapSwitchResourceReference.class, "bootstrap-switch.min.css")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BootstrapSwitchResourceReference.class, "bootstrap-switch-custom.css")));
		return dependencies;
	}

}
