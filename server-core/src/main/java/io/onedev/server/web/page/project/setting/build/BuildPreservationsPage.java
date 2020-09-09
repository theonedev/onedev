package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class BuildPreservationsPage extends BuildSettingPage {

	public BuildPreservationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildPreservationsBean bean = new BuildPreservationsBean();
		bean.setBuildPreservations(getProject().getBuildSetting().getBuildPreservations());
		
		PropertyEditor<Serializable> editor = PropertyContext.edit("editor", bean, "buildPreservations");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSession().success("Build preserve rules saved");
				getProject().getBuildSetting().setBuildPreservations(bean.getBuildPreservations());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(BuildPreservationsPage.class, 
						BuildPreservationsPage.paramsOf(getProject()));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Build Preserve Rules");
	}

}
