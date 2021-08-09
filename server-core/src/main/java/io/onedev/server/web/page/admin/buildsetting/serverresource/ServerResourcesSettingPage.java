package io.onedev.server.web.page.admin.buildsetting.serverresource;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class ServerResourcesSettingPage extends AdministrationPage {

	public ServerResourcesSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GlobalBuildSetting buildSetting = OneDev.getInstance(SettingManager.class).getBuildSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveBuildSetting(buildSetting);
				getSession().success("Server resource setting has been saved");
				
				setResponsePage(ServerResourcesSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", buildSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Server Resource");
	}

}
