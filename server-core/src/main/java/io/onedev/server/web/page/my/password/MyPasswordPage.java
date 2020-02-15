package io.onedev.server.web.page.my.password;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyPasswordPage extends MyPage {
	
	public MyPasswordPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (OneDev.getInstance(SettingManager.class).getAuthenticator() != null 
				&& getLoginUser().getPassword().equals(User.EXTERNAL_MANAGED)) {
			String message = "You are currently authenticated via external system, please change password there instead";
			add(new Label("content", message).add(AttributeAppender.append("class", "alert alert-warning")));
		} else {
			add(new PasswordEditPanel("content", new AbstractReadOnlyModel<User>() {

				@Override
				public User getObject() {
					return getLoginUser();
				}
				
			}));
		}
	}
	
}
