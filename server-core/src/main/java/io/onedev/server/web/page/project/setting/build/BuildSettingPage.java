package io.onedev.server.web.page.project.setting.build;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.BuildSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class BuildSettingPage extends ProjectSettingPage {

	public BuildSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildSetting buildSetting = getProject().getBuildSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getProject().setBuildSetting(buildSetting);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				getSession().success("Build setting has been updated");
				
				setResponsePage(BuildSettingPage.class, BuildSettingPage.paramsOf(getProject()));
			}
			
		};
		form.add(BeanContext.edit("editor", buildSetting));
		
		add(form);
	}

}
