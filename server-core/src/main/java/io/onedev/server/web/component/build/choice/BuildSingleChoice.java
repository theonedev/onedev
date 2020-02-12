package io.onedev.server.web.component.build.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class BuildSingleChoice extends Select2Choice<Build> {

	public BuildSingleChoice(String id, IModel<Build> model, BuildChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose build...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.buildChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.buildChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.buildChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BuildChoiceResourceReference()));
	}
	
}
