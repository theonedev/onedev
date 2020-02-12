package io.onedev.server.web.component.branch.choice;

import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;

@SuppressWarnings("serial")
public class BranchSingleChoice extends Select2Choice<String> {

	public BranchSingleChoice(String id, IModel<String> model, IModel<Map<String, String>> choicesModel) {
		super(id, model, new StringChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (isRequired())
			getSettings().setPlaceholder("Choose branch...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.branchChoiceFormatter.escapeMarkup");
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
		response.render(JavaScriptHeaderItem.forReference(new BranchChoiceResourceReference()));
	}
	
}