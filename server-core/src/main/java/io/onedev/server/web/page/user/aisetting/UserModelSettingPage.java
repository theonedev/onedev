package io.onedev.server.web.page.user.aisetting;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.aisetting.ModelSettingPanel;

public class UserModelSettingPage extends UserAiSettingPage {
		
	public UserModelSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		            
		add(new ModelSettingPanel("modelSetting", new LoadableDetachableModel<User>() {
			
		    @Override
		    protected User load() {
		    	return getUser();
		    }
		    
		}));
	}
	
}
