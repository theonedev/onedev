package io.onedev.server.web.page.user.aisetting;

import static io.onedev.server.model.User.Type.AI;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.user.UserPage;

public abstract class UserAiSettingPage extends UserPage {
		
	public UserAiSettingPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled() || getUser().getType() != AI)
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		var params = paramsOf(getUser());
		tabs.add(new PageTab(Model.of(_T("Model")), null, UserModelSettingPage.class, params));		
		tabs.add(new PageTab(Model.of(_T("System Prompt")), null, UserSystemPromptPage.class, params));		
		tabs.add(new PageTab(Model.of(_T("Entitlement")), null, UserEntitlementSettingPage.class, params));		

		add(new Tabbable("aiSettingTabs", tabs));
	}
	
}
