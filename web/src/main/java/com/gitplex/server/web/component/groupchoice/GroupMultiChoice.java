package com.gitplex.server.web.component.groupchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Group;
import com.gitplex.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class GroupMultiChoice extends Select2MultiChoice<Group> {

	public GroupMultiChoice(String id, IModel<Collection<Group>> groupsModel, 
			AbstractGroupChoiceProvider choiceProvider) {
		super(id, groupsModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose groups ...");
		getSettings().setFormatResult("gitplex.server.groupChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.groupChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.groupChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
	}

}
