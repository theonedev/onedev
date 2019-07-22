package io.onedev.server.web.component.select2;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.mousewheel.MouseWheelResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class Select2ResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public Select2ResourceReference() {
		super(Select2ResourceReference.class, "res/select2.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new MouseWheelResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new Select2CssResourceReference()));
		return dependencies;
	}

}
