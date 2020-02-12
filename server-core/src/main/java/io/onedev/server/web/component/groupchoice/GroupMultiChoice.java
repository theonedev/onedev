package io.onedev.server.web.component.groupchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Group;
import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class GroupMultiChoice extends Select2MultiChoice<Group> {

	public GroupMultiChoice(String id, IModel<Collection<Group>> selectionsModel, IModel<Collection<Group>> choicesModel) {
		super(id, selectionsModel, new GroupChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose groups...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.groupChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.groupChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.groupChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
	}

}
