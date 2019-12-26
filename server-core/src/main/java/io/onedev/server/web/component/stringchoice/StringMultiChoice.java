package io.onedev.server.web.component.stringchoice;

import java.util.Collection;
import java.util.Map;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class StringMultiChoice extends Select2MultiChoice<String> {

	public StringMultiChoice(String id, IModel<Collection<String>> selectionsModel, IModel<Map<String, String>> choicesModel) {
		super(id, selectionsModel, new StringChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
