package com.gitplex.server.web.component.dropzonefield;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.gitplex.server.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

@SuppressWarnings("serial")
public class DropzoneFieldResourceReference extends BaseDependentResourceReference {

	public DropzoneFieldResourceReference() {
		super(DropzoneFieldResourceReference.class, "dropzone-field.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(DropzoneField.class, "dropzone-field.css")));
		return dependencies;
	}

}
