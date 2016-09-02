package com.pmease.gitplex.web.component.branchchoice;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class BranchChoiceResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BranchChoiceResourceReference() {
		super(BranchChoiceResourceReference.class, "branch-choice.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(BranchChoiceResourceReference.class, "branch-choice.css")));
		return dependencies;
	}
	
}
