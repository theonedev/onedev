package io.onedev.server.web.page.admin.securitysetting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

public class SecuritySettingPage extends AdministrationPage {

	public SecuritySettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SecuritySetting securitySetting = OneDev.getInstance(SettingService.class).getSecuritySetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(securitySetting).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingService.class).saveSecuritySetting(securitySetting);
				var newAuditContent = VersionedXmlDoc.fromBean(securitySetting).toXML();
				auditService.audit(null, "changed security settings", oldAuditContent, newAuditContent);

				getSession().success(_T("Security settings have been updated"));				
				setResponsePage(SecuritySettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", securitySetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Security Settings"));
	}

}
