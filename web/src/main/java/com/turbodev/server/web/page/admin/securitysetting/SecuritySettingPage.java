package com.turbodev.server.web.page.admin.securitysetting;

import org.apache.wicket.markup.html.form.Form;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.model.support.setting.SecuritySetting;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SecuritySettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SecuritySetting securitySetting = TurboDev.getInstance(ConfigManager.class).getSecuritySetting();

		Form<?> form = new Form<Void>("securitySetting") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				TurboDev.getInstance(ConfigManager.class).saveSecuritySetting(securitySetting);
				getSession().success("Security setting has been updated");
				
				setResponsePage(SecuritySettingPage.class);
			}
			
		};
		form.add(BeanContext.editBean("editor", securitySetting));
		
		add(form);
	}

}
