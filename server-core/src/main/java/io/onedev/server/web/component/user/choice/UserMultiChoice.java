package io.onedev.server.web.component.user.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.User;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class UserMultiChoice extends Select2MultiChoice<User> {

	private static final long serialVersionUID = 1L;

	public UserMultiChoice(String id, IModel<Collection<User>> selectionsModel, IModel<Collection<User>>choicesModel) {
		super(id, selectionsModel, new UserChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose users...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.userChoiceFormatter.escapeMarkup");
        setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

}
