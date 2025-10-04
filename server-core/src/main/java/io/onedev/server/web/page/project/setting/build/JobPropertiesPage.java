package io.onedev.server.web.page.project.setting.build;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.web.editable.PropertyContext;

public class JobPropertiesPage extends ProjectBuildSettingPage {
	
	private boolean showArchived;
	
	private Form<?> form;
	
	public JobPropertiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getBuildSetting().getJobProperties()).toXML();
		var bean = new JobPropertiesBean();
		bean.setProperties(getDisplayProperties());
		
		var editor = PropertyContext.edit("editor", bean, "properties");
		
		form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getProject().getBuildSetting().setJobProperties(bean.getProperties());
				var newAuditContent = VersionedXmlDoc.fromBean(getProject().getBuildSetting().getJobProperties()).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed job properties", oldAuditContent, newAuditContent);
				setResponsePage(JobPropertiesPage.class, JobPropertiesPage.paramsOf(getProject()));
				getSession().success(_T("Job properties saved"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		
		form.add(new Link<Void>("toggleArchived") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						return showArchived? _T("Hide Archived"): _T("Show Archived");
					}
				}));
			}

			@Override
			public void onClick() {
				showArchived = !showArchived;
				bean.setProperties(getDisplayProperties());
				var editor = PropertyContext.edit("editor", bean, "properties");
				form.replace(editor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getBuildSetting().getJobProperties().stream().anyMatch(JobProperty::isArchived));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(form);
	}
	
	private List<JobProperty> getDisplayProperties() {
		return getProject().getBuildSetting().getJobProperties()
			.stream()
			.filter(it -> showArchived || !it.isArchived())
			.collect(toList());
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Job Properties"));
	}

}
