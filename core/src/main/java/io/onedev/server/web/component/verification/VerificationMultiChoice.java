package io.onedev.server.web.component.verification;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2MultiChoice;

public class VerificationMultiChoice extends Select2MultiChoice<String> {

	private static final long serialVersionUID = 1L;

	public VerificationMultiChoice(String id, IModel<Collection<String>> model, VerificationChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose verifications...");
		getSettings().setFormatResult("onedev.server.verificationChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.verificationChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.verificationChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new VerificationResourceReference()));
	}

}
