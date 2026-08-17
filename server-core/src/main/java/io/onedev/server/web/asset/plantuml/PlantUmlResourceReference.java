package io.onedev.server.web.asset.plantuml;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;
import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class PlantUmlResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PlantUmlResourceReference() {
		super(PlantUmlResourceReference.class, "plantuml-init.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				PlantUmlResourceReference.class, "viz-global.js")));
		return dependencies;
	}

}
