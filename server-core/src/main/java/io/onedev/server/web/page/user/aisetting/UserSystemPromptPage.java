package io.onedev.server.web.page.user.aisetting;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.aisetting.SystemPromptPanel;

public class UserSystemPromptPage extends UserAiSettingPage {
		
	public UserSystemPromptPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new SystemPromptPanel("systemPrompt", new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return getUser();
			}

		}));
	}
	
}
