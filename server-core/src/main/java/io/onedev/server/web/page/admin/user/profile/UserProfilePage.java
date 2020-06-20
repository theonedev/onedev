package io.onedev.server.web.page.admin.user.profile;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserDeleteLink;
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
		
		if (getUser().isExternalManaged()) {
			add(BeanContext.view("content", getUser(), Sets.newHashSet("password"), true));
			add(new UserDeleteLink("delete") {

				@Override
				protected User getUser() {
					return UserProfilePage.this.getUser();
				}
				
			});
		} else {
			add(new ProfileEditPanel("content", userModel));
			add(new WebMarkupContainer("delete").setVisible(false));
		}
		
	}

}
