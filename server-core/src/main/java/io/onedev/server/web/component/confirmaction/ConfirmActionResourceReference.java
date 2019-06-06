package io.onedev.server.web.component.confirmaction;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ConfirmActionResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ConfirmActionResourceReference() {
		super(ConfirmActionResourceReference.class, "confirm-action.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ConfirmActionResourceReference.class, "confirm-action.css")));
		return dependencies;
	}

}
