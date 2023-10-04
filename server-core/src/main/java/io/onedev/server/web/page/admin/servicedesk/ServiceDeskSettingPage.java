package io.onedev.server.web.page.admin.servicedesk;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.mailservice.MailServicePage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class ServiceDeskSettingPage extends AdministrationPage {

	public ServiceDeskSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("mailService", MailServicePage.class));
		
		ServiceDeskSettingHolder serviceDeskSettingHolder = new ServiceDeskSettingHolder();
		serviceDeskSettingHolder.setServiceDeskSetting(OneDev.getInstance(SettingManager.class).getServiceDeskSetting());
		
		BeanEditor editor = BeanContext.edit("editor", serviceDeskSettingHolder);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(SettingManager.class).saveServiceDeskSetting(serviceDeskSettingHolder.getServiceDeskSetting());
				getSession().success("Service desk settings have been saved");
			}
			
		};
		
		Form<?> form = new Form<Void>("form");
		
		form.add(editor);
		form.add(saveButton);
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Service Desk Settings");
	}

}