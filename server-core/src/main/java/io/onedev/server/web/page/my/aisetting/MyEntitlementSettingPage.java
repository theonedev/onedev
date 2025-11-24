package io.onedev.server.web.page.my.aisetting;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.aisetting.EntitlementSettingPanel;

public class MyEntitlementSettingPage extends MyAiSettingPage {
	
	public MyEntitlementSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new EntitlementSettingPanel("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getUser();
			}
			
		}));
	}

}
