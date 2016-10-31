package com.gitplex.web.component.accountchoice;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.web.page.base.BaseDependentResourceReference;

public class AccountChoiceResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public AccountChoiceResourceReference() {
		super(AccountChoiceResourceReference.class, "account-choice.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(AccountChoiceResourceReference.class, "account-choice.css")));
		return dependencies;
	}

}
