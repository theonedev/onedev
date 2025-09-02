package io.onedev.server.web.page.user.ssoaccounts;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.ssoaccount.SsoAccountListPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserSsoAccountsPage extends UserPage {
		
	public UserSsoAccountsPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled() || getUser().isServiceAccount())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		            
		add(new SsoAccountListPanel("accountList", new LoadableDetachableModel<User>() {
			
		    @Override
		    protected User load() {
		    	return getUser();
		    }
		    
		}));
	}
	
}
