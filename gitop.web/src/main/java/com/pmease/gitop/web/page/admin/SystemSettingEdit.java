package com.pmease.gitop.web.page.admin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.setting.SystemSetting;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class SystemSettingEdit extends AdministrationLayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		final SystemSetting systemSetting = Gitop.getInstance(ConfigManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form"){

			@Override
			protected void onSubmit() {
				Gitop.getInstance(ConfigManager.class).saveSystemSetting(systemSetting);
				success("System setting has been updated");
			}

			@Override
			protected void onError() {
				super.onError();
				error("Fix errors below");
			}
			
		}; 
		form.add(BeanContext.edit("editor", systemSetting));
		form.add(new NotificationPanel("feedback", form));
		form.add(new AjaxSubmitLink("update") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(SystemSettingEdit.this);
			}
			
		});
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - System Settings";
	}
}
