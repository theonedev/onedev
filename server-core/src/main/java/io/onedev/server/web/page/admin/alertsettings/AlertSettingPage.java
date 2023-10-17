package io.onedev.server.web.page.admin.alertsettings;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class AlertSettingPage extends AdministrationPage {

	public AlertSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var alertSetting = OneDev.getInstance(SettingManager.class).getAlertSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveAlertSetting(alertSetting);
				getSession().success("Alert settings have been updated");
				
				setResponsePage(AlertSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", alertSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Alert Settings");
	}

}
