package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.setting.SystemSetting;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		final SystemSetting systemSetting = GitPlex.getInstance(ConfigManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				GitPlex.getInstance(ConfigManager.class).saveSystemSetting(systemSetting);
				getSession().success("System setting has been updated");
			}

			@Override
			protected void onError() {
				super.onError();
				getSession().error("Fix errors below");
			}
			
		};
		form.add(BeanContext.editBean("editor", systemSetting));
		
		sidebar.add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - System Settings";
	}
}
