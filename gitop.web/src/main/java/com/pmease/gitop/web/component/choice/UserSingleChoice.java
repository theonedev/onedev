package com.pmease.gitop.web.component.choice;

import org.apache.wicket.model.IModel;

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
		getSettings().setFormatResult("gitop.choiceFormatter.user.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.user.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.user.escapeMarkup");
	}

}
