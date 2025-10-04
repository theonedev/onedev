package io.onedev.server.web.page.admin.issuesetting.timetracking;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class TimeTrackingSettingPage extends IssueSettingPage {

	public TimeTrackingSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var timeTrackingSetting = getSettingService().getIssueSetting().getTimeTrackingSetting();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				var issueSetting = getSettingService().getIssueSetting();
				var oldAuditContent = VersionedXmlDoc.fromBean(issueSetting.getTimeTrackingSetting()).toXML();
				issueSetting.setTimeTrackingSetting(timeTrackingSetting);
				var newAuditContent = VersionedXmlDoc.fromBean(issueSetting.getTimeTrackingSetting()).toXML();
				getSettingService().saveIssueSetting(issueSetting);
				auditService.audit(null, "changed time tracking settings", oldAuditContent, newAuditContent);
				Session.get().success(_T("Time tracking settings have been saved"));
			}
		};
		form.add(BeanContext.edit("editor", timeTrackingSetting));
		add(form);
	}
	
	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Time Tracking Settings") + "</span>").setEscapeModelStrings(false);
	}
	
}
