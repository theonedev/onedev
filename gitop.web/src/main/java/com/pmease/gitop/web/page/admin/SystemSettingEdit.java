package com.pmease.gitop.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.component.FeedbackPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.setting.SystemSetting;

@SuppressWarnings("serial")
public class SystemSettingEdit extends AdministrationLayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		SystemSetting systemSetting = Gitop.getInstance(ConfigManager.class).getSystemSetting();

		final EditContext editContext = EditableUtils.getContext(systemSetting);
		
		Form<?> form = new Form<Void>("form"){

			@Override
			protected void onSubmit() {
				editContext.validate();
				if (!editContext.hasValidationError()) {
					Gitop.getInstance(ConfigManager.class).saveSystemSetting((SystemSetting) editContext.getBean());
					success("System setting has been updated");
				} else {
					error("Fix errors below");
				}
			}
			
		}; 
		form.add((Component)editContext.renderForEdit("editor"));
		form.add(new FeedbackPanel("feedback", form));
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
