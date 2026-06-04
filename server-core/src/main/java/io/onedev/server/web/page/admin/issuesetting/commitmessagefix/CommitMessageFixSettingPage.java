package io.onedev.server.web.page.admin.issuesetting.commitmessagefix;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class CommitMessageFixSettingPage extends IssueSettingPage {

	private String oldAuditContent;
	
	public CommitMessageFixSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var commitMessageFixSetting = getSetting().getCommitMessageFixSetting();
		oldAuditContent = VersionedXmlDoc.fromBean(commitMessageFixSetting).toXML();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSetting().setCommitMessageFixSetting(commitMessageFixSetting);
				var newAuditContent = VersionedXmlDoc.fromBean(commitMessageFixSetting).toXML();
				getSettingService().saveIssueSetting(getSetting());
				auditService.audit(null, "changed commit message fix settings", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				Session.get().success(_T("Settings updated"));
			}
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", commitMessageFixSetting));
		add(form);
	}
	
	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Commit Message Fix Settings") + "</span>").setEscapeModelStrings(false);
	}
	
}
