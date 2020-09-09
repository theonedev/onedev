package io.onedev.server.web.page.my.profile;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserDeleteLink;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyProfilePage extends MyPage {

	public MyProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("externalManagedNote", "Your profile is managed from " + getLoginUser().getAuthSource())
				.setVisible(getLoginUser().isExternalManaged()));
		
		if (getLoginUser().isExternalManaged()) { 
			add(BeanContext.view("content", getLoginUser(), Sets.newHashSet("password"), true));
		} else { 
			add(new ProfileEditPanel("content", new AbstractReadOnlyModel<User>() {

				@Override
				public User getObject() {
					return getLoginUser();
				}
				
			}));
		}
		
		add(new UserDeleteLink("delete") {

			@Override
			protected User getUser() {
				return getLoginUser();
			}
			
		}.setVisible(getLoginUser().isExternalManaged()));
		
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My Profile");
	}

}
