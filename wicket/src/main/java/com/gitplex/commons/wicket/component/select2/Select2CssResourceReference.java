package com.gitplex.commons.wicket.component.select2;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.commons.wicket.page.CommonDependentCssResourceReference;

public class Select2CssResourceReference extends CommonDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public Select2CssResourceReference() {
		super(Select2CssResourceReference.class, "res/select2-bootstrap.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(Select2CssResourceReference.class, "res/select2.css")));
		return dependencies;
	}

}
