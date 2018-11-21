package io.onedev.server.web.page.admin.issuesetting;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.web.component.issue.statetransition.StateTransitionListPanel;
import io.onedev.server.web.component.issue.statetransition.UseDefaultListener;

@SuppressWarnings("serial")
public class DefaultStateTransitionsPage extends GlobalIssueSettingPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateTransitionListPanel("content", getSetting().getDefaultTransitionSpecs()) {

			@Override
			protected void onChanged(AjaxRequestTarget target) {
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
			}

			@Override
			protected UseDefaultListener getUseDefaultListener() {
				return null;
			}
			
		});
		
	}
	
}
