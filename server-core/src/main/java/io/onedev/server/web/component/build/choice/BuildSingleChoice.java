package io.onedev.server.web.component.build.choice;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.web.component.select2.Select2Choice;

public class BuildSingleChoice extends Select2Choice<Build> {

	public BuildSingleChoice(String id, IModel<Build> model, BuildChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose build..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.buildChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.buildChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.buildChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BuildChoiceResourceReference()));
	}
	
}
