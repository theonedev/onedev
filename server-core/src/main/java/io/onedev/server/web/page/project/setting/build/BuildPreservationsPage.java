package io.onedev.server.web.page.project.setting.build;

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

public class BuildPreservationsPage extends ProjectBuildSettingPage {

	public BuildPreservationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildPreservationsBean bean = new BuildPreservationsBean();
		bean.setBuildPreservations(getProject().getBuildSetting().getBuildPreservations());
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "buildPreservations");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().getBuildSetting().setBuildPreservations(bean.getBuildPreservations());
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed build preserve rules", oldAuditContent, newAuditContent);
				setResponsePage(BuildPreservationsPage.class, 
						BuildPreservationsPage.paramsOf(getProject()));
					getSession().success(_T("Build preserve rules saved"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Build Preserve Rules"));
	}

}
