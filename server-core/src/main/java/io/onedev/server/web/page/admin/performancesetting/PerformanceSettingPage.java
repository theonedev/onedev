package io.onedev.server.web.page.admin.performancesetting;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.PerformanceSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class PerformanceSettingPage extends AdministrationPage {

	public PerformanceSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PerformanceSetting performanceSetting = OneDev.getInstance(SettingManager.class).getPerformanceSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).savePerformanceSetting(performanceSetting);
				getSession().success("Performance settings have been saved");
				
				setResponsePage(PerformanceSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", performanceSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Performance Settings");
	}

}
