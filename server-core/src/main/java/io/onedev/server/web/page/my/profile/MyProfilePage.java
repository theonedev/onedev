package io.onedev.server.web.page.my.profile;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.user.UserDeleteLink;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.my.MyPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
			boolean canRemoveSelf = !getLoginUser().isRoot()
					&& SecurityUtils.getPrevUserId().equals(0L)
					&& OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfDeregister();
			add(new UserDeleteLink("delete") {

				@Override
				protected User getUser() {
					return getLoginUser();
				}

			}.setVisible(canRemoveSelf));
		} else { 
			add(new ProfileEditPanel("content", new AbstractReadOnlyModel<>() {

				@Override
				public User getObject() {
					return getLoginUser();
				}

			}));
			add(new WebMarkupContainer("delete").setVisible(false));
		}
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My Profile");
	}

}
