package io.onedev.server.web.component.stringchoice;

import java.util.Map;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class StringSingleChoice extends Select2Choice<String> {

	public StringSingleChoice(String id, IModel<String> selectionModel, 
			IModel<Map<String, String>> choicesModel, boolean tagsMode) {
		super(id, selectionModel, new StringChoiceProvider(choicesModel, tagsMode));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setAllowClear(!isRequired());
		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
