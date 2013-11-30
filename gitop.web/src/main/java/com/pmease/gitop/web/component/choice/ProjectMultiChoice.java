package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.Project;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2MultiChoice;

public class ProjectMultiChoice extends Select2MultiChoice<Project> {
	private static final long serialVersionUID = 1L;

	public ProjectMultiChoice(String id, IModel<Collection<Project>> model, ChoiceProvider<Project> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Start typing to find projects ...");
		getSettings().setFormatResult("ProjectChoice.formatter.formatResult");
		getSettings()
				.setFormatSelection("ProjectChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("ProjectChoice.formatter.escapeMarkup");
	}

	private ResourceReference projectChoiceReference = new PackageResourceReference(
			ProjectMultiChoice.class, "projectchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(projectChoiceReference));
	}
}
