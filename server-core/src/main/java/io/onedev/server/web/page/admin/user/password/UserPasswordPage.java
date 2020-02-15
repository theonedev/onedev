package io.onedev.server.web.page.admin.user.password;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserPasswordPage extends UserPage {
	
	public UserPasswordPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (OneDev.getInstance(SettingManager.class).getAuthenticator() != null 
				&& getUser().getPassword().equals(User.EXTERNAL_MANAGED)) {
			String message = "The user is currently authenticated via external system, please change password there instead";
			add(new Label("content", message).add(AttributeAppender.append("class", "alert alert-warning")));
		} else {
			add(new PasswordEditPanel("content", userModel));
		}
	}
	
}
