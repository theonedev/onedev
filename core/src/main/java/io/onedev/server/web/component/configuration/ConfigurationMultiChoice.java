package io.onedev.server.web.component.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public class ConfigurationMultiChoice extends Select2MultiChoice<String> {

	private static final long serialVersionUID = 1L;

	public ConfigurationMultiChoice(String id, IModel<Collection<String>> model) {
		super(id, model, new StringChoiceProvider(getChoices()));
	}

	private static Map<String, String> getChoices() {
		Map<String, String> choices = new LinkedHashMap<>();
		if (WicketUtils.getPage() instanceof ProjectPage) {
			ProjectPage projectPage = (ProjectPage) WicketUtils.getPage();
			List<String> configurations = projectPage.getProject().getConfigurations()
					.stream().map(it->it.getName()).collect(Collectors.toList());
			Collections.sort(configurations);
			for (String configuration: configurations)
				choices.put(configuration, configuration);
		}
		return choices;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose configurations...");
		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
	}

}
