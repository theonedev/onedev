package com.gitplex.server.web.page.admin.systemsetting;

import org.apache.wicket.markup.html.form.Form;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.model.support.setting.SystemSetting;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.page.admin.AdministrationPage;

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
