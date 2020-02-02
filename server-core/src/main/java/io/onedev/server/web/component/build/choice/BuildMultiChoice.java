package io.onedev.server.web.component.build.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class BuildMultiChoice extends Select2MultiChoice<Build> {

	public BuildMultiChoice(String id, IModel<Collection<Build>> model, BuildChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose builds...");
		else
			getSettings().setPlaceholder("Not specified");
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
