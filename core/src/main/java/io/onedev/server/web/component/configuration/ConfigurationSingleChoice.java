package io.onedev.server.web.component.configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public class ConfigurationSingleChoice extends Select2Choice<String> {

	private static final long serialVersionUID = 1L;

	public ConfigurationSingleChoice(String id, IModel<String> model) {
		super(id, model, new StringChoiceProvider(getConfigurationNames()));
	}

	private static List<String> getConfigurationNames() {
		ProjectPage projectPage = (ProjectPage) WicketUtils.getPage();
		List<String> configurationNames = projectPage.getProject().getConfigurations()
				.stream().map(it->it.getName()).collect(Collectors.toList());
		Collections.sort(configurationNames);
		return configurationNames;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose configuration...");
		getSettings().setFormatResult("onedev.server.configurationChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.configurationChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.configurationChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new ConfigurationChoiceResourceReference()));
	}

}
