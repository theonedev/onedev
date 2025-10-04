package io.onedev.server.web.page.project.setting.webhook;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class WebHooksPage extends ProjectSettingPage {

	public WebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHooksBean bean = new WebHooksBean();
		bean.setWebHooks(getProject().getWebHooks());
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();

		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "webHooks");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().setWebHooks(bean.getWebHooks());
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed web hooks", oldAuditContent, newAuditContent);
				setResponsePage(WebHooksPage.class, WebHooksPage.paramsOf(getProject()));
				getSession().success(_T("Web hooks saved"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Web Hooks"));
	}

}
