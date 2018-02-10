package com.turbodev.server.web.page.admin.systemsetting;

import org.apache.wicket.markup.html.form.Form;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.model.support.setting.SystemSetting;
import com.turbodev.server.web.editable.BeanContext;
import com.turbodev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SystemSetting systemSetting = TurboDev.getInstance(ConfigManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				TurboDev.getInstance(ConfigManager.class).saveSystemSetting(systemSetting);
				getSession().success("System setting has been updated");
				
				setResponsePage(SystemSettingPage.class);
			}
			
		};
		form.add(BeanContext.editBean("editor", systemSetting));
		
		add(form);
	}

}
