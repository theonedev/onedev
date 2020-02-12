package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class MilestoneSingleChoice extends Select2Choice<Milestone> {

	public MilestoneSingleChoice(String id, IModel<Milestone> selectionModel, IModel<Collection<Milestone>> milestonesModel) {
		super(id, selectionModel, new MilestoneChoiceProvider(milestonesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose milestone...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.milestoneChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.milestoneChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.milestoneChoiceFormatter.escapeMarkup");
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
		
		response.render(JavaScriptHeaderItem.forReference(new MilestoneChoiceResourceReference()));
	}
	
}
