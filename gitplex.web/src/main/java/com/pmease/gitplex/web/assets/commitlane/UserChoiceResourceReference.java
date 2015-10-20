package com.pmease.gitplex.web.assets.commitlane;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class UserChoiceResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final UserChoiceResourceReference INSTANCE = new UserChoiceResourceReference();
	
	private UserChoiceResourceReference() {
		super(UserChoiceResourceReference.class, "user-choice.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(UserChoiceResourceReference.class, "user-choice.css")));
		return dependencies;
	}

}
