package io.onedev.server.web.page.admin.jobexecutor;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class JobExecutorPage extends AdministrationPage {

	public JobExecutorPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobExecutorBean bean = new JobExecutorBean();
		bean.setJobExecutor(OneDev.getInstance(SettingManager.class).getJobExecutor());
		
		PropertyEditor<Serializable> editor = PropertyContext.editBean("editor", bean, "jobExecutor");
		editor.setOutputMarkupId(true);
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(SettingManager.class).saveJobExecutor(bean.getJobExecutor());
				getSession().success("Job executor setting has been saved");
			}
			
		};

		Form<?> form = new Form<Void>("jobExecutor");
		
		form.add(editor);
		form.add(saveButton);
		
		add(form);
	}

}