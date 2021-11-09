package io.onedev.server.web.component.milestone.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class MilestoneMultiChoice extends Select2MultiChoice<Milestone> {

	private static final long serialVersionUID = 1L;

	public MilestoneMultiChoice(String id, IModel<Collection<Milestone>> selectionsModel, IModel<Collection<Milestone>>choicesModel) {
		super(id, selectionsModel, new MilestoneChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose milestones...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.milestoneChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.milestoneChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.milestoneChoiceFormatter.escapeMarkup");
        setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new MilestoneChoiceResourceReference()));
	}

}
