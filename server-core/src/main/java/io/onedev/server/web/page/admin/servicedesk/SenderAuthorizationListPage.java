package io.onedev.server.web.page.admin.servicedesk;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SenderAuthorizationListPage extends AdministrationPage {

	public SenderAuthorizationListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ServiceDeskSetting setting = getSettingManager().getServiceDeskSetting();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getSettingManager().saveServiceDeskSetting(setting);
			}
			
		};
		
		form.add(BeanContext.edit("editor", setting, 
				Lists.newArrayList(ServiceDeskSetting.PROP_SENDER_AUTHORIZATIONS), false));
		add(form);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Sender Authorizations");
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
}
