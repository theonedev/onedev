package com.gitplex.web.component.depotfile.editsave;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.web.page.base.BaseDependentResourceReference;

public class EditSaveResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public EditSaveResourceReference() {
		super(EditSaveResourceReference.class, "edit-save.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(EditSaveResourceReference.class, "edit-save.css")));
		return dependencies;
	}

}
