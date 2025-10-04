package io.onedev.server.web.page.admin.performancesetting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

public class PerformanceSettingPage extends AdministrationPage {

	public PerformanceSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PerformanceSetting performanceSetting = OneDev.getInstance(SettingService.class).getPerformanceSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(performanceSetting).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(performanceSetting).toXML();
				OneDev.getInstance(SettingService.class).savePerformanceSetting(performanceSetting);
				auditService.audit(null, "changed performance settings", oldAuditContent, newAuditContent);				
				getSession().success(_T("Performance settings have been saved"));
				
				setResponsePage(PerformanceSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", performanceSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Performance Settings"));
	}

}
