package io.onedev.server.web.page.project.setting.configuration;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.model.Configuration;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class ConfigurationEditPage extends ProjectSettingPage {

	private static final String PARAM_CONFIGURATION = "configuration";
	
	private final Long configurationId;
	
	private final String oldName;
	
	public ConfigurationEditPage(PageParameters params) {
		super(params);
		
		configurationId = params.get(PARAM_CONFIGURATION).toLong();
		oldName = getConfiguration().getName();
	}

	private Configuration getConfiguration() {
		return OneDev.getInstance(ConfigurationManager.class).load(configurationId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Configuration configuration = getConfiguration();
		BeanEditor editor = BeanContext.editBean("editor", configuration);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ConfigurationManager configurationManager = OneDev.getInstance(ConfigurationManager.class);
				Configuration configurationWithSameName = configurationManager.find(getProject(), configuration.getName());
				if (configurationWithSameName != null && !configurationWithSameName.equals(configuration)) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another configuration in the project");
				} 
				if (!editor.hasErrors(true)) {
					Configuration reloaded = getConfiguration();
					editor.getBeanDescriptor().copyProperties(configuration, reloaded);
					configurationManager.save(reloaded, oldName);
					setResponsePage(ConfigurationListPage.class, ConfigurationListPage.paramsOf(getProject()));
					Session.get().success("Configuration updated");
				}
			}
			
		};	
		form.add(editor);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ConfigurationCssResourceReference()));
	}

	public static PageParameters paramsOf(Configuration configuration) {
		PageParameters params = paramsOf(configuration.getProject());
		params.add(PARAM_CONFIGURATION, configuration.getId());
		return params;
	}
}
