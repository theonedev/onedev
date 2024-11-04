package io.onedev.server.web.editable;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class EditableResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public EditableResourceReference() {
		super(EditableResourceReference.class, "editable.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				EditableResourceReference.class, "editable.css")));
		return dependencies;
	}

}
