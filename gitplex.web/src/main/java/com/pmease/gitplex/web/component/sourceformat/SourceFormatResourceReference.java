package com.pmease.gitplex.web.component.sourceformat;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.pmease.gitplex.web.page.base.BaseDependentCssResourceReference;
import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class SourceFormatResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SourceFormatResourceReference() {
		super(SourceFormatResourceReference.class, "source-format.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(SourceFormatResourceReference.class, "source-format.css")));
		return dependencies;
	}

}
