package io.onedev.server.web.page.my;

import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;

@SuppressWarnings("serial")
public class MyPasswordPage extends MyPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PasswordEditPanel("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}
			
		}));
	}
	
}
