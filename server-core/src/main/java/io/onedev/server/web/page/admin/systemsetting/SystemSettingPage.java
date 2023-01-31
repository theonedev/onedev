package io.onedev.server.web.page.admin.systemsetting;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SystemSettingPage extends AdministrationPage {

	public SystemSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SystemSetting systemSetting = OneDev.getInstance(SettingManager.class).getSystemSetting();

		String ingressUrl = OneDev.getInstance().getIngressUrl();
		add(new TextField<String>("ingressUrl", Model.of(ingressUrl)).setVisible(ingressUrl != null));

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveSystemSetting(systemSetting);
				getSession().success("System settings have been saved");
				
				setResponsePage(SystemSettingPage.class);
			}
			
		};
		Collection<String> excludedProps = new HashSet<>();
		if (new File(Bootstrap.installDir, "IN_DOCKER").exists()) {
			excludedProps.add(SystemSetting.PROP_GIT_LOCATION);
			excludedProps.add(SystemSetting.PROP_CURL_LOCATION);
		}
		if (OneDev.getInstance().getIngressUrl() != null)
			excludedProps.add("serverUrl");
		
		form.add(BeanContext.edit("editor", systemSetting, excludedProps, true));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "System Settings");
	}

}
