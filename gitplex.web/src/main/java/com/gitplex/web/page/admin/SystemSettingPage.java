package com.gitplex.web.page.admin;

import org.apache.wicket.markup.html.form.Form;

import com.gitplex.core.GitPlex;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.core.setting.SystemSetting;
import com.gitplex.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SystemSetting systemSetting = GitPlex.getInstance(ConfigManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				GitPlex.getInstance(ConfigManager.class).saveSystemSetting(systemSetting);
				getSession().success("System setting has been updated");
				
				setResponsePage(SystemSettingPage.class);
			}
			
		};
		form.add(BeanContext.editBean("editor", systemSetting));
		
		add(form);
	}

}
