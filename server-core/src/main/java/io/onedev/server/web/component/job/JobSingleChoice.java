package io.onedev.server.web.component.job;

import java.util.Map;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;

public class JobSingleChoice extends Select2Choice<String> {

	private static final long serialVersionUID = 1L;

	public JobSingleChoice(String id, IModel<String> model, IModel<Map<String, String>> choicesModel) {
		super(id, model, new StringChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose job...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
