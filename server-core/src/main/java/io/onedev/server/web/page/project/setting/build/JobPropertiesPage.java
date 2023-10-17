package io.onedev.server.web.page.project.setting.build;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.web.editable.PropertyContext;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("serial")
public class JobPropertiesPage extends ProjectBuildSettingPage {
	
	private Component showArchivedButton;
	
	private Form<?> form;
	
	public JobPropertiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new JobPropertiesBean();
		bean.setProperties(getDisplayProperties());
		
		var editor = PropertyContext.edit("editor", bean, "properties");
		
		form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Job properties saved");
				getProject().getBuildSetting().setJobProperties(bean.getProperties());
				OneDev.getInstance(ProjectManager.class).update(getProject());
				bean.setProperties(getDisplayProperties());
				var editor = PropertyContext.edit("editor", bean, "properties");
				form.replace(editor);
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		
		form.add(showArchivedButton = new Link<Void>("showArchived") {

			@Override
			public void onClick() {
				showArchivedButton.setVisibilityAllowed(false);
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
			.filter(it -> showArchivedButton != null && !showArchivedButton.isVisibilityAllowed() || !it.isArchived())
			.sorted((o1, o2) -> {
				if (o1.isArchived() && !o2.isArchived())
					return 1;
				else if (!o1.isArchived() && o2.isArchived())
					return -1;
				else
					return 0;
			}).collect(toList());
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Job Properties");
	}

}
