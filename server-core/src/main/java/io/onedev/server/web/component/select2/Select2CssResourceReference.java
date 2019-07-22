package io.onedev.server.web.component.select2;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class Select2CssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public Select2CssResourceReference() {
		super(Select2CssResourceReference.class, "res/select2-bootstrap.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				Select2CssResourceReference.class, "res/select2.css")));
		return dependencies;
	}

}
