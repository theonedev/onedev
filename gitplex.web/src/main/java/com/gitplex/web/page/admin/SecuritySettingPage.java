package com.gitplex.web.page.admin;

import org.apache.wicket.markup.html.form.Form;

import com.gitplex.core.GitPlex;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.core.setting.SecuritySetting;
import com.gitplex.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class SecuritySettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SecuritySetting securitySetting = GitPlex.getInstance(ConfigManager.class).getSecuritySetting();

		Form<?> form = new Form<Void>("securitySetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				GitPlex.getInstance(ConfigManager.class).saveSecuritySetting(securitySetting);
				getSession().success("Security setting has been updated");
				
				setResponsePage(SecuritySettingPage.class);
			}
			
		};
		form.add(BeanContext.editBean("editor", securitySetting));
		
		add(form);
	}

}
