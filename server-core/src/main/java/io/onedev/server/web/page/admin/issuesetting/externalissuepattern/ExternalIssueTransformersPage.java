package io.onedev.server.web.page.admin.issuesetting.externalissuepattern;

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
import io.onedev.server.model.support.issue.ExternalIssueTransformers;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class ExternalIssueTransformersPage extends IssueSettingPage {

    private static final long serialVersionUID = 1L;

    private String oldAuditContent;

    public ExternalIssueTransformersPage(PageParameters params) {
        super(params);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

		ExternalIssueTransformers transformers = getSetting().getExternalIssueTransformers();
		oldAuditContent = VersionedXmlDoc.fromBean(transformers).toXML();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSetting().setExternalIssueTransformers(transformers);
				var newAuditContent = VersionedXmlDoc.fromBean(transformers).toXML();
				getSettingService().saveIssueSetting(getSetting());
				auditService.audit(null, "changed external issue transformers", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				Session.get().success(_T("Settings updated"));
			}
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", transformers));
		add(form);
    }

    private SettingService getSettingService() {
        return OneDev.getInstance(SettingService.class);
    }

    @Override
    protected Component newTopbarTitle(String componentId) {
        return new Label(componentId, _T("External Issue Transformers"));
    }

} 