package com.turbodev.server.web.page.user;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.launcher.loader.AppLoader;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public class PasswordEditPage extends UserPage {
	
	public PasswordEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PasswordEditBean bean = new PasswordEditBean();
		
		Set<String> excludedProperties = new HashSet<>();
		
		// in case administrator changes password we do not ask for old password
		if (SecurityUtils.isAdministrator()) 
			excludedProperties.add("oldPassword");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getUser().setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(bean.getNewPassword()));
				TurboDev.getInstance(UserManager.class).save(getUser(), null);
				Session.get().success("Password has been changed");

				bean.setOldPassword(null);
				replace(BeanContext.editBean("editor", bean, excludedProperties));
			}

		};
		add(form);
		
		form.add(BeanContext.editBean("editor", bean, excludedProperties));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getUser());
	}
	
}
