package io.onedev.server.web.page.admin.generalsecuritysetting;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class GeneralSecuritySettingPage extends AdministrationPage {

	public GeneralSecuritySettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SecuritySetting securitySetting = OneDev.getInstance(SettingManager.class).getSecuritySetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveSecuritySetting(securitySetting);
				getSession().success("Security setting has been updated");
				
				setResponsePage(GeneralSecuritySettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", securitySetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "General Security Setting");
	}

}
