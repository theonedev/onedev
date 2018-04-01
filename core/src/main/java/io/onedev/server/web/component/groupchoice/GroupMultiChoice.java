package io.onedev.server.web.component.groupchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class GroupMultiChoice extends Select2MultiChoice<GroupFacade> {

	public GroupMultiChoice(String id, IModel<Collection<GroupFacade>> groupsModel, 
			AbstractGroupChoiceProvider choiceProvider) {
		super(id, groupsModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose groups ...");
		getSettings().setFormatResult("onedev.server.groupChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.groupChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.groupChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
	}

}
