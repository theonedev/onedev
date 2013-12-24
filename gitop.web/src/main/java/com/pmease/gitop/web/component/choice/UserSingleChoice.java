package com.pmease.gitop.web.component.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitop.model.User;

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
		getSettings().setFormatResult("UserChoice.formatter.formatResult");
		getSettings()
				.setFormatSelection("UserChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("UserChoice.formatter.escapeMarkup");
	}

	private ResourceReference userChoiceReference = 
			new JavaScriptResourceReference(UserSingleChoice.class, "userchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(userChoiceReference));
	}

}
