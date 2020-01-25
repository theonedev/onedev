package io.onedev.server.web.page.admin.user.sshkeys;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserSshKeysPage extends UserPage {
	
	public UserSshKeysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("content", userModel));
	}
	
}
