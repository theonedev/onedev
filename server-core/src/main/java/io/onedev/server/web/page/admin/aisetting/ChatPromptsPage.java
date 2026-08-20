package io.onedev.server.web.page.admin.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.web.page.admin.AdministrationPage;

public class ChatPromptsPage extends AdministrationPage {

	public ChatPromptsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView promptsView = new RepeatingView("prompts");
		promptsView.add(new ChatPromptEditPanel(promptsView.newChildId(),
				AiSetting.PROP_CODE_EXPLANATION_PROMPT, AiSetting.DEFAULT_CODE_EXPLANATION_PROMPT));
		promptsView.add(new ChatPromptEditPanel(promptsView.newChildId(),
				AiSetting.PROP_ISSUE_SUMMARY_PROMPT, AiSetting.DEFAULT_ISSUE_SUMMARY_PROMPT));
		promptsView.add(new ChatPromptEditPanel(promptsView.newChildId(),
				AiSetting.PROP_PULL_REQUEST_SUMMARY_PROMPT, AiSetting.DEFAULT_PULL_REQUEST_SUMMARY_PROMPT));
		promptsView.add(new ChatPromptEditPanel(promptsView.newChildId(),
				AiSetting.PROP_BUILD_FAILURE_ISSUE_PROMPT, AiSetting.DEFAULT_BUILD_FAILURE_ISSUE_PROMPT));
		add(promptsView);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Chat Prompts"));
	}

}
