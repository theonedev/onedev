package io.onedev.server.web.page.admin.user.profile;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserProfilePage extends UserPage {

	public UserProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("externalManagedNote", "Profile of this user is managed from " + getUser().getAuthSource())
				.setVisible(getUser().isExternalManaged()));
		
		if (getUser().isExternalManaged()) 
			add(BeanContext.view("content", getUser(), Sets.newHashSet("password"), true));
		else 
			add(new ProfileEditPanel("content", userModel));
	}

}
