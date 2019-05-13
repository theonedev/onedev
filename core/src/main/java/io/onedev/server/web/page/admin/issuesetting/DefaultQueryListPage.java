package io.onedev.server.web.page.admin.issuesetting;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
public class DefaultQueryListPage extends GlobalIssueSettingPage {

	public DefaultQueryListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				getSession().success("Default queries have been updated");
				
				setResponsePage(DefaultQueryListPage.class);
			}
			
		};
		form.add(PropertyContext.edit("editor", getSetting(), "defaultQueries"));
		
		add(form);
	}

}
