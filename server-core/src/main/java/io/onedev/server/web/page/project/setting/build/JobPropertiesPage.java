package io.onedev.server.web.page.project.setting.build;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;

@SuppressWarnings("serial")
public class JobPropertiesPage extends ProjectBuildSettingPage {

	public JobPropertiesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobPropertiesBean bean = new JobPropertiesBean();
		bean.setProperties(getProject().getBuildSetting().getJobProperties());
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "properties");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Job properties saved");
				getProject().getBuildSetting().setJobProperties(bean.getProperties());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(JobPropertiesPage.class, 
						JobPropertiesPage.paramsOf(getProject()));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Job Properties");
	}

}
