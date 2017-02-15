package com.gitplex.server.web.component.select2;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.mousewheel.MouseWheelResourceReference;
import com.gitplex.server.web.page.CommonDependentResourceReference;

public class Select2ResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public Select2ResourceReference() {
		super(Select2ResourceReference.class, "res/select2.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new MouseWheelResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new Select2CssResourceReference()));
		return dependencies;
	}

}
