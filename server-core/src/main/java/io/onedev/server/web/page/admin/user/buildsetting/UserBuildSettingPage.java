package io.onedev.server.web.page.admin.user.buildsetting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.support.build.UserBuildSetting;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.admin.user.UserTab;

@SuppressWarnings("serial")
public abstract class UserBuildSettingPage extends UserPage {

	public UserBuildSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new UserTab("Job Secrets", null, UserJobSecretsPage.class));
		tabs.add(new UserTab("Action Authorizations", null, UserActionAuthorizationsPage.class));
		tabs.add(new UserTab("Build Preserve Rules", null, UserBuildPreservationsPage.class));
		add(new Tabbable("buildSettingTabs", tabs));
	}
	
	protected UserBuildSetting getBuildSetting() {
		return getUser().getBuildSetting();
	}

}
