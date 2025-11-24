package io.onedev.server.web.page.user.aisetting;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.aisetting.EntitlementSettingPanel;

public class UserEntitlementSettingPage extends UserAiSettingPage {
		
	public UserEntitlementSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		            
		add(new EntitlementSettingPanel("entitlementSetting", new LoadableDetachableModel<User>() {
			
		    @Override
		    protected User load() {
		    	return getUser();
		    }
		    
		}));
	}
	
}
