package io.onedev.server.web.page.my.buildsetting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.support.UserBuildSetting;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public abstract class MyBuildSettingPage extends MyPage {

	public MyBuildSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Secrets"), MySecretsPage.class));
		tabs.add(new PageTab(Model.of("Build Preserve Rules"), MyBuildPreservationsPage.class));
		add(new Tabbable("buildSettingTabs", tabs));
	}

	protected UserBuildSetting getBuildSetting() {
		return getLoginUser().getBuildSetting();
	}
	
}
