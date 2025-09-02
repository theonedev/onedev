package io.onedev.server.web.page.my.ssoaccounts;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.ssoaccount.SsoAccountListPanel;
import io.onedev.server.web.page.my.MyPage;

public class MySsoAccountsPage extends MyPage {
		
	public MySsoAccountsPage(PageParameters params) {
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
		    	return getLoginUser();
		    }
		    
		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My SSO Accounts"));
	}
	
}
