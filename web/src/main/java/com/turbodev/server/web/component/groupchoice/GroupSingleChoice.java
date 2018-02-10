package com.turbodev.server.web.component.groupchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.turbodev.server.util.facade.GroupFacade;
import com.turbodev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class GroupSingleChoice extends Select2Choice<GroupFacade> {

	public GroupSingleChoice(String id, IModel<GroupFacade> groupModel, 
			AbstractGroupChoiceProvider choiceProvider) {
		super(id, groupModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a group ...");
		getSettings().setFormatResult("turbodev.server.groupChoiceFormatter.formatResult");
		getSettings().setFormatSelection("turbodev.server.groupChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("turbodev.server.groupChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
	}
	
}