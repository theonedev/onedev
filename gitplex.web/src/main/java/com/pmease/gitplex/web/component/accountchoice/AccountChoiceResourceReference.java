package com.pmease.gitplex.web.component.accountchoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AccountChoiceResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AccountChoiceResourceReference INSTANCE = new AccountChoiceResourceReference();
	
	private AccountChoiceResourceReference() {
		super(AccountChoiceResourceReference.class, "account-choice.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(AccountChoiceResourceReference.class, "account-choice.css")));
		return dependencies;
	}

}
