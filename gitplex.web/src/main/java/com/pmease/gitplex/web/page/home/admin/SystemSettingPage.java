package com.pmease.gitplex.web.page.home.admin;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.setting.SystemSetting;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	private BeanEditor<Serializable> editor;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final SystemSetting systemSetting = GitPlex.getInstance(ConfigManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form");
		form.add(editor = BeanContext.editBean("editor", systemSetting));
		
		// use ajax in order not to clean form dirty state in case there are field errors 
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				GitPlex.getInstance(ConfigManager.class).saveSystemSetting(systemSetting);
				getSession().success("System setting has been updated");
				
				setResponsePage(SystemSettingPage.class);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				
				getSession().error("Fix errors below");
				target.add(editor);
			}
			
		});
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - System Settings";
	}
}
