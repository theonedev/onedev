package com.gitplex.server.web.component.userchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class UserSingleChoice extends Select2Choice<UserFacade> {

	public UserSingleChoice(String id, IModel<UserFacade> model, AbstractUserChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a user ...");
		getSettings().setFormatResult("gitplex.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}
	
}
