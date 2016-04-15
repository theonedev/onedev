package com.pmease.gitplex.web.component.branchchoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BranchChoiceResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final BranchChoiceResourceReference INSTANCE = new BranchChoiceResourceReference();
	
	private BranchChoiceResourceReference() {
		super(BranchChoiceResourceReference.class, "branch-choice.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(BranchChoiceResourceReference.class, "branch-choice.css")));
		return dependencies;
	}
	
}
