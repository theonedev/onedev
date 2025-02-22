package io.onedev.server.web.component.pack.choice;

import io.onedev.server.model.Pack;
import io.onedev.server.web.component.select2.SelectToActChoice;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public abstract class SelectPackToActChoice extends SelectToActChoice<Pack> {

	public SelectPackToActChoice(String id, PackChoiceProvider choiceProvider) {
		super(id, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder(getPlaceholder());
		getSettings().setFormatResult("onedev.server.packChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.packChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.packChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PackChoiceResourceReference()));
	}

	protected abstract String getPlaceholder();
	
}
