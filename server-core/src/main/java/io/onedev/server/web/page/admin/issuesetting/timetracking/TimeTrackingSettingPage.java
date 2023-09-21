package io.onedev.server.web.page.admin.issuesetting.timetracking;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class TimeTrackingSettingPage extends IssueSettingPage {

	public TimeTrackingSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var timeTrackingSetting = getSettingManager().getIssueSetting().getTimeTrackingSetting();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				var issueSetting = getSettingManager().getIssueSetting();
				issueSetting.setTimeTrackingSetting(timeTrackingSetting);
				getSettingManager().saveIssueSetting(issueSetting);
				Session.get().success("Time tracking settings have been saved");
			}
		};
		form.add(BeanContext.edit("editor", timeTrackingSetting));
		add(form);
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Time Tracking Settings</span>").setEscapeModelStrings(false);
	}
	
}
