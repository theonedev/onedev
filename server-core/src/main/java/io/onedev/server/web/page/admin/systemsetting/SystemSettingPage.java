package io.onedev.server.web.page.admin.systemsetting;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	public SystemSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SystemSetting systemSetting = OneDev.getInstance(SettingManager.class).getSystemSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveSystemSetting(systemSetting);
				getSession().success("System setting has been updated");
				
				setResponsePage(SystemSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", systemSetting));
		
		add(form);
	}

}
