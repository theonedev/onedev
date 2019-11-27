package io.onedev.server.web.component.modal.confirm;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ConfirmResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ConfirmResourceReference() {
		super(ConfirmResourceReference.class, "confirm.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ConfirmResourceReference.class, "confirm.css")));
		return dependencies;
	}

}
