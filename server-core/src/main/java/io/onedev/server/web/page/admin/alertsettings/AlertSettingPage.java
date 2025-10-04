package io.onedev.server.web.page.admin.alertsettings;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

public class AlertSettingPage extends AdministrationPage {

	public AlertSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var alertSetting = OneDev.getInstance(SettingService.class).getAlertSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(alertSetting.getNotifyUsers()).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(alertSetting.getNotifyUsers()).toXML();
				OneDev.getInstance(SettingService.class).saveAlertSetting(alertSetting);
				auditService.audit(null, "changed alert settings", oldAuditContent, newAuditContent);
				getSession().success(_T("Alert settings have been updated"));
				
				setResponsePage(AlertSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", alertSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Alert Settings"));
	}

}
