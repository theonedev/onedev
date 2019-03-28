package io.onedev.server.web.page.project.setting.configuration;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ConfigurationManager;
import io.onedev.server.model.Configuration;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class NewConfigurationPage extends ProjectSettingPage {

	private Configuration configuration = new Configuration();
	
	public NewConfigurationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.editBean("editor", configuration);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ConfigurationManager configurationManager = OneDev.getInstance(ConfigurationManager.class);
				Configuration configurationWithSameName = configurationManager.find(getProject(), configuration.getName());
				if (configurationWithSameName != null) {
					editor.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another configuration in the project");
				} 
				if (!editor.hasErrors(true)) {
					configuration.setProject(getProject());
					configurationManager.save(configuration, null);
					Session.get().success("Configuration created");
					setResponsePage(ConfigurationListPage.class, ConfigurationListPage.paramsOf(getProject()));
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
}
