package io.onedev.server.web.component.iteration.choice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Iteration;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class IterationMultiChoice extends Select2MultiChoice<Iteration> {

	private static final long serialVersionUID = 1L;

	public IterationMultiChoice(String id, IModel<Collection<Iteration>> selectionsModel, IModel<Collection<Iteration>>choicesModel) {
		super(id, selectionsModel, new IterationChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose iterations..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.iterationChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.iterationChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.iterationChoiceFormatter.escapeMarkup");
        setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IterationChoiceResourceReference()));
	}

}
