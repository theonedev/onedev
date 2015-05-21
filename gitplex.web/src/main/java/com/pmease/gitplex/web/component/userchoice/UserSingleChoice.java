package com.pmease.gitplex.web.component.userchoice;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class UserSingleChoice extends Select2Choice<User> {

	public UserSingleChoice(String id, IModel<User> model) {
		super(id, model, new UserChoiceProvider());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Typing to find an user ...");
		getSettings().setFormatResult("gitplex.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(UserChoiceAsset.class, "user-choice.js")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(UserChoiceAsset.class, "user-choice.css")));
	}
	
}
