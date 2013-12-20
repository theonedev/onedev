package com.pmease.gitop.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.setting.MailSetting;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class MailSettingEdit extends AbstractLayoutPage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		MailSetting mailSetting = Gitop.getInstance(ConfigManager.class).getMailSetting();
		if (mailSetting == null)
			mailSetting = new MailSetting();

		final EditContext editContext = EditableUtils.getContext(mailSetting);
		
		Form<?> form = new Form<Void>("form"){

			@Override
			protected void onSubmit() {
				editContext.validate();
				if (!editContext.hasValidationError()) {
					Gitop.getInstance(ConfigManager.class).saveMailSetting((MailSetting) editContext.getBean());
					getSession().info("Email setting has been updated.");
					setResponsePage(MailSettingEdit.class);
				}
			}
			
		}; 
		form.add((Component)editContext.renderForEdit("objectEditor"));
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Mail Setting";
	}
	
}
