package io.onedev.server.web.component.job;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;

public class JobSingleChoice extends Select2Choice<String> {

	private static final long serialVersionUID = 1L;

	public JobSingleChoice(String id, IModel<String> model, IModel<List<String>> choicesModel, boolean tagsMode) {
		super(id, model, new StringChoiceProvider(choicesModel, tagsMode));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose job..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
